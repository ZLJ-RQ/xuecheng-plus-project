package com.xuecheng.orders.jobhandler;

import com.alibaba.fastjson.JSON;
import com.xuecheng.messagesdk.model.po.MqMessage;
import com.xuecheng.messagesdk.service.MessageProcessAbstract;
import com.xuecheng.messagesdk.service.MqMessageService;
import com.xuecheng.orders.config.PayNotifyConfig;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author 若倾
 * @version 1.0
 * @description TODO
 * @date 2023/2/20 20:23
 */
@Slf4j
@Component
public class PayNotifyTask extends MessageProcessAbstract {

    @Autowired
    MqMessageService mqMessageService;

    @Autowired
    RabbitTemplate rabbitTemplate;

    //任务调度入口
    @XxlJob("NotifyPayResultJobHandler")
    public void notifyPayResultJobHandler() throws Exception {
        // 分片参数
        int shardIndex = XxlJobHelper.getShardIndex();
        int shardTotal = XxlJobHelper.getShardTotal();
        log.debug("shardIndex="+shardIndex+",shardTotal="+shardTotal);
        //只查询支付通知的消息
        process(shardIndex,shardTotal, PayNotifyConfig.MESSAGE_TYPE,5,60);
    }


    @Override
    public boolean execute(MqMessage mqMessage) {
        log.debug("向消息队列发送支付结果通知消息:{}",mqMessage);
        //发送消息
        send(mqMessage);
        //由于消息表的记录需要等到订单服务收到回复后才能删除，这里返回false不让消息sdk自动删除
        return false;
    }

    //接收回复
    @RabbitListener(queues = PayNotifyConfig.PAYNOTIFY_REPLY_QUEUE)
    public void receive(String message) {
        log.info("收到支付结果通知回复:{}",message);
        MqMessage mqMessage = JSON.parseObject(message, MqMessage.class);
        mqMessageService.completed(mqMessage.getId());
    }

        /**
         * @description 发送支付结果通知
         * @param message  消息内容
         * @return void
         * @author Mr.M
         * @date 2022/9/20 9:43
         */
    private void send(MqMessage message) {
        //将消息对象转成消息字符串
        String messageJson = JSON.toJSONString(message);
        //发送消息给交换机,使用fanout交换机,采用广播模式,第一个参数是交换机,第二个参数是路由(用不到),第三个参数是消息
        rabbitTemplate.convertAndSend(PayNotifyConfig.PAYNOTIFY_EXCHANGE_FANOUT,"",messageJson);
        log.debug("向消息队列发送支付结果通知消息成功:{}",message);
    }
}
