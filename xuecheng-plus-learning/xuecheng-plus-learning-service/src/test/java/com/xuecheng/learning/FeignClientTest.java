package com.xuecheng.learning;

import com.xuecheng.content.model.po.CoursePublish;
import com.xuecheng.learning.feignclient.ContentServiceClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @description Feign接口测试类
 * @author Mr.M
 * @date 2022/10/24 17:15
 * @version 1.0
 */
 @SpringBootTest
public class FeignClientTest {

  @Autowired
  ContentServiceClient contentServiceClient;

  @Test
  public void testContentServiceClient(){
   CoursePublish coursepublish = contentServiceClient.getCoursepublish(2L);
   Assertions.assertNotNull(coursepublish);
  }
}
