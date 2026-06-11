package com.sky.controller.notify;

import com.alibaba.druid.support.json.JSONUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sky.properties.WeChatProperties;
import com.sky.service.OrderService;
import com.wechat.pay.contrib.apache.httpclient.util.AesUtil;
import com.wechat.pay.contrib.apache.httpclient.util.PemUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.entity.ContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.security.Signature;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.HashMap;

/**
 * 支付回调相关接口
 */
@RestController
@RequestMapping("/notify")
@Slf4j
public class PayNotifyController {

    @Autowired
    private OrderService orderService;
    @Autowired
    private WeChatProperties weChatProperties;

    /**
     * 支付成功回调
     * 安全处理流程：读取 body → 验签 → 解密 → 幂等业务处理 → 响应微信
     */
    @RequestMapping("/paySuccess")
    public void paySuccessNotify(HttpServletRequest request, HttpServletResponse response) throws Exception {

        // 1. 读取请求体（签名验证和解密都依赖原始 body）
        String body = readData(request);
        log.info("支付成功回调，原始 body：{}", body);

        // 2. 验证微信回调签名（防伪造请求 + 防重放攻击）
        if (!verifySignature(request, body)) {
            // 验签不通过
            log.error("微信回调签名验证失败，疑似伪造请求，body={}", body);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        // 3. 解密业务数据
        String plainText = decryptData(body);
        log.info("回调解密成功");

        JSONObject jsonObject = JSON.parseObject(plainText);
        String outTradeNo = jsonObject.getString("out_trade_no");
        String transactionId = jsonObject.getString("transaction_id");

        log.info("回调订单号：{}，微信交易号：{}", outTradeNo, transactionId);

        // 4. 幂等业务处理（paySuccess 内部已做幂等检查）
        orderService.paySuccess(outTradeNo);

        // 5. 告知微信处理成功（否则微信会重试）
        responseToWeixin(response);
    }

    /**
     * 验证微信回调通知的签名
     * 规范：https://pay.weixin.qq.com/wiki/doc/apiv3/wechatpay/wechatpay4_1.shtml
     *
     * 验签消息格式：
     *   {Wechatpay-Timestamp}\n - 验签的时间戳
     *   {Wechatpay-Nonce}\n - 验签的随机字符串
     *   {body}\n
     */
    private boolean verifySignature(HttpServletRequest request, String body) {
        String timestamp = request.getHeader("Wechatpay-Timestamp");
        String nonce     = request.getHeader("Wechatpay-Nonce");
        String signature = request.getHeader("Wechatpay-Signature");

        // 头信息缺失
        if (!StringUtils.hasText(timestamp) || !StringUtils.hasText(nonce) || !StringUtils.hasText(signature)) {
            log.warn("微信回调缺少必要签名头信息，timestamp={}, nonce={}", timestamp, nonce);
            return false;
        }

        // 防重放：时间戳不得早于当前时间 5 分钟
        long ts;
        try {
            ts = Long.parseLong(timestamp);
        } catch (NumberFormatException e) {
            log.warn("微信回调时间戳格式非法：{}", timestamp);
            return false;
        }
        if (Math.abs(System.currentTimeMillis() / 1000 - ts) > 300) {
            log.warn("微信回调时间戳超出容忍范围（±5min），timestamp={}", timestamp);
            return false;
        }

        // 平台证书文件未配置时，降级为仅时间戳校验并打印警告
        String certFilePath = weChatProperties.getWeChatPayCertFilePath();
        if (!StringUtils.hasText(certFilePath)) {
            log.warn("未配置微信平台证书路径（sky.wechat.we-chat-pay-cert-file-path），跳过 RSA 签名验证。" +
                     "生产环境请务必配置平台证书以保障安全！");
            return true;
        }

        // 使用微信平台证书公钥验证 RSA-SHA256 签名
        try {
            X509Certificate certificate = PemUtil.loadCertificate(
                    new FileInputStream(new File(certFilePath)));

            // 构造待验签消息
            String message = timestamp + "\n" + nonce + "\n" + body + "\n";

            Signature sig = Signature.getInstance("SHA256withRSA");
            sig.initVerify(certificate.getPublicKey());
            sig.update(message.getBytes(StandardCharsets.UTF_8));

            byte[] signatureBytes = Base64.getDecoder().decode(signature);
            boolean result = sig.verify(signatureBytes);
            if (!result) {
                log.error("微信回调 RSA 签名验证未通过");
            }
            return result;
        } catch (Exception e) {
            log.error("微信回调签名验证异常，certFilePath={}", certFilePath, e);
            return false;
        }
    }

    /**
     * 读取请求体原始字符串
     */
    private String readData(HttpServletRequest request) throws Exception {
        BufferedReader reader = request.getReader();
        StringBuilder result = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            if (result.length() > 0) {
                result.append("\n");
            }
            result.append(line);
        }
        return result.toString();
    }

    /**
     * AES-GCM 解密微信回调中的业务数据
     * @return 微信回调中的业务数据
     */
    private String decryptData(String body) throws Exception {
        JSONObject resultObject = JSON.parseObject(body);
        JSONObject resource = resultObject.getJSONObject("resource");
        // 通知资源数据，包括
        // Base64编码后的回调数据密文
        String ciphertext    = resource.getString("ciphertext");
        // 参与解密的随机串
        String nonce         = resource.getString("nonce");
        // 参与解密的附加数据
        String associatedData = resource.getString("associated_data");

        AesUtil aesUtil = new AesUtil(weChatProperties.getApiV3Key().getBytes(StandardCharsets.UTF_8));
        return aesUtil.decryptToString(
                associatedData.getBytes(StandardCharsets.UTF_8),
                nonce.getBytes(StandardCharsets.UTF_8),
                ciphertext);
    }

    /**
     * 向微信返回处理成功响应
     * 若不返回此结构，微信将在 24 小时内多次重试
     */
    private void responseToWeixin(HttpServletResponse response) throws Exception {
        response.setStatus(200);
        HashMap<Object, Object> map = new HashMap<>();
        map.put("code", "SUCCESS");
        map.put("message", "SUCCESS");
        response.setHeader("Content-type", ContentType.APPLICATION_JSON.toString());
        response.getOutputStream().write(JSONUtils.toJSONString(map).getBytes(StandardCharsets.UTF_8));
        response.flushBuffer();
    }
}
