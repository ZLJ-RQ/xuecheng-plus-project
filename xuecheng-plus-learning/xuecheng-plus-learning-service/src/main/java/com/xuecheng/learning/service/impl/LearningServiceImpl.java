package com.xuecheng.learning.service.impl;

import com.xuecheng.base.model.RestResponse;
import com.xuecheng.base.utils.StringUtil;
import com.xuecheng.content.model.po.CoursePublish;
import com.xuecheng.learning.feignclient.ContentServiceClient;
import com.xuecheng.learning.feignclient.MediaServiceClient;
import com.xuecheng.learning.model.dto.XcCourseTablesDto;
import com.xuecheng.learning.service.LearningService;
import com.xuecheng.learning.service.MyCourseTablesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.print.attribute.standard.Media;

/**
 * @author 若倾
 * @version 1.0
 * @description TODO
 * @date 2023/2/20 23:33
 */
@Component
public class LearningServiceImpl implements LearningService {

    @Autowired
    ContentServiceClient contentServiceClient;

    @Autowired
    MediaServiceClient mediaServiceClient;

    @Autowired
    MyCourseTablesService myCourseTablesService;

    //获取媒资视频的地址
    @Override
    public RestResponse<String> getVideo(String userId, Long courseId, Long teachplanId, String mediaId) {
        //先判断用户是否登录
        if (StringUtil.isNotEmpty(userId)){
            //判断学习资格
            XcCourseTablesDto learning = myCourseTablesService.getLearningStatus(userId, courseId);
            String learnStatus = learning.getLearnStatus();
            //学习资格，[{"code":"702001","desc":"正常学习"},{"code":"702002","desc":"没有选课或选课后没有支付"},{"code":"702003","desc":"已过期需要申请续期或重新支付"}]
            if ("702001".equals(learnStatus)){
                return mediaServiceClient.getPlayUrlByMediaId(mediaId);
            }else if ("702003".equals(learnStatus)){
                RestResponse.validfail("已过期需要申请续期或重新支付");
            }
        }
        //用户未登录
        //如果免费,还是调用媒资
        CoursePublish coursepublish = contentServiceClient.getCoursepublish(courseId);
        String charge = coursepublish.getCharge();
        if (charge.equals("201000")){//免费,可以正常学习
            return mediaServiceClient.getPlayUrlByMediaId(mediaId);
        }
        //未支付的付费课程,登录和未登录都返回这个
        return RestResponse.validfail("请购买课程后继续学习");
    }
}
