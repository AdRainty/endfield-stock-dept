package com.adrainty.stock.exception;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

/**
 * 全局异常处理器
 * 
 * @author adrainty
 * @since 2026-02-26
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    /**
     * 统一返回结果
     */
    @Data
    public static class ApiResult<T> {
        private int code;
        private String message;
        private T data;
        private LocalDateTime timestamp;
        
        public static <T> ApiResult<T> error(int code, String message) {
            ApiResult<T> result = new ApiResult<>();
            result.setCode(code);
            result.setMessage(message);
            result.setTimestamp(LocalDateTime.now());
            return result;
        }
        
        public static <T> ApiResult<T> success(T data) {
            ApiResult<T> result = new ApiResult<>();
            result.setCode(0);
            result.setMessage("success");
            result.setData(data);
            result.setTimestamp(LocalDateTime.now());
            return result;
        }
    }
    
    /**
     * 处理未登录异常
     */
    @ExceptionHandler(NotLoginException.class)
    public ResponseEntity<ApiResult<Void>> handleNotLoginException(NotLoginException e) {
        log.warn("用户未登录：{}", e.getMessage());
        ApiResult<Void> result = ApiResult.error(401, "用户未登录或登录已过期");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result);
    }
    
    /**
     * 处理无权限异常
     */
    @ExceptionHandler(NotPermissionException.class)
    public ResponseEntity<ApiResult<Void>> handleNotPermissionException(NotPermissionException e) {
        log.warn("用户无权限：{}", e.getMessage());
        ApiResult<Void> result = ApiResult.error(403, "用户无权限访问该资源");
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(result);
    }
    
    /**
     * 处理参数校验异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResult<Void>> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .findFirst()
            .orElse("参数校验失败");
        log.warn("参数校验失败：{}", message);
        ApiResult<Void> result = ApiResult.error(400, message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
    }
    
    /**
     * 处理绑定异常
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiResult<Void>> handleBindException(BindException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .findFirst()
            .orElse("参数绑定失败");
        log.warn("参数绑定失败：{}", message);
        ApiResult<Void> result = ApiResult.error(400, message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
    }
    
    /**
     * 处理业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResult<Void>> handleBusinessException(BusinessException e) {
        log.warn("业务异常：{}", e.getMessage());
        ApiResult<Void> result = ApiResult.error(e.getCode(), e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
    }
    
    /**
     * 处理其他异常
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResult<Void>> handleException(Exception e) {
        log.error("系统异常：", e);
        ApiResult<Void> result = ApiResult.error(500, "系统内部错误：" + e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
    }
}
