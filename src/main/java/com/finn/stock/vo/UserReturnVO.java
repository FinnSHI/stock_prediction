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
 * @create: 2022/07/30 19:20
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserReturnVO {

    /*
    * user id
    * */
    private String userId;

    /*
    * date
    * */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:SS")
    private String date;

    /*
    * 累积的 return
    * */
    private BigDecimal cumuReturn;
}
