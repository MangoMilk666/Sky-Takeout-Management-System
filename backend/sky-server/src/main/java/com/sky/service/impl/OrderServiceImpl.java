package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.*;
import com.sky.entity.*;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.OrderBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.*;
import com.sky.result.PageResult;
import com.sky.service.OrderService;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.*;
import com.sky.websocket.WebSocketServer;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.bridge.Message;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@Slf4j
public class OrderServiceImpl implements OrderService {
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private AddressBookMapper addressBookMapper;
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private CouponMapper couponMapper;
    @Autowired
    private UserCouponMapper userCouponMapper;
    @Autowired
    private UserMapper userMapper;
    // 注入vx支付工具类
    @Autowired
    private WeChatPayUtil weChatPayUtil;
    @Autowired
    private WebSocketServer webSocketServer;

    /**
     * 用户下单
     * @param ordersSubmitDTO
     * @return
     */
    @Transactional
    @Override
    public OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO) {
        // 1.处理业务异常情况
        // 地址薄为空
        AddressBook addressBook = addressBookMapper.getById(ordersSubmitDTO.getAddressBookId());
        if (addressBook == null) {
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }
        //购物车为空
        List<ShoppingCart> shoppingCartList = shoppingCartMapper.getList(BaseContext.getCurrentId());
        if (shoppingCartList == null || shoppingCartList.isEmpty()) {
            throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }

        // 2.订单表插入1条数据
        // 封装一个Orders对象
        Orders order = new Orders();
        BeanUtils.copyProperties(ordersSubmitDTO, order);
        order.setNumber(String.valueOf(System.currentTimeMillis()) ); //订单号
        order.setStatus(Orders.PENDING_PAYMENT); //设置状态：待付款
        order.setUserId(BaseContext.getCurrentId()); //用户id
        order.setOrderTime(LocalDateTime.now());
        order.setPayStatus(Orders.UN_PAID);
        BigDecimal originalAmount = order.getAmount() == null ? BigDecimal.ZERO : order.getAmount();
        order.setOriginalAmount(originalAmount);
        order.setDiscountAmount(originalAmount);

        Long couponId = ordersSubmitDTO.getCouponId();
        if (couponId != null) {
            Coupon coupon = couponMapper.getById(couponId);
            if (coupon == null) {
                throw new OrderBusinessException(MessageConstant.COUPON_NOT_FOUND);
            }
            LocalDateTime now = LocalDateTime.now();
            if (coupon.getBeginTime() == null || coupon.getEndTime() == null || now.isBefore(coupon.getBeginTime()) || now.isAfter(coupon.getEndTime())) {
                throw new OrderBusinessException(MessageConstant.COUPON_NOT_AVAILABLE);
            }

            Long userId = BaseContext.getCurrentId();
            Long userCouponId = userCouponMapper.getUnusedIdByCouponId(userId, couponId);
            if (userCouponId == null) {
                throw new OrderBusinessException(MessageConstant.COUPON_NOT_AVAILABLE);
            }

            BigDecimal discountAmount = originalAmount;
            if (coupon.getDiscountType() != null && coupon.getDiscount() != null) {
                if (coupon.getDiscountType() == 1) {
                    discountAmount = originalAmount.multiply(coupon.getDiscount());
                } else if (coupon.getDiscountType() == 2) {
                    discountAmount = originalAmount.subtract(coupon.getDiscount());
                }
            }
            if (discountAmount.compareTo(BigDecimal.ZERO) < 0) {
                discountAmount = BigDecimal.ZERO;
            }
            discountAmount = discountAmount.setScale(2, BigDecimal.ROUND_HALF_UP);

            order.setCouponId(couponId);
            order.setDiscountAmount(discountAmount);
            order.setAmount(discountAmount);

            Integer updated = userCouponMapper.markUsed(userCouponId, userId, now);
            if (updated == null || updated == 0) {
                throw new OrderBusinessException(MessageConstant.COUPON_NOT_AVAILABLE);
            }
        }
//        order.setUserName(addressBook.getConsignee()); //用户名，收货人？
        order.setPhone(addressBook.getPhone());
        order.setAddress(addressBook.getDetail()); // 用户地址
        order.setConsignee(addressBook.getConsignee()); //收货人

        orderMapper.insert(order); //插入后设置返回id

        // 3.订单明细表插入n条数据
        // 订单明细列表数据在购物车列表数据基础上得到
        List<OrderDetail> orderDetailList = new ArrayList<>();
        for (ShoppingCart shoppingCart : shoppingCartList) {
            OrderDetail orderDetail = new OrderDetail();
            BeanUtils.copyProperties(shoppingCart, orderDetail);
            orderDetail.setOrderId(order.getId());
            orderDetailList.add(orderDetail);
        }
        orderDetailMapper.insertBatchOrderDetail(orderDetailList);
        // 4.清空用户当前的购物车数据
        shoppingCartMapper.clearCart(BaseContext.getCurrentId());
        // 5.封装返回结果VO
        OrderSubmitVO orderSubmitVO = new OrderSubmitVO();
        orderSubmitVO.setId(order.getId());
        orderSubmitVO.setOrderNumber(order.getNumber());
        orderSubmitVO.setOrderAmount(order.getAmount());
        orderSubmitVO.setOrderTime(order.getOrderTime());
        return orderSubmitVO;
    }

    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        // 当前登录用户id
        Long userId = BaseContext.getCurrentId();
        User user = userMapper.getById(userId);

        // pay方法封装调用了微信支付接口，生成预支付交易单后的返回结果
        // 微信统一下单成功，返回二次签名后供微信支付正式调用
        // 微信统一下单失败，返回返回微信官方接口的错误响应
        JSONObject jsonObject = weChatPayUtil.pay(
                ordersPaymentDTO.getOrderNumber(), //商户订单号
                new BigDecimal(0.01), //支付金额，单位 元
                "Smart-Dining平台订单", //商品描述
                user.getOpenid() //微信用户的openid
        );

         // 返回结果如果是
         // {
         //   "code": "ORDERPAID",
         //   "message": "订单已支付"
         // }
        if (jsonObject.getString("code") != null && jsonObject.getString("code").equals("ORDERPAID")) {
            throw new OrderBusinessException("该订单已支付");
        }

        /* {
            "code": "INVALID_REQUEST",
                "message": "参数错误"
        }*/
        if (jsonObject.getString("code") != null && jsonObject.getString("code").equals("INVALID_REQUEST")) {
            throw new OrderBusinessException("订单参数错误");
        }


        OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
        vo.setPackageStr(jsonObject.getString("package"));

        return vo;
    }

    /**
     * 验证支付成功后，修改订单状态
     * 要求实现幂等性
     * @param outTradeNo
     */
    @Transactional
    public void paySuccess(String outTradeNo) {

        // 1. 查询订单，防止订单号不存在时 NPE
        Orders ordersDB = orderMapper.getByNumber(outTradeNo);
        if (ordersDB == null) {
            log.warn("paySuccess: 订单不存在，orderNumber={}", outTradeNo);
            return;
        }

        // 2. 幂等检查：已支付则直接返回，防止微信发送重复回调，重复更新订单
        if (Orders.PAID.equals(ordersDB.getPayStatus())) {
            log.info("paySuccess: 订单已处理过，跳过重复处理，orderNumber={}", outTradeNo);
            return;
        }

        // 3. 更新订单状态、支付状态、结账时间
        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .checkoutTime(LocalDateTime.now())
                .build();

        orderMapper.update(orders);

        // 4. 通过 WebSocket 向管理端推送来单提醒
        Map<String, Object> map = new HashMap<>();
        // 1=来单提醒，2=催单
        map.put("type", 1);
        map.put("orderId", ordersDB.getId());
        map.put("content", "订单号：" + outTradeNo);

        String json = JSON.toJSONString(map);
        webSocketServer.sendToAllClient(json);
        log.info("paySuccess: 来单提醒已推送，orderId={}", ordersDB.getId());
    }

    /**
     * 分页查询历史订单
     * @return
     */
    @Override
    public PageResult pageQueryByUser(OrdersPageQueryDTO ordersPageQueryDTO) {
        // 分页参数
        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());
        // 获取当前用户id
        ordersPageQueryDTO.setUserId(BaseContext.getCurrentId());

        // 分页查询，最终返回Page<OrderHistoryVO>对象
        Page<OrderHistoryVO> historyOrderPage = orderMapper.pageQueryByUser(ordersPageQueryDTO);
        for (OrderHistoryVO orderHistoryVO : historyOrderPage.getResult()) {
            List<OrderDetail> orderDetailList = orderDetailMapper.getDetailByOrderId(orderHistoryVO.getId());
            orderHistoryVO.setOrderDetailList(orderDetailList);
        }
        return new PageResult(historyOrderPage.getTotal(), historyOrderPage.getResult());
    }

        /**
         * 测试用：跳过微信支付，直接更新订单状态
         */
        @Override
        public String getEstimatedTimeForTest(OrdersPaymentDTO ordersPaymentDTO) {
            // 1. 根据订单号查询订单
            String orderNumber = ordersPaymentDTO.getOrderNumber();
            Orders orders = orderMapper.getByNumber(orderNumber);

            // 2. 更新订单状态和支付状态
            // 状态：2-待接单，支付状态：1-已支付
            orders.setStatus(Orders.TO_BE_CONFIRMED);
            orders.setPayStatus(Orders.PAID);
            orders.setCheckoutTime(LocalDateTime.now());

            orderMapper.update(orders);

            // 3. 计算预计送达时间（当前时间 + 1小时）
            LocalDateTime estimatedTime = orders.getOrderTime().plusHours(1);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            // 来单提醒推送
            Map map = new HashMap();
            map.put("type", 1); // 1表示来单提醒，2表示客户催单
            map.put("orderId", orders.getId());
            map.put("content", "订单号：" + orderNumber);
            // 通过 websocket 向管理端浏览器推送消息
            String json = JSON.toJSONString(map);
            webSocketServer.sendToAllClient(json);

            return estimatedTime.format(formatter);
        }

    /**
     * 根据订单id查询订单详情
     * @param orderId
     * @return
     */
    @Override
    public OrderHistoryVO getDetailsById(Long orderId) {
        Orders orders = orderMapper.getById(orderId);
        OrderHistoryVO orderHistoryVO = new OrderHistoryVO();
        BeanUtils.copyProperties(orders, orderHistoryVO);
        List<OrderDetail> orderDetailList = orderDetailMapper.getDetailByOrderId(orderId);
        orderHistoryVO.setOrderDetailList(orderDetailList);
        return orderHistoryVO;
    }

    /**
     * 取消订单
     * @param id
     */
    @Override
    @Transactional
    public void cancelOrderByUser(Long id) {
        // 查询到具体订单
        Orders orders = orderMapper.getById(id);
        // 处理特殊情况：订单不存在
        if (orders == null){
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        // 特殊情况：商家已接单/在派送/已完成/已取消的订单无法取消
        if (Objects.equals(orders.getStatus(), Orders.CONFIRMED) || Objects.equals(orders.getStatus(), Orders.DELIVERY_IN_PROGRESS) ||
                Objects.equals(orders.getStatus(), Orders.COMPLETED) || Objects.equals(orders.getStatus(), Orders.CANCELLED)) {
            throw new OrderBusinessException("取消失败!" + MessageConstant.ORDER_STATUS_ERROR);
        }
        // 更新订单状态，取消时间
        orders.setStatus(Orders.CANCELLED);
        orders.setCancelTime(LocalDateTime.now());
        orderMapper.update(orders);
    }

    /**
     * 再来一单
     * @param id
     */
    @Override
    public void placeSameNewOrder(Long id) {
        // 得到再来一单的订单内容
        Orders orders = orderMapper.getById(id);
        // 特殊情况处理
        if (orders == null){
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        // 快捷填充购物车
        List<OrderDetail> orderDetailList = orderDetailMapper.getDetailByOrderId(id);
        for  (OrderDetail orderDetail : orderDetailList) {
            ShoppingCart shoppingCart = new ShoppingCart();
            BeanUtils.copyProperties(orderDetail,shoppingCart);
            shoppingCart.setUserId(BaseContext.getCurrentId());
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartMapper.insert(shoppingCart);
        }
    }

    /**
     * 管理段分页查询订单
     * @param ordersPageQueryDTO
     * @return
     */
    @Override
    public PageResult pageQueryByAdmin(OrdersPageQueryDTO ordersPageQueryDTO) {
        // 分页参数
        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());

        // 分页查询，最终返回Page<OrderHistoryVO>对象
        Page<OrderHistoryVO> historyOrderPage = orderMapper.pageQueryByAdmin(ordersPageQueryDTO);
        for (OrderHistoryVO orderHistoryVO : historyOrderPage.getResult()) {
            StringBuffer orderDishesBuffer = new StringBuffer();
            List<OrderDetail> orderDetailList = orderDetailMapper.getDetailByOrderId(orderHistoryVO.getId());
            for (OrderDetail orderDetail : orderDetailList) {
                orderDishesBuffer.append(orderDetail.getName());
                orderDishesBuffer.append(", ");
            }
            orderHistoryVO.setOrderDishes(orderDishesBuffer.toString());
        }
        return new PageResult(historyOrderPage.getTotal(), historyOrderPage.getResult());
    }

    /**
     * 各个状态的订单数量统计
     * @return
     */
    @Override
    public OrderStatisticsVO getOrderStatistics() {
        OrderStatisticsVO vo = new OrderStatisticsVO();
        Integer confirmed = orderMapper.countByOrderStatus(Orders.CONFIRMED);
        Integer toBeConfirmed = orderMapper.countByOrderStatus(Orders.TO_BE_CONFIRMED);
        Integer deliveryInProgress = orderMapper.countByOrderStatus(Orders.DELIVERY_IN_PROGRESS);
        vo.setConfirmed(confirmed);
        vo.setToBeConfirmed(toBeConfirmed);
        vo.setDeliveryInProgress(deliveryInProgress);
        return vo;
    }

    /**
     * 管理端接单
     */
    @Override
    public void confirmOrderByAdmin(OrdersConfirmDTO ordersConfirmDTO) {
        // 查询出订单
        Orders order = orderMapper.getById(ordersConfirmDTO.getId());
        // 修改订单状态
        order.setStatus(Orders.CONFIRMED);
        // update结果
        orderMapper.update(order);
    }

    /**
     * 管理端拒单
     * @param ordersRejectionDTO
     */
    @Override
    @Transactional
    public void rejectOrderByAdmin(OrdersRejectionDTO ordersRejectionDTO) {
        // 查找订单
        Orders orders = orderMapper.getById(ordersRejectionDTO.getId());
        // 添加拒单原因
        orders.setRejectionReason(ordersRejectionDTO.getRejectionReason());

        orderMapper.update(orders);
    }

    /**
     * 管理端取消订单
     * @param ordersCancelDTO
     */
    @Override
    public void cancelOrderByAdmin(OrdersCancelDTO ordersCancelDTO) {
        // find orders
        Orders order = orderMapper.getById(ordersCancelDTO.getId());
        order.setStatus(Orders.CANCELLED);
        order.setCancelTime(LocalDateTime.now());
        order.setCancelReason(ordersCancelDTO.getCancelReason());

        orderMapper.update(order);
    }

    /**
     * 派送订单
     * @param id
     */
    @Override
    public void deliverOrder(Long id) {
        Orders order = orderMapper.getById(id);
        order.setStatus(Orders.DELIVERY_IN_PROGRESS);
        orderMapper.update(order);
    }

    /**
     * 完成订单
     * @param id
     */
    @Override
    public void completeOrder(Long id) {
        Orders orders = orderMapper.getById(id);
        orders.setStatus(Orders.COMPLETED);
        orders.setDeliveryTime(LocalDateTime.now());
        orderMapper.update(orders);
    }

    /**
     * 用户催单
     */
    @Override
    public void remindByUser(Long id) {
        // 校验订单是否存在
        Orders orders = orderMapper.getById(id);
        if (orders == null){
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        String orderNumber = orders.getNumber();
        Map map = new HashMap();
        // 催单推送
        map.put("type", 2); // 1表示来单提醒，2表示客户催单
        map.put("orderId", id);
        map.put("content", "顾客催单：订单号" + orderNumber);
        // 通过 websocket 向管理端浏览器推送消息
        String json = JSON.toJSONString(map);
        webSocketServer.sendToAllClient(json);
    }


}
