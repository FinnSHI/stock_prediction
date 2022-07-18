package com.finn.stock.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.finn.stock.exception.ApiException;
import com.finn.stock.message.entity.CommonResult;
import com.finn.stock.repository.dao.TotalReturnDao;
import com.finn.stock.repository.entity.TotalReturnDO;
import com.finn.stock.service.ReturnService;
import com.finn.stock.vo.BacktestVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/*
 * @description: return
 * @author: Finn
 * @create: 2022/07/16 17:12
 */
@Slf4j
@Service
public class ReturnServiceImpl implements ReturnService {

    @Autowired
    private TotalReturnDao totalReturnDao;

    @Override
    public CommonResult<BacktestVO> getBackTest(String startDate, String endDate) {
        // TODO: 异步编排：①读数据库 ②通过python得到maxBacktest ②通过python得到annualized return
        // period return
        BigDecimal firstReturn = totalReturnDao.selectOne(new LambdaQueryWrapper<TotalReturnDO>()
                .select(TotalReturnDO::getTotalReturn)
                .eq(TotalReturnDO::getDate, startDate)).getTotalReturn();
        List<BigDecimal> returns = totalReturnDao.selectList(new LambdaQueryWrapper<TotalReturnDO>()
                        .select(TotalReturnDO::getTotalReturn)
                        .ge(TotalReturnDO::getDate, startDate)
                        .le(TotalReturnDO::getDate, endDate))
                .stream()
                .map((totalReturnDO ->
                        totalReturnDO.getTotalReturn().divide(firstReturn, 2, RoundingMode.HALF_UP)))
                .collect(Collectors.toList());

        // total return
        BigDecimal totalReturn = returns.get(returns.size() - 1).subtract(returns.get(0));

        // annualized return，调用python
        String ar = null;
        BigDecimal annualizedReturn = null;
        try {
            ar = getAnnualizedReturn(startDate, endDate, returns.get(0).toString(),
                returns.get(returns.size() - 1).toString());
        } catch (ApiException e) {
            log.error(e.getErrorMsg());
            return CommonResult.fail(e.getMessage());
        }
        if (!Objects.isNull(ar)) {
            annualizedReturn = new BigDecimal(ar);
        }

        // best test
        String mb = getMaxBacktest(returns);
        BigDecimal maxBacktest = null;
        try {
            mb = getMaxBacktest(returns);
        } catch (ApiException e) {
            log.error(e.getErrorMsg());
            return CommonResult.fail(e.getMessage());
        }
        if (!Objects.isNull(mb)) {
            maxBacktest = new BigDecimal(mb);
        }


        return CommonResult.success(BacktestVO.builder()
                        .totalReturn(totalReturn)
                        .annualizedReturn(annualizedReturn)
                        .periodReturn(returns)
                        .maxBacktest(maxBacktest)
                        .build());
    }

    public String getAnnualizedReturn(String firstDate, String endDate, String firstReturn, String lastReturn) {
        BufferedReader in = null;
        BufferedReader err = null;
        try {
            String[] argvs = new String[] { "python", "\\stock\\src\\main\\resources\\python\\annualized_rate.py", firstDate, firstReturn, endDate, lastReturn};
            Process proc = Runtime.getRuntime().exec(argvs);// 执行py文件
            in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            err = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
            String line = null;
            StringBuilder error = new StringBuilder();
            if (!Objects.isNull(line = in.readLine())) {
                return line;
            }
            while (!Objects.isNull(line = err.readLine())) {
                System.out.println(line);
                error.append(line);
            }
            if (error.length() > 0) {
                throw new ApiException(error.toString());
            }
            return null;
            // proc.waitFor();
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new ApiException("调用Python算法失败！");
        } finally {
            try {
                if (!Objects.isNull(in))
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public String getMaxBacktest(List<BigDecimal> returns) {
        BufferedReader in = null;
        BufferedReader err = null;
        ArrayList<String> py = new ArrayList<>(Arrays.asList("python",
                "\\stock\\src\\main\\resources\\python\\max_backtest.py"));
        for (BigDecimal r : returns) {
            py.add(r.toString());
        }
        try {
            String[] argvs = py.toArray(new String[0]);
            Process proc = Runtime.getRuntime().exec(argvs);// 执行py文件
            in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            err = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
            String line = null;
            StringBuilder error = new StringBuilder();
            if (!Objects.isNull(line = in.readLine())) {
                return line;
            }
            while (!Objects.isNull(line = err.readLine())) {
                System.out.println(line);
                error.append(line);
            }
            if (error.length() > 0) {
                throw new ApiException(error.toString());
            }
            proc.waitFor();

            return null;
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new ApiException("调用Python算法失败！");
        } finally {
            try {
                if (!Objects.isNull(in))
                    in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
