package com.winterchen.airportal.aop;


import com.winterchen.airportal.base.Result;
import com.winterchen.airportal.base.ResultCode;
import com.winterchen.airportal.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * @author winterchen
 * @version 1.0
 * @date 2021/4/27 9:41 上午
 * @description 统一异常处理
 **/
@Slf4j
@RestControllerAdvice
public class GlobalExceptionAspect {

    @ExceptionHandler(BusinessException.class)
    public Result<?> handleBusinessException(BusinessException ex, HttpServletRequest request) {
        log.error("throw business exception", ex);
        return Result.error(ex.getCode(), ex.getMessage());
    }

    @ExceptionHandler(value= {MethodArgumentNotValidException.class , BindException.class})
    public Result<?> handleVaildException(Exception e){
        log.error("request params error", e);
        BindingResult bindingResult = null;
        if (e instanceof MethodArgumentNotValidException) {
            bindingResult = ((MethodArgumentNotValidException)e).getBindingResult();
        } else if (e instanceof BindException) {
            bindingResult = ((BindException)e).getBindingResult();
        }
        Map<String,String> errorMap = new HashMap<>(16);
        bindingResult.getFieldErrors().forEach((fieldError)->
                errorMap.put(fieldError.getField(),fieldError.getDefaultMessage())
        );
        return Result.error(ResultCode.PARAM_IS_INVALID.getCode(), errorMap.toString());
    }


    @ExceptionHandler(Throwable.class)
    public Result<?> handleBaseException(Throwable e, HttpServletRequest request) {
        log.error("throw error", e);
        if (StringUtils.isNotBlank(e.getMessage())) {
            return Result.error(ResultCode.KNOWN_ERROR.getCode(), e.getMessage());
        }
        return Result.error(ResultCode.KNOWN_ERROR.getCode());
    }

}
