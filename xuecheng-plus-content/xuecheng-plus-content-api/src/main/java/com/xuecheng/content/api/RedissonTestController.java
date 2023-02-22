package com.xuecheng.content.api;

import org.redisson.api.RQueue;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Queue;

/**
 * @author 若倾
 * @version 1.0
 * @description TODO
 * @date 2023/2/21 23:22
 */
@RestController
@RequestMapping("/redisson")
public class RedissonTestController {

    @Autowired
    RedissonClient redissonClient;

    /**
        入队
    */
    @GetMapping("/joinqueue")
    public Queue<String> joinqueue(String queuer) {
        RQueue<String> queue001 = redissonClient.getQueue("queue001");
        //添加value
        queue001.add(queuer);
        return queue001;
    }

    /**
     出队
     */
    @GetMapping("/removequeue")
    public String removequeue(){
        RQueue<String> queue001 = redissonClient.getQueue("queue001");
        //返回移除的value
        return queue001.poll();
    }
}
