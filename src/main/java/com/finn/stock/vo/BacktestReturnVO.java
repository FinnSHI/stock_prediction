package com.finn.stock.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/*
 * @description:
 * @author: Finn
 * @create: 2022/07/31 22:19
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BacktestReturnVO {

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:SS")
    private String date;

    private BigDecimal periodReturn;
}
