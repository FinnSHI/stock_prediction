package com.finn.stock.controller;

import com.finn.stock.controller.request.DateRequest;
import com.finn.stock.message.entity.CommonResult;
import com.finn.stock.service.ReturnService;
import com.finn.stock.vo.BacktestVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/*
 * @description: 收益率
 * @author: Finn
 * @create: 2022/07/16 17:11
 */
@RestController
public class ReturnController {

    @Autowired
    private ReturnService returnService;

    @PostMapping("/return/backtest")
    public CommonResult<BacktestVO> getBackTest(@RequestBody DateRequest request) {

        return returnService.getBackTest(request.getStartDate(), request.getEndDate());
    }
}
