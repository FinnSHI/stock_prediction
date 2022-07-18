package com.finn.stock.controller.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
/*
 * @description: date
 * @author: Finn
 * @create: 2022/07/16 19:45
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DateRequest {

    /*
    * 开始日期
    * */
    private String startDate;

    /*
    * 结束日期
    * */
    private String endDate;
}
