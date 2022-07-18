package com.finn.stock.exception;

import com.finn.stock.enums.ResultCodeEnum;
import com.finn.stock.message.entity.ResultCode;
import lombok.Getter;

@Getter
public class ApiException extends RuntimeException {
    private String errorCode;

    private String errorMsg;

    public ApiException(ResultCodeEnum resultCodeEnum) {
        this.errorCode = resultCodeEnum.getCode();
        this.errorMsg = resultCodeEnum.getMessage();
    }

    public ApiException(String message) {
        this.errorCode = ResultCode.FAILED.getCode().toString();
        this.errorMsg = message;
    }
}
