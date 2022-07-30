package com.finn.stock.service;

import com.finn.stock.message.entity.CommonResult;

import java.math.BigDecimal;
import java.util.List;

/*
 * @description: 调用python相关算法
 * @author: Finn
 * @create: 2022/07/15 11:10
 */
public interface StockService {


    CommonResult<List<BigDecimal>> getBenchmarkReturn(String startDate, String endDate);
}
