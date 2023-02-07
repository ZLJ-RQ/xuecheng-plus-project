package com.xuecheng;

import com.alibaba.nacos.common.utils.IoUtils;
import io.minio.*;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilterInputStream;

/**
 * @author 若倾
 * @version 1.0
 * @description TODO
 * @date 2023/2/1 13:46
 */
public class TestMinIO {

    static MinioClient minioClient =
            MinioClient.builder()
                    .endpoint("http://192.168.101.65:9000")
                    .credentials("minioadmin", "minioadmin")
                    .build();

    @Test
    public void upload(){
        try {
            minioClient.uploadObject(
                    UploadObjectArgs.builder()
                            .bucket("testbucket")
                            .object("txt/新建文本文档.txt")
                            .filename("D:\\Desktop\\学成在线\\资料\\讲义\\新建文本文档.txt")
                            .build());
            System.out.println("上传成功了");
        }catch (Exception e){
            System.out.println("上传失败");
        }
    }

    @Test
    public void delete(){
        try {
            RemoveObjectArgs removeObjectArgs = RemoveObjectArgs.builder().bucket("testbucket").object("txt/新建文本文档.txt").build();
            minioClient.removeObject(removeObjectArgs);
        }catch (Exception e){

        }
    }

    @Test
    public void getFile(){
        GetObjectArgs getObjectArgs = GetObjectArgs.builder().bucket("testbucket").object("txt/新建文本文档.txt").build();
        //try里加括号,try执行完自动关闭资源
        try(
                FilterInputStream inputStream = minioClient.getObject(getObjectArgs);
                FileOutputStream outputStream = new FileOutputStream(new File("D:\\Desktop\\学成在线\\资料\\讲义\\1.txt"));
                ) {
            if (inputStream!=null){
                IoUtils.copy(inputStream,outputStream);
            }
        }catch (Exception e){

        }
    }
}
