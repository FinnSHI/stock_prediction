package com.finn.stock.repository.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

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

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:SS")
    private String date;

    private String selectedStock;

    private BigDecimal userReturn;

    private BigDecimal cumuReturn;

    private String selectedStockYest;

    private BigDecimal lastCumuReturn;
}
