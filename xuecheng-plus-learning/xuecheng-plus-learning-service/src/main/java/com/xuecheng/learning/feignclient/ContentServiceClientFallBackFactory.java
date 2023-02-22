package com.xuecheng.learning.feignclient;

import com.xuecheng.content.model.po.CoursePublish;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


/**
 * @author 若倾
 * @version 1.0
 * @description TODO
 * @date 2023/2/18 10:49
 */
@Slf4j
@Component
public class ContentServiceClientFallBackFactory implements FallbackFactory<ContentServiceClient> {
    @Override
    public ContentServiceClient create(Throwable throwable) {
        return new ContentServiceClient() {
            @Override
            public CoursePublish getCoursepublish(Long courseId) {
                log.debug("远程调用内容管理服务熔断异常：{}",throwable.getMessage());
                return null;
            }
        };
    }
}
