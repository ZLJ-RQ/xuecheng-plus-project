package com.xuecheng.auth.controller;

import com.xuecheng.ucenter.model.po.XcUser;
import com.xuecheng.ucenter.service.AuthService;
import com.xuecheng.ucenter.service.impl.WxAuthServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;

/**
 * @author 若倾
 * @version 1.0
 * @description 微信登录接口
 * @date 2023/2/14 20:18
 */
//需要重定向到网址所以使用controller
@Controller
public class WxLoginController {

    @Autowired
    WxAuthServiceImpl wxAuthService;

    @RequestMapping("/wxLogin")
    public String wxLogin(String code, String state) throws IOException {
        //在service中写一个用授权码去获取令牌,再用令牌获取登录人信息,再将信息存储到数据库
        XcUser xcUser = wxAuthService.wxAuth(code);
        //如果返回null,表示数据库没有,返回一个错误页面
        if (xcUser==null){
            return "redirect:http://www.xuecheng-plus.com/error.html";
        }
        String username = xcUser.getUsername();
        return "redirect:http://www.xuecheng-plus.com/sign.html?username="+username+"&authType=wx";
    }
}
