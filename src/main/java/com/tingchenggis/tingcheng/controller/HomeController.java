package com.tingchenggis.tingcheng.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 * 主页控制器
 * 
 * 处理前端页面路由
 * 
 * @author TingChengGIS
 */
@Controller
public class HomeController {

    @GetMapping({"/", "/index", "/home"})
    public ModelAndView home() {
        return new ModelAndView("forward:/index.html");
    }

    @GetMapping("/share/route")
    public ModelAndView shareRoute() {
        return new ModelAndView("forward:/share.html");
    }
}