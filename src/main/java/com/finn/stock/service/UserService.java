package com.finn.stock.service;

import com.finn.stock.message.entity.CommonResult;
import com.finn.stock.vo.UserInfoVO;
import com.finn.stock.vo.UserReturnVO;

import java.math.BigDecimal;
import java.util.List;

/*
 * @description: user
 * @author: Finn
 * @create: 2022/07/16 16:28
 */
public interface UserService {

    CommonResult<String> createUser(String id, BigDecimal cash);


    CommonResult<List<UserReturnVO>> getCumuReturn(String id);

    CommonResult<UserInfoVO> getUserInfo(String id);
}
