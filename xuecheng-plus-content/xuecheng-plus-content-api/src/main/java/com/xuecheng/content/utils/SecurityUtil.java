package com.xuecheng.content.utils;

import com.alibaba.fastjson.JSON;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import springfox.documentation.spring.web.json.Json;

import java.io.Serializable;
import java.time.LocalDateTime;


/**
 * @author 若倾
 * @version 1.0
 * @description TODO
 * @date 2023/2/13 19:52
 */
@Slf4j
public class SecurityUtil {

    public static XcUser getUser() {
        //获取到存储的用户信息
        Object principalObj = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        //判断是否字符串
        if (principalObj instanceof String){
            //转成字符串
            String principal=principalObj.toString();
            //从json转为对象
            XcUser xcUser = null;
            try {
                xcUser = JSON.parseObject(principal, XcUser.class);
            } catch (Exception e) {
                log.debug("获取当前登录用户身份出错:{}", e.getMessage());
                e.printStackTrace();
            }
            return xcUser;
        }
        return null;
    }



    @Data
    public static class XcUser implements Serializable {

        private static final long serialVersionUID = 1L;

        private String id;

        private String username;

        private String password;

        private String salt;

        private String name;
        private String nickname;
        private String wxUnionid;
        private String companyId;
        /**
         * 头像
         */
        private String userpic;

        private String utype;

        private LocalDateTime birthday;

        private String sex;

        private String email;

        private String cellphone;

        private String qq;

        /**
         * 用户状态
         */
        private String status;

        private LocalDateTime createTime;

        private LocalDateTime updateTime;


    }

}
