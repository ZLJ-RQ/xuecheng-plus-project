package com.xuecheng.base.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.List;

/**
 * @author 若倾
 * @version 1.0
 * @description TODO
 * @date 2023/1/27 12:27
 */
@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ResponseBody
    @ExceptionHandler(XueChengPlusException.class)
    @ResponseStatus//定义返回的code为500
    public RestErrorResponse  doXuechengPlusException(XueChengPlusException e){
        log.error("【系统异常】{}",e.getErrMessage());
        return new RestErrorResponse(e.getErrMessage());
    }

    @ResponseBody
    @ExceptionHandler(Exception.class)
    @ResponseStatus//定义返回的code为500
    public RestErrorResponse  doException(Exception e){
        log.error("【系统异常】{}",e.getMessage());
        return new RestErrorResponse(CommonError.UNKOWN_ERROR.getErrMessage());
    }

    @ResponseBody
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus//定义返回的code为500
    public RestErrorResponse  doMethodArgumentNotValidException(MethodArgumentNotValidException e){
        BindingResult bindingResult = e.getBindingResult();
        List<FieldError> fieldErrors = bindingResult.getFieldErrors();
        StringBuffer errors=new StringBuffer();
        fieldErrors.forEach(error->{
            errors.append(error.getDefaultMessage()).append(",");
        });
        log.error(errors.toString());
        return new RestErrorResponse(errors.toString());
    }
}
