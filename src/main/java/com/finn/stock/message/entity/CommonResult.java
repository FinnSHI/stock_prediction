package com.finn.stock.message.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommonResult<T> {
    private Integer code;
    private String message;
    private T result;

    /**
     * 返回成功
     *
     * @param <T>
     * @return
     */
    public static <T> CommonResult<T> success() {
        return new CommonResult<T>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), null);
    }

    /**
     * 返回成功结果
     *
     * @param result
     * @param <T>
     * @return
     */
    public static <T> CommonResult<T> success(T result) {
        return new CommonResult<T>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), result);
    }

    /**
     * 返回成功结果，携带信息
     *
     * @param result
     * @param <T>
     * @return
     * @Param message
     */
    public static <T> CommonResult<T> success(T result, String message) {
        return new CommonResult<T>(ResultCode.SUCCESS.getCode(), message, result);
    }

    /**
     * 失败返回结果
     *
     * @param errorCode 错误码
     */
    public static <T> CommonResult<T> fail(IErrorCode errorCode) {
        return new CommonResult<T>(errorCode.getCode(), errorCode.getMessage(), null);
    }

    /**
     * 失败返回错误码，和指定信息
     *
     * @param errorCode 错误码
     * @param message   错误信息
     */
    public static <T> CommonResult<T> fail(IErrorCode errorCode, String message) {
        return new CommonResult<T>(errorCode.getCode(), message, null);
    }

    /**
     * 执行失败返回结果，携带信息
     *
     * @param message 反馈信息
     */
    public static <T> CommonResult<T> fail(String message) {
        return new CommonResult<T>(ResultCode.FAILED.getCode(), message, null);
    }

    /**
     * 执行失败，返回默认信息
     */
    public static <T> CommonResult<T> fail() {
        return new CommonResult<T>(ResultCode.FAILED.getCode(), ResultCode.FAILED.getMessage(), null);
    }

}

