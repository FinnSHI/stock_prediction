package com.finn.stock.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.finn.stock.message.entity.CommonResult;
import com.finn.stock.repository.dao.UserInfoDao;
import com.finn.stock.repository.dao.UserReturnDao;
import com.finn.stock.repository.entity.UserInfoDO;
import com.finn.stock.repository.entity.UserReturnDO;
import com.finn.stock.service.UserService;
import com.finn.stock.vo.UserReturnVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;

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
    public CommonResult<UserReturnVO> getCumuReturn(String id) {

        UserInfoDO userInfo = userInfoDao.selectOne(new LambdaQueryWrapper<UserInfoDO>().eq(UserInfoDO::getUserId, id));

        List<UserReturnDO> userReturns = userReturnDao.selectList(new LambdaQueryWrapper<UserReturnDO>()
                .eq(UserReturnDO::getUserInfoId, userInfo.getId()));

        UserReturnDO user = null;
        UserReturnVO ret = null;
        try {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            long maxTimeStamp = 0L;
            for (UserReturnDO userReturn : userReturns) {
                if (formatter.parse(userReturn.getDate()).getTime() > maxTimeStamp) {
                    user = userReturn;
                }
            }

             ret = UserReturnVO.builder()
                    .userId(id)
                    .date(user.getDate())
                    .cumuReturn(user.getCumuReturn())
                    .build();
        } catch (Exception e) {
            log.error(e.getMessage());
            return CommonResult.fail("获取数据失败");
        }

        return CommonResult.success(ret);
    }
}
