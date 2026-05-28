package com.tingchenggis.tingcheng.exception;

/** 资源不存在，返回 404 */
public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) { super(message); }
}
