package com.xuecheng.content.feignclient;

import com.xuecheng.content.config.MultipartSupportConfig;
import com.xuecheng.content.feignclient.model.CourseIndex;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author 若倾
 * @version 1.0
 * @description TODO
 * @date 2023/2/9 17:11
 */
//value取调用的服务名,配置multipart转化配置
@FeignClient(value = "search",fallbackFactory = SearchServiceClientFallbackFactory.class)
@RequestMapping("/search")
public interface SearchServiceClient {

    @ApiOperation("添加课程索引")
    @PostMapping("/index/course")
    public Boolean add(@RequestBody CourseIndex courseIndex);
}
