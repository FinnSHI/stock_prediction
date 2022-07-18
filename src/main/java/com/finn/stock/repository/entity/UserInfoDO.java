package com.finn.stock.repository.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/*
 * @description: user_info
 * @author: Finn
 * @create: 2022/07/16 16:20
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName(value = "user_info", schema="stock")
public class UserInfoDO {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private String userId;

    private BigDecimal cash;
}
