package com.finn.stock.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 枚举信息：错误code/message
 */
@Getter
@AllArgsConstructor
public enum ResultCodeEnum {

    // 成功代码
    SUCCESS("00000", "执行成功"),

    // 失败代码
    IDENTITY_VERIFICATION_FAILED("A0000", "token不合法"),
    QUERY_ERROR("A0001", "数据库查询不到数据"),
    ILLEGAL_PARAM("A0002", "参数校验失败"),
    DATABASE_DRIVER_ERROR("A0004", "数据库驱动加载失败"),
    DATABASE_CONNECT_ERROR("A0005","数据库连接失败"),
    NULL_PARAM("A0006", "参数为空"),
    SQL_SYNTAX_ERROR("A0007", "sql句法错误"),
    API_POST_ERROR("A0008", "接口调用失败");

    private String code;
    private String message;

}
