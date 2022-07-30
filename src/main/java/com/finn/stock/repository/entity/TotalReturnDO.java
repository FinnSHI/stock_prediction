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
 * @description: total_return
 * @author: Finn
 * @create: 2022/07/16 17:06
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName(value = "total_return", schema="stock")
public class TotalReturnDO {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /*
    * 日期
    * */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:SS")
    private String date;

    /*
     * 基准线的收益率
     * */
    private BigDecimal benchmarkReturn;

    /*
    * 收益率
    * */
    private BigDecimal totalReturn;
}
