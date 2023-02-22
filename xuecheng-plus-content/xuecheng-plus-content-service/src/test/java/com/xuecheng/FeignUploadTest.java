package com.xuecheng;

import com.xuecheng.content.config.MultipartSupportConfig;
import com.xuecheng.content.feignclient.MediaServiceClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

/**
 * @author 若倾
 * @version 1.0
 * @description TODO
 * @date 2023/2/9 17:15
 */
@SpringBootTest
public class FeignUploadTest {

    //要生成代理对象,所以得在启动类上加上@EnableFeignClients去扫描feignclient具体包的位置
    @Autowired
    MediaServiceClient mediaServiceClient;

    @Test
    public void test(){
        File file = new File("D:\\Desktop\\学成在线\\资料\\test1.html");
        MultipartFile multipartFile = MultipartSupportConfig.getMultipartFile(file);
        String result = mediaServiceClient.upload(multipartFile, "course", "test1.html");
        System.out.println(result);
    }
}
