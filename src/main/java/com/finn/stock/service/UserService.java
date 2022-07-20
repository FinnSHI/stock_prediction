package com.finn.stock.service;

import com.finn.stock.message.entity.CommonResult;

import java.math.BigDecimal;

/*
 * @description: user
 * @author: Finn
 * @create: 2022/07/16 16:28
 */
public interface UserService {

    CommonResult<String> createUser(String id, BigDecimal cash);


}
