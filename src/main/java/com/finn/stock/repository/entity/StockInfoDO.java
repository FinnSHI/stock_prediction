package com.finn.stock.repository.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/*
 * @description: stock_info
 * @author: Finn
 * @create: 2022/07/19 14:05
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName(value = "stock_info", schema="stock")
public class StockInfoDO {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /*
    * month
    * */
    private Integer month;

    /*
    * stock_list
    * */
    private String stockList;
}
