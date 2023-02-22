package com.xuecheng.learning.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.learning.config.PayNotifyConfig;
import com.xuecheng.learning.mapper.XcChooseCourseMapper;
import com.xuecheng.learning.model.po.XcChooseCourse;
import com.xuecheng.learning.service.MyCourseTablesService;
import com.xuecheng.messagesdk.model.po.MqMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author 若倾
 * @version 1.0
 * @description TODO
 * @date 2023/2/20 20:46
 */
@Slf4j
@Component
public class ReceivePayNotifyService {

    @Autowired
    XcChooseCourseMapper xcChooseCourseMapper;

    @Autowired
    MyCourseTablesService myCourseTablesService;

    @Autowired
    RabbitTemplate rabbitTemplate;


    @RabbitListener(queues = {PayNotifyConfig.PAYNOTIFY_QUEUE})
    public void receive(String message) {
        MqMessage mqMessage = JSON.parseObject(message, MqMessage.class);
        //消息类型
        String messageType = mqMessage.getMessageType();
        //订单类型,60201表示购买课程
        String businessKey2 = mqMessage.getBusinessKey2();
        //这里只处理支付结果通知并且购买课程的消息
        if (messageType.equals(PayNotifyConfig.MESSAGE_TYPE) && "60201".equals(businessKey2)) {
            //选课id
            String businessKey1 = mqMessage.getBusinessKey1();
            //查询选课信息
            XcChooseCourse chooseCourse = xcChooseCourseMapper.selectById(businessKey1);
            if (chooseCourse==null){
                log.info("收到支付结果通知,查询不到选课记录,businessKey1:{}",businessKey1);
                return;
            }
            XcChooseCourse xcChooseCourse = new XcChooseCourse();
            xcChooseCourse.setStatus("701001");//选课成功
            xcChooseCourseMapper.update(xcChooseCourse,new LambdaQueryWrapper<XcChooseCourse>().eq(XcChooseCourse::getId,businessKey1));
            //向我的课程表添加记录
            XcChooseCourse course = xcChooseCourseMapper.selectById(businessKey1);
            myCourseTablesService.addCourseTables(course);
            //向订单消息回复
            send(mqMessage);
        }
    }

    /**
     * @description 回复消息
     * @param message  回复消息
     * @return void
     * @author Mr.M
     * @date 2022/9/20 9:43
     */
    public void send(MqMessage message) {
        //转json发送消息
        String jsonString = JSON.toJSONString(message);
        rabbitTemplate.convertAndSend(PayNotifyConfig.PAYNOTIFY_REPLY_QUEUE,jsonString);
        log.info("学习中心服务向订单服务回复消息:{}",message);
    }
    }
