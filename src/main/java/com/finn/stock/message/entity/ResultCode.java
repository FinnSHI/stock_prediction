package com.finn.stock.message.entity;



/**
 * 枚举了一些常用API操作码
 * Created by macro on 2019/4/19.
 */
public enum ResultCode implements IErrorCode {
    SUCCESS(200, "执行成功"),
    FAILED(500, "执行失败"),
    VALIDATE_FAILED(406, "参数检验失败"),
    UNAUTHORIZED(401, "尚未登录或token已经过期"),
    FORBIDDEN(403, "没有权限"),
    SQL_FAILED(409, "用户sql语法错误"),
    PARAMETER_FAILED(410, "前端传参错误");

    private Integer code;
    private String message;

    private ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
