package com.finn.stock.controller;

import com.finn.stock.controller.request.DateRequest;
import com.finn.stock.message.entity.CommonResult;
import com.finn.stock.service.StockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

/*
 * @description: stock
 * @author: Finn
 * @create: 2022/07/30 20:41
 */
@RestController
public class StockController {

    @Autowired
    private StockService stockService;

    @PostMapping("/return/benchmark")
    public CommonResult<List<BigDecimal>> getBenchMark(@RequestBody DateRequest request) {

        return stockService.getBenchmarkReturn(request.getStartDate(), request.getEndDate());
    }
}
