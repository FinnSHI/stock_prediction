package com.finn.stock.controller.request;

import lombok.Data;

import java.math.BigDecimal;

/*
 * @description: 创建用户
 * @author: Finn
 * @create: 2022/07/16 14:21
 */
@Data
public class UserCreateRequest {

    String id;

    String cash;
}
