package com.xuecheng.ucenter.feignclient;

import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

/**
 * @author 若倾
 * @version 1.0
 * @description TODO
 * @date 2023/2/14 19:07
 */
@Slf4j
@Component
public class CheckCodeFallBackFactory implements FallbackFactory<CheckCodeClient> {
    @Override
    public CheckCodeClient create(Throwable throwable) {
        return new CheckCodeClient() {
            @Override
            public Boolean verify(String key, String code) {
                log.debug("远程验证码服务熔断异常：{}",throwable.getMessage());
                return null;
            }
        };
    }
}
