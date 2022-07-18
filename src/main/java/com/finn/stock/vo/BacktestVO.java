package com.finn.stock.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/*
 * @description: backtest
 * @author: Finn
 * @create: 2022/07/16 19:45
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BacktestVO {

    /*
    * 整体收益率
    * */
    private BigDecimal totalReturn;

    /*
     * 年化收益率
     * */
    private BigDecimal annualizedReturn;

    /*
     * 最大回撤
     * */
    private BigDecimal maxBacktest;

    /*
    * 收益率
    * */
    private List<BigDecimal> periodReturn;
}
