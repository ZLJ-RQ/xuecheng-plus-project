package com.xuecheng.learning.feignclient;

import com.xuecheng.content.model.po.CoursePublish;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author 若倾
 * @version 1.0
 * @description TODO
 * @date 2023/2/18 10:47
 */
@FeignClient(value = "content-api",fallbackFactory = ContentServiceClientFallBackFactory.class)
@RequestMapping("/content")
public interface ContentServiceClient {

    @ResponseBody
    @GetMapping("/coursepublish/{courseId}")
    public CoursePublish getCoursepublish(@PathVariable("courseId") Long courseId);
}
