package com.finn.stock.controller;

import com.finn.stock.controller.request.UserCreateRequest;
import com.finn.stock.message.entity.CommonResult;
import com.finn.stock.service.UserService;
import com.finn.stock.vo.UserReturnVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

/*
 * @description: 用户
 * @author: Finn
 * @create: 2022/07/15 17:44
 */
@RestController
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/user/create")
    public CommonResult<String> createUser(@RequestBody UserCreateRequest request) {

        return userService.createUser(request.getId(), new BigDecimal(request.getCash()));
    }

    @PostMapping("/user/return")
    public CommonResult<List<UserReturnVO>> getCumuReturn(@RequestBody UserCreateRequest request) {

        return userService.getCumuReturn(request.getId());
    }
}
