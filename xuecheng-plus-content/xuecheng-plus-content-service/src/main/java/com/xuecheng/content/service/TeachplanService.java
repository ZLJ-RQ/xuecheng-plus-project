package com.xuecheng.content.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.xuecheng.content.model.dto.BindTeachplanMediaDto;
import com.xuecheng.content.model.dto.SaveTeachplanDto;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.model.po.Teachplan;
import com.xuecheng.content.model.po.TeachplanMedia;

import java.util.List;

/**
* @author 若倾
* @description 针对表【teachplan(课程计划)】的数据库操作Service
* @createDate 2023-01-24 14:24:39
*/
public interface TeachplanService extends IService<Teachplan> {

     List<TeachplanDto> findTeachplanTree(long courseId);

     void saveTeachplan(SaveTeachplanDto teachplanDto);

     void deleteTeachplan(long id);

     void moveTeachplan(String moveType,long id);
     /**
      * @description 教学计划绑定媒资
      * @param bindTeachplanMediaDto
      * @return com.xuecheng.content.model.po.TeachplanMedia
      * @author 若倾
      * @date 2023/2/6 20:55
      */
     TeachplanMedia associationMedia(BindTeachplanMediaDto bindTeachplanMediaDto);
}
