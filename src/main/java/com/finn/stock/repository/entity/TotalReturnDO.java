package com.finn.stock.repository.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.ibatis.annotations.Mapper;

import java.math.BigDecimal;
import java.util.Date;

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
    private Date date;

    /*
    * 收益率
    * */
    private BigDecimal totalReturn;
}
