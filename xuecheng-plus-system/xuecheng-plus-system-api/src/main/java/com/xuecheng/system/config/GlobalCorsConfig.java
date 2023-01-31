package com.xuecheng.system.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * @author 若倾
 * @version 1.0
 * @description TODO
 * @date 2023/1/24 20:56
 */
@Configuration
public class GlobalCorsConfig {

    @Bean
    public CorsFilter corsFilter(){
        CorsConfiguration configuration = new CorsConfiguration();
        //允许域名进行跨域调用的白名单
        configuration.addAllowedOrigin("*");
        //放行全部请求头信息
        configuration.addAllowedHeader("*");
        //允许所有请求方法跨域调用
        configuration.addAllowedMethod("*");
        //允许跨域发送cookie
        configuration.setAllowCredentials(true);
        //按路径进行拦截
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        //第一个参数指定拦截的地址,第二个参数是配置
        source.registerCorsConfiguration("/**",configuration);
        return new CorsFilter(source);
    }
}
