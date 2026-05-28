package com.tingchenggis.tingcheng.exception;

/** 业务规则违反异常，返回 400 */
public class BusinessException extends RuntimeException {
    public BusinessException(String message) { super(message); }
    public BusinessException(String message, Throwable cause) { super(message, cause); }
}
