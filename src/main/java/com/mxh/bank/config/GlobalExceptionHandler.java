package com.mxh.bank.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mxh.bank.exception.BusinessException;
import com.mxh.bank.model.response.ErrorResponse;
import com.mxh.bank.service.MessageService;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.Request;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.lang.reflect.Field;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    private static final String DEFAULT_ERROR_CODE = "BM-001";
    private static final String DEFAULT_ERROR_MESSAGE = "AM-001";

    @Autowired
    private MessageService messageService;

    // 处理转账参数错误（如自转账）
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException ex) {
        log.error("business error, ex = ", ex);
        String message = messageService.getMessage(ex.getErrorCode(), ex.getArgs());
        ErrorResponse errorResponse = new ErrorResponse(ex.getErrorCode(), message);
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        log.error("parameter verification error, ex = ", ex);
        BindingResult bindingResult = ex.getBindingResult();
        // 获取字段校验错误信息
        Class<?> targetClass = ex.getParameter().getParameterType();
        for (FieldError fieldError : bindingResult.getFieldErrors()) {
            String message = messageService.getMessage(fieldError.getDefaultMessage(), getJsonFieldName(fieldError, targetClass));
            ErrorResponse errorResponse = new ErrorResponse(fieldError.getDefaultMessage(), message);
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(new ErrorResponse(DEFAULT_ERROR_CODE, messageService.getMessage(DEFAULT_ERROR_CODE)), HttpStatus.INTERNAL_SERVER_ERROR);
    }


    // 处理转账过程中其他异常（如数据库异常）
    @ExceptionHandler({Exception.class})
    public ResponseEntity<ErrorResponse> handleException(Exception ex) {
        log.error("inner error, ex = ", ex);
        String message = messageService.getMessage(DEFAULT_ERROR_CODE);
        return new ResponseEntity<>(new ErrorResponse(DEFAULT_ERROR_CODE, message), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public static String getJsonFieldName(FieldError fieldError, Class<?> targetClass) {
        String javaFieldName = fieldError.getField();
        try {
            Field field = targetClass.getDeclaredField(javaFieldName);

            if (field.isAnnotationPresent(JsonProperty.class)) {
                JsonProperty jsonProperty = field.getAnnotation(JsonProperty.class);
                return jsonProperty.value();
            }
        } catch (NoSuchFieldException e) {
            return fieldError.getField();
        }
        return javaFieldName;
    }
}