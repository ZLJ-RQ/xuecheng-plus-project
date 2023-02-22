package com.xuecheng.content.feignclient;

import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author 若倾
 * @version 1.0
 * @description TODO
 * @date 2023/2/9 21:46
 */
@Slf4j
@Component
public class MediaServiceClientFallbackFactory implements FallbackFactory<MediaServiceClient> {
    //这个方法要实现FallbackFactory,泛型中放feign调用接口
    @Override
    public MediaServiceClient create(Throwable throwable) {
        //返回接口对象,实现里面的方法,最后返回null,告诉上游服务,实施了降级处理
        return new MediaServiceClient() {
            @Override
            public String upload(MultipartFile filedata, String folder, String objectName) {
                log.debug("远程调用媒资管理服务熔断异常：{}",throwable.getMessage());
                return null;
            }
        };
    }
}
