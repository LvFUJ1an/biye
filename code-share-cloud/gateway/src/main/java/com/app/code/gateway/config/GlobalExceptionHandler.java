package com.app.code.gateway.config;


import com.app.code.common.BaseResponse;
import com.app.code.common.ErrorCode;
import com.app.code.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 业务异常处理
     */
    @ExceptionHandler(BusinessException.class)
    public BaseResponse<?> businessExceptionHandler(BusinessException e) {
        log.error("业务异常：{}", e.getMessage(), e);
        return BaseResponse.error(e.getCode(), e.getMessage());
    }

    /**
     * 运行时异常处理
     */
    @ExceptionHandler(RuntimeException.class)
    public BaseResponse<?> runtimeExceptionHandler(RuntimeException e) {
        log.error("运行时异常：", e);
        return BaseResponse.error(ErrorCode.SYSTEM_ERROR.getCode(), "系统错误");
    }
} 