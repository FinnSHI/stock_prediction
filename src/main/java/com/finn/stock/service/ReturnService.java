package com.finn.stock.service;

import com.finn.stock.message.entity.CommonResult;
import com.finn.stock.vo.BacktestVO;

/*
 * @description: return
 * @author: Finn
 * @create: 2022/07/16 17:12
 */
public interface ReturnService {

    CommonResult<BacktestVO> getBackTest(String startDate, String endDate);
}
