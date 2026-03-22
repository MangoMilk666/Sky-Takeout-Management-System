package com.sky.service.impl;

import com.sky.mapper.OrderMapper;
import com.sky.service.ReportService;
import com.sky.vo.TurnoverReportVO;
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
}
