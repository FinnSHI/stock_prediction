package com.finn.stock.service.impl;

import com.finn.stock.message.entity.CommonResult;
import com.finn.stock.repository.dao.UserInfoDao;
import com.finn.stock.repository.entity.UserInfoDO;
import com.finn.stock.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/*
 * @description: user
 * @author: Finn
 * @create: 2022/07/16 16:28
 */
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserInfoDao userInfoDao;

    @Transactional
    @Override
    public CommonResult<String> createUser(String id, BigDecimal cash) {
        if (cash.compareTo(BigDecimal.ZERO) < 0) {
            return CommonResult.fail("用户金额不能小于0");
        }

        userInfoDao.insert(UserInfoDO.builder().userId(id).cash(cash).build());
        return CommonResult.success("用户创建成功");
    }
}
