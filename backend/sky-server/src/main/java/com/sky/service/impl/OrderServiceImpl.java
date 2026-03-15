package com.sky.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.DishPageQueryDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.*;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.OrderBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.*;
import com.sky.result.PageResult;
import com.sky.service.OrderService;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
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
    private UserMapper userMapper;
    // 注入vx支付工具类
    @Autowired
    private WeChatPayUtil weChatPayUtil;

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

        //调用微信支付接口，生成预支付交易单
        JSONObject jsonObject = weChatPayUtil.pay(
                ordersPaymentDTO.getOrderNumber(), //商户订单号
                new BigDecimal(0.01), //支付金额，单位 元
                "苍穹外卖订单", //商品描述
                user.getOpenid() //微信用户的openid
        );

        if (jsonObject.getString("code") != null && jsonObject.getString("code").equals("ORDERPAID")) {
            throw new OrderBusinessException("该订单已支付");
        }

        OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
        vo.setPackageStr(jsonObject.getString("package"));

        return vo;
    }

    /**
     * 支付成功，修改订单状态
     *
     * @param outTradeNo
     */
    @Transactional
    public void paySuccess(String outTradeNo) {

        // 根据订单号查询订单
        Orders ordersDB = orderMapper.getByNumber(outTradeNo);

        // 根据订单id更新订单的状态、支付方式、支付状态、结账时间
        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .checkoutTime(LocalDateTime.now())
                .build();

        orderMapper.update(orders);
    }

    /**
     * 分页查询历史订单
     * @return
     */
    @Override
    public PageResult pageQuery(OrdersPageQueryDTO ordersPageQueryDTO) {
        // 分页参数
        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());
        // 获取当前用户id
        ordersPageQueryDTO.setUserId(BaseContext.getCurrentId());

        // 分页查询，最终返回Page<OrderHistoryVO>对象
        Page<OrderHistoryVO> historyOrderPage = orderMapper.pageQuery(ordersPageQueryDTO);
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
    public void cancelOrder(Long id) {
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
}
