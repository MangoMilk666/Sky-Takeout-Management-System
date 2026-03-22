package com.sky.service.impl;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.vo.OrderReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ReportServiceImpl implements ReportService {
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private UserMapper userMapper;
    /**
     * 获取区间日期内的营业额数据
     */
    @Override
    public TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end) {
        // begin - end日期集合
        List<LocalDate> dateList = new ArrayList();
        LocalDate date = begin;
        dateList.add(date);
        while (date.isBefore(end)){
            date = date.plusDays(1);
            dateList.add(date);
        }
        // 取出元素逗号分隔
        String dateString = StringUtils.join(dateList, ",");

        List<BigDecimal> turnoverList = new ArrayList<>();
        for (LocalDate d : dateList) {
            // db中存的是时间，所以要传入起始时间查询
            LocalDateTime initialTime = LocalDateTime.of(d, LocalTime.MIN);
            LocalDateTime finalTime = LocalDateTime.of(d, LocalTime.MAX);
            BigDecimal oneDayTurnover = orderMapper.getTurnoverStatistics(initialTime, finalTime);
            turnoverList.add(oneDayTurnover);
        }
        String turnoverString = StringUtils.join(turnoverList, ",");
        return TurnoverReportVO
                .builder()
                .dateList(dateString)
                .turnoverList(turnoverString)
                .build();
    }

    /**
     * 区间内用户统计
     */
    @Override
    public UserReportVO getUserStatistics(LocalDate begin, LocalDate end) {
        // begin - end日期集合
        List<LocalDate> dateList = new ArrayList();
        LocalDate date = begin;
        dateList.add(date);
        while (date.isBefore(end)){
            date = date.plusDays(1);
            dateList.add(date);
        }

        List<Integer> totalUserList = new ArrayList<>();
        List<Integer> newUserList = new ArrayList<>();
        for (LocalDate d : dateList) {
            LocalDateTime initialTime = LocalDateTime.of(d, LocalTime.MIN);
            LocalDateTime finalTime = LocalDateTime.of(d, LocalTime.MAX);
            int userNum = userMapper.getUserNum(finalTime);
            int newUser = !totalUserList.isEmpty() ? userNum - totalUserList.get(totalUserList.size()-1) : userNum;
            totalUserList.add(userNum);
            newUserList.add(newUser);
        }
        return UserReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .totalUserList(StringUtils.join(totalUserList, ","))
                .newUserList(StringUtils.join(newUserList, ","))
                .build();
    }

    @Override
    public OrderReportVO getOrderStatistics(LocalDate begin, LocalDate end) {
        // begin - end日期集合
        List<LocalDate> dateList = new ArrayList();
        LocalDate date = begin;
        dateList.add(date);
        while (date.isBefore(end)){
            date = date.plusDays(1);
            dateList.add(date);
        }
        //每日订单数，以逗号分隔，例如：260,210,215
        List<Integer> orderCountList = new ArrayList<>();

        //每日有效订单数，以逗号分隔，例如：20,21,10
        List<Integer> validOrderCountList = new ArrayList<>();

        //订单总数
        int totalOrderCount = 0;
        //有效订单数
        int validOrderCount = 0;

        for (LocalDate d : dateList) {
            LocalDateTime initialTime = LocalDateTime.of(d, LocalTime.MIN);
            LocalDateTime finalTime = LocalDateTime.of(d, LocalTime.MAX);
            int orderNum = orderMapper.countByRange(initialTime, finalTime);
            int validOrderNum = orderMapper.countByStatusAndRange(initialTime, finalTime, Orders.COMPLETED);
            orderCountList.add(orderNum);
            totalOrderCount += orderNum;

            validOrderCountList.add(validOrderNum);
            validOrderCount += validOrderNum;
        }

        //订单完成率
        Double orderCompletionRate = totalOrderCount == 0 ? 0 : validOrderCount*1.0 / totalOrderCount;
        return OrderReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .orderCountList(StringUtils.join(orderCountList, ","))
                .validOrderCountList(StringUtils.join(validOrderCountList, ","))
                .totalOrderCount(totalOrderCount)
                .validOrderCount(validOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .build();
    }


}
