package com.finn.stock.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.finn.stock.message.entity.CommonResult;
import com.finn.stock.repository.dao.TotalReturnDao;
import com.finn.stock.repository.entity.TotalReturnDO;
import com.finn.stock.service.StockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;


/*
 * @description: 调用python相关算法
 * @author: Finn
 * @create: 2022/07/15 11:10
 */
@Service
public class StockServiceImpl implements StockService {

    @Autowired
    private TotalReturnDao totalReturnDao;

    @Override
    public CommonResult<List<BigDecimal>> getBenchmarkReturn(String startDate, String endDate) {
        BigDecimal firstReturn = totalReturnDao.selectOne(new LambdaQueryWrapper<TotalReturnDO>()
                .select(TotalReturnDO::getTotalReturn)
                .eq(TotalReturnDO::getDate, startDate)).getTotalReturn();
        List<BigDecimal> returns = totalReturnDao.selectList(new LambdaQueryWrapper<TotalReturnDO>()
                        .select(TotalReturnDO::getTotalReturn)
                        .ge(TotalReturnDO::getDate, startDate)
                        .le(TotalReturnDO::getDate, endDate))
                .stream()
                .map((totalReturnDO ->
                        totalReturnDO.getTotalReturn().divide(firstReturn, 14, RoundingMode.HALF_UP)))
                .collect(Collectors.toList());

        return CommonResult.success(returns);
    }
}
