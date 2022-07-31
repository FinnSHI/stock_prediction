package com.finn.stock.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/*
 * @description:
 * @author: Finn
 * @create: 2022/07/31 21:37
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserInfoVO {

    private String userId;

    private BigDecimal currentCash;

    private BigDecimal initialCash;

    private BigDecimal cumuReturn;

    private BigDecimal dailyReturn;

    private BigDecimal annualizedReturn;

    private String stockList;

    private String stockSelected;
}
