package com.xuecheng.orders.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.utils.IdWorkerUtils;
import com.xuecheng.base.utils.JsonUtil;
import com.xuecheng.base.utils.QRCodeUtil;
import com.xuecheng.messagesdk.service.MqMessageService;
import com.xuecheng.orders.config.PayNotifyConfig;
import com.xuecheng.orders.mapper.XcOrdersGoodsMapper;
import com.xuecheng.orders.mapper.XcOrdersMapper;
import com.xuecheng.orders.mapper.XcPayRecordMapper;
import com.xuecheng.orders.model.dto.AddOrderDto;
import com.xuecheng.orders.model.dto.PayRecordDto;
import com.xuecheng.orders.model.dto.PayStatusDto;
import com.xuecheng.orders.model.po.XcOrders;
import com.xuecheng.orders.model.po.XcOrdersGoods;
import com.xuecheng.orders.model.po.XcPayRecord;
import com.xuecheng.orders.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.weaver.ast.Or;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author 若倾
 * @version 1.0
 * @description TODO
 * @date 2023/2/19 10:32
 */
@Slf4j
@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    XcOrdersMapper xcOrdersMapper;

    @Autowired
    XcOrdersGoodsMapper xcOrdersGoodsMapper;

    @Autowired
    XcPayRecordMapper xcPayRecordMapper;

    @Autowired
    MqMessageService mqMessageService;

    @Value("${pay.alipay.APP_ID}")
    String APP_ID;

    @Transactional
    @Override
    public PayRecordDto createOrder(String userId, AddOrderDto addOrderDto) {
        //添加商品订单
        XcOrders xcOrders = saveXcOrders(userId, addOrderDto);
        //添加支付交易记录
        XcPayRecord payRecord = createPayRecord(xcOrders);
        //生成二维码
        String qrCode = null;
        QRCodeUtil qrCodeUtil = new QRCodeUtil();
        try {
            qrCode=qrCodeUtil.createQRCode("http://192.168.101.1/api/orders/requestpay?payNo="+payRecord.getPayNo(),200,200);
        } catch (IOException e) {
            XueChengPlusException.cast("生成二维码出错");
        }
        PayRecordDto payRecordDto = new PayRecordDto();
        BeanUtils.copyProperties(payRecord,payRecordDto);
        payRecordDto.setQrcode(qrCode);
        return payRecordDto;
    }

    @Override
    public XcPayRecord getPayRecordByPayno(String payNo) {
        LambdaQueryWrapper<XcPayRecord> queryWrapper = new LambdaQueryWrapper<XcPayRecord>().eq(XcPayRecord::getPayNo, payNo);
        return xcPayRecordMapper.selectOne(queryWrapper);
    }

    @Override
    public void saveAliPayStatus(PayStatusDto payStatusDto) {
        //判断支付状态,更新支付记录表
        String trade_status = payStatusDto.getTrade_status();
        if (trade_status.equals("TRADE_SUCCESS")) {//支付成功
            String payNo = payStatusDto.getOut_trade_no();
            //根据商户订单号来查询订单记录
            XcPayRecord payRecord = getPayRecordByPayno(payNo);
            if (payRecord==null){
                log.info("收到支付结果通知查询不到支付记录,收到的信息{}",payStatusDto);
                return;
            }
            String status = payRecord.getStatus();
            if ("601002".equals(status)){
                log.info("收到支付结果通知,支付记录状态已支付成功");
                return;
            }
            String app_id_alipay = payStatusDto.getApp_id();
            //订单记录存在则判断交易金额是否一致
            Float totalPrice = payRecord.getTotalPrice()*100;
            Float total_amount =Float.parseFloat(payStatusDto.getTotal_amount())*100;//转成分
            if (!app_id_alipay.equals(APP_ID)||totalPrice.intValue()!=(total_amount.intValue())){
                log.error("收到支付结果通知,校验失败,支付宝的参数appid:{},交易金额:{},我们自己的参数:appid{},total_amount:{}",app_id_alipay,total_amount,APP_ID,totalPrice);
                return;
            }
            //更新订单表记录,更新第三方流水号,第三方支付类型,支付状态,支付成功时间
            XcPayRecord xcPayRecord = new XcPayRecord();
            xcPayRecord.setOutPayNo(payStatusDto.getTrade_no());
            xcPayRecord.setOutPayChannel("603002");
            xcPayRecord.setStatus("601002");
            xcPayRecord.setPaySuccessTime(LocalDateTime.now());
            int update = xcPayRecordMapper.update(xcPayRecord, new LambdaQueryWrapper<XcPayRecord>().eq(XcPayRecord::getPayNo, payNo));
            if (update > 0) {
                log.info("收到支付通知，更新支付交易状态成功.支付交易流水号:{},支付结果:{}", payNo, trade_status);
            } else {
                log.error("收到支付通知，更新支付交易状态失败.支付交易流水号:{},支付结果:{}", payNo, trade_status);
            }
            //根据订单id,拿到订单
            Long orderId = xcPayRecord.getOrderId();
            XcOrders orders = xcOrdersMapper.selectById(orderId);
            if (orders==null){
                log.error("收到支付通知，查询订单记录不存在,支付宝传来参数为:{},订单id为:{}", payStatusDto, orderId);
            }
            //更新订单状态
            XcOrders orders_u = new XcOrders();
            orders_u.setStatus("600002");
            int order_update = xcOrdersMapper.update(orders_u, new LambdaQueryWrapper<XcOrders>().eq(XcOrders::getId, orderId));
            if (order_update>0){
                log.info("收到支付通知，更新订单状态成功.付交易流水号:{},支付结果:{},订单号:{},状态:{}", payNo, trade_status, orderId, "600002");
                //业务id是选课id,在订单表中有绑定外部系统业务id
                String outBusinessId = orders.getOutBusinessId();
//                String messageType消息类型(支付结果通知),String businessKey1(业务id),String businessKey2(具体订单业务类型,课程还是学习资料),String businessKey3
                //通过messageType和订单业务类型来判断这个消息是不是我的
                mqMessageService.addMessage(PayNotifyConfig.MESSAGE_TYPE,outBusinessId,orders.getOrderType(),null);
            }else{
                log.error("收到支付通知，更新订单状态失败.支付交易流水号:{},支付结果:{},订单号:{},状态:{}", payNo, trade_status, orderId, "600001");
            }


        }


        //更新订单表
    }

    //创建支付交易记录
    public XcPayRecord createPayRecord(XcOrders orders){
        XcPayRecord payRecord = new XcPayRecord();
        //雪花算法,创建支付记录交易号
        long payNo  = IdWorkerUtils.getInstance().nextId();
        payRecord.setPayNo(payNo);
        payRecord.setOrderId(orders.getId());//商品订单号
        payRecord.setOrderName(orders.getOrderName());
        payRecord.setTotalPrice(orders.getTotalPrice());
        payRecord.setCurrency("CNY");
        payRecord.setCreateDate(LocalDateTime.now());
        payRecord.setStatus("601001");//未支付
        payRecord.setUserId(orders.getUserId());
        xcPayRecordMapper.insert(payRecord);
        return payRecord;

    }

    //添加商品订单和商铺明细信息
    @Transactional
    public XcOrders saveXcOrders(String userId, AddOrderDto addOrderDto) {
        //添加商品订单,要保证幂等性,不能重复添加商铺订单
        //拿到选课表id,根据选课表id,去判断是否重复下单
        String outBusinessId = addOrderDto.getOutBusinessId();
        XcOrders order = getOrderByBusinessId(outBusinessId);
        if (order!=null){
            return order;
        }
        //先添加商铺订单,从前端拿
        order=new XcOrders();
        //根据雪花算法,生成唯一的订单id
        long orderId  = IdWorkerUtils.getInstance().nextId();
        order.setId(orderId);
        order.setTotalPrice(addOrderDto.getTotalPrice());
        order.setCreateDate(LocalDateTime.now());
        order.setStatus("600001");//未支付
        order.setUserId(userId);
        order.setOrderType(addOrderDto.getOrderType());
        order.setOrderName(addOrderDto.getOrderName());
        order.setOrderDetail(addOrderDto.getOrderDetail());
        order.setOrderDescrip(addOrderDto.getOrderDescrip());
        order.setOutBusinessId(addOrderDto.getOutBusinessId());//选课记录id
        xcOrdersMapper.insert(order);
        //添加订单明细表
        String orderDetail = addOrderDto.getOrderDetail();
        //将dto里的json转成list
        List<XcOrdersGoods> xcOrdersGoods = JSON.parseArray(orderDetail, XcOrdersGoods.class);
        //将list中的每个数据,遍历插入
        xcOrdersGoods.forEach(orderGoods->{
            //将订单id添加到订单明细表记录
            orderGoods.setOrderId(orderId);
            xcOrdersGoodsMapper.insert(orderGoods);
        });
        return order;
    }

    //根据业务id查询订单
    public XcOrders getOrderByBusinessId(String businessId) {
        LambdaQueryWrapper<XcOrders> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(XcOrders::getOutBusinessId,businessId);
        XcOrders xcOrders = xcOrdersMapper.selectOne(queryWrapper);
        return xcOrders;
    }
}
