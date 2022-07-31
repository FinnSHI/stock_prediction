package com.finn.stock.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.finn.stock.exception.ApiException;
import com.finn.stock.message.entity.CommonResult;
import com.finn.stock.repository.dao.StockInfoDao;
import com.finn.stock.repository.dao.UserInfoDao;
import com.finn.stock.repository.dao.UserReturnDao;
import com.finn.stock.repository.entity.StockInfoDO;
import com.finn.stock.repository.entity.UserInfoDO;
import com.finn.stock.repository.entity.UserReturnDO;
import com.finn.stock.service.UserService;
import com.finn.stock.vo.UserInfoVO;
import com.finn.stock.vo.UserReturnVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/*
 * @description: user
 * @author: Finn
 * @create: 2022/07/16 16:28
 */
@Slf4j
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserInfoDao userInfoDao;

    @Autowired
    private UserReturnDao userReturnDao;

    @Autowired
    private StockInfoDao stockInfoDao;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public CommonResult<String> createUser(String id, BigDecimal cash) {
        if (cash.compareTo(BigDecimal.ZERO) < 0) {
            return CommonResult.fail("用户金额不能小于0");
        }
        SimpleDateFormat formatter= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date(System.currentTimeMillis());
        String format = formatter.format(date);
        UserInfoDO user = UserInfoDO.builder().userId(id).cash(cash).build();
        UserInfoDO existUser = userInfoDao
                .selectOne(new LambdaQueryWrapper<UserInfoDO>()
                .eq(UserInfoDO::getUserId, id));
        if (!Objects.isNull(existUser)) {
            return CommonResult.fail("用户已存在!");
        }
        try {
            userInfoDao.insert(user);
            userReturnDao.insert(UserReturnDO.builder()
                    .userInfoId(user.getId())
                    .date(format)
                    .selectedStock("0000000000")
                    .userReturn(new BigDecimal(1))
                    .cumuReturn(new BigDecimal(1))
                    .selectedStockYest("0000000000")
                    .lastCumuReturn(new BigDecimal(1))
                    .build());
        } catch (Exception e) {
            log.error(e.getMessage());
            return CommonResult.fail("用户创建失败");
        }
        return CommonResult.success("用户创建成功");
    }

    @Override
    public CommonResult<List<UserReturnVO>> getCumuReturn(String id) {

        UserInfoDO userInfo = userInfoDao.selectOne(new LambdaQueryWrapper<UserInfoDO>().eq(UserInfoDO::getUserId, id));
        if (Objects.isNull(userInfo)) {
            return CommonResult.fail("用户不存在！");
        }
        List<UserReturnVO> ret = userReturnDao.selectList(new LambdaQueryWrapper<UserReturnDO>()
                        .eq(UserReturnDO::getUserInfoId, userInfo.getId()))
                .stream()
                .map((userReturnDO) -> UserReturnVO.builder()
                        .userId(id)
                        .date(userReturnDO.getDate())
                        .cumuReturn(userReturnDO.getCumuReturn())
                        .build())
                .collect(Collectors.toList());

//        UserReturnDO user = null;
//        UserReturnVO ret = null;
//        try {
//            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//            long maxTimeStamp = 0L;
//            for (UserReturnDO userReturn : userReturns) {
//                if (formatter.parse(userReturn.getDate()).getTime() > maxTimeStamp) {
//                    user = userReturn;
//                }
//            }
//
//             ret = UserReturnVO.builder()
//                    .userId(id)
//                    .date(user.getDate())
//                    .cumuReturn(user.getCumuReturn())
//                    .build();
//        } catch (Exception e) {
//            log.error(e.getMessage());
//            return CommonResult.fail("获取数据失败");
//        }

        return CommonResult.success(ret);
    }

    @Override
    public CommonResult<UserInfoVO> getUserInfo(String id) {
        UserInfoDO userInfo = userInfoDao.selectOne(new LambdaQueryWrapper<UserInfoDO>().eq(UserInfoDO::getUserId, id));
        if (Objects.isNull(userInfo)) {
            return CommonResult.fail("用户不存在！");
        }
        List<UserReturnDO> userReturns = userReturnDao.selectList(new LambdaQueryWrapper<UserReturnDO>()
                .eq(UserReturnDO::getUserInfoId, userInfo.getId()));
        UserInfoDO userInfoDO = userInfoDao.selectOne(new LambdaQueryWrapper<UserInfoDO>()
                .eq(UserInfoDO::getUserId, id));
        UserReturnDO user = null;
        UserInfoVO ret = null;
        try {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            long maxTimeStamp = 0L;
            for (UserReturnDO userReturn : userReturns) {
                if (formatter.parse(userReturn.getDate()).getTime() > maxTimeStamp) {
                    user = userReturn;
                }
            }
            String date = user.getDate();
            int month = Integer.parseInt(date.split("-")[1]);
            StockInfoDO stockInfoDO = stockInfoDao.selectOne(new LambdaQueryWrapper<StockInfoDO>().eq(StockInfoDO::getMonth, month));

            ret = UserInfoVO.builder()
                    .userId(id)
                    .initialCash(userInfoDO.getCash())
                    .currentCash(userInfoDO.getCash().multiply(user.getCumuReturn()))
                    .stockList(stockInfoDO.getStockList())
                    .stockSelected(user.getSelectedStock())
                    .dailyReturn(user.getUserReturn())
                    .cumuReturn(user.getCumuReturn())
                    .annualizedReturn(new BigDecimal(getAnnualizedReturn(userReturns.get(0).getDate(),
                            user.getDate(),
                            userReturns.get(0).getUserReturn().toString(),
                            user.getUserReturn().toString())))
                    .build();

        } catch (Exception e) {
            log.error(e.getMessage());
            return CommonResult.fail("获取数据失败");
        }

        return CommonResult.success(ret);
    }

    public String getAnnualizedReturn(String firstDate, String endDate, String firstReturn, String lastReturn) {
        BufferedReader in = null;
        BufferedReader err = null;
        try {
            String[] argvs = new String[] { "python", "\\stock\\src\\main\\resources\\python\\annualized_rate.py",
                    firstDate, firstReturn, endDate, lastReturn};
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
}
