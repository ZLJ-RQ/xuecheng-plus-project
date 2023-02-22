package com.xuecheng.content.feignclient;

import org.springframework.web.multipart.MultipartFile;

/**
 * @author 若倾
 * @version 1.0
 * @description TODO
 * @date 2023/2/9 21:45
 */
public class MediaServiceClientFallback implements MediaServiceClient{

    //实现feign调用的方法,变成本地调用返回结果,但这种方式不能返回异常
    @Override
    public String upload(MultipartFile filedata, String folder, String objectName) {
        return null;
    }
}
