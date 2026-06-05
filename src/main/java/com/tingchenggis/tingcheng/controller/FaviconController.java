
package com.tingchenggis.tingcheng.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Favicon 控制器
 * 
 * 处理浏览器自动请求的 /favicon.ico，避免 NoResourceFoundException 错误
 * 
 * @author TingChengGIS
 */
@Controller
public class FaviconController {

    /**
     * 返回空响应给浏览器
     * 避免 NoResourceFoundException: No static resource favicon.ico
     */
    @GetMapping("/favicon.ico")
    public ResponseEntity<Void> favicon() {
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
