package com.xuecheng.content.service.jobhandler;

import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.content.service.CoursePublishService;
import com.xuecheng.messagesdk.model.po.MqMessage;
import com.xuecheng.messagesdk.service.MessageProcessAbstract;
import com.xuecheng.messagesdk.service.MqMessageService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;

@Slf4j
@Component
public class CoursePublishTask extends MessageProcessAbstract {

    @Autowired
    CoursePublishService coursePublishService;

    //课程发布消息类型
    public static final String MESSAGE_TYPE = "course_publish";

    //任务调度入口
    @XxlJob("CoursePublishJobHandler")
    public void coursePublishJobHandler() throws Exception {
        // 分片参数
        int shardIndex = XxlJobHelper.getShardIndex();
        int shardTotal = XxlJobHelper.getShardTotal();
        log.debug("shardIndex=" + shardIndex + ",shardTotal=" + shardTotal);
        //参数:分片序号、分片总数、消息类型、一次最多取到的任务数量、一次任务调度执行的超时时间
        process(shardIndex, shardTotal, MESSAGE_TYPE, 5, 60);
    }

    @Override
    public boolean execute(MqMessage mqMessage) {
        //获取消息相关的业务信息
        String businessKey1 = mqMessage.getBusinessKey1();
        long courseId = Integer.parseInt(businessKey1);
//        //课程静态化
        generateCourseHtml(mqMessage, courseId);
//        //课程缓存
//        saveCourseCache(mqMessage, courseId);
//        //课程索引
        saveCourseIndex(mqMessage, courseId);
        log.debug("开始执行课程发布任务");
        return true;
    }


    public void saveCourseIndex(MqMessage mqMessage,long courseId){
        log.debug("开始插入课程索引,课程id:{}",courseId);
        //先查询这个消息id
        Long id = mqMessage.getId();
        //因为父类注入了MqMessageService,所以从this中可以get到
        MqMessageService mqMessageService = this.getMqMessageService();
        //判断第一阶段是否完成了,完成了则return
        int stageTwo = mqMessageService.getStageTwo(id);
        if (stageTwo>0){
            log.debug("当前阶段是插入课程索引已经完成不再处理,任务信息:{}",mqMessage);
            return ;
        }
        //创建课程索引
        coursePublishService.saveCourseIndex(courseId);

        //保存第一阶段状态
        mqMessageService.completedStageTwo(id);
    }

    //课程静态化页面,上传到minio
    public void generateCourseHtml(MqMessage mqMessage,long courseId){
        log.debug("开始进行课程静态化,课程id:{}",courseId);
        //先查询这个消息id
        Long id = mqMessage.getId();
        //因为父类注入了MqMessageService,所以从this中可以get到
        MqMessageService mqMessageService = this.getMqMessageService();
        //判断第一阶段是否完成了,完成了则return
        int stageOne = mqMessageService.getStageOne(id);
        if (stageOne>0){
            log.debug("当前阶段是静态化课程信息任务已经完成不再处理,任务信息:{}",mqMessage);
            return ;
        }
        //调用生成静态文件的方法
        File file =coursePublishService.generateCourseHtml(courseId);
        if (file==null){
            XueChengPlusException.cast("课程静态化异常");
        }
        //调用上传到minio的方法
        coursePublishService.uploadCourseHtml(courseId,file);
        //保存第一阶段状态
        mqMessageService.completedStageOne(id);
    }
}
