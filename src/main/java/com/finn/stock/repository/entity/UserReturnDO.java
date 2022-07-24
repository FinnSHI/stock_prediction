package com.finn.stock.repository.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

/*
 * @description: user_return
 * @author: Finn
 * @create: 2022/07/19 14:07
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName(value = "user_return", schema="stock")
public class UserReturnDO {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private Integer userInfoId;

    private Date date;

    private String selectedStock;

    private BigDecimal userReturn;

    private BigDecimal cumuReturn;

    private String selectedStockYest;
}
