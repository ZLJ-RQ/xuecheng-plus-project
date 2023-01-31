package com.xuecheng.content.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.xuecheng.content.model.dto.SaveTeachplanDto;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.model.po.Teachplan;

import java.util.List;

/**
* @author 若倾
* @description 针对表【teachplan(课程计划)】的数据库操作Service
* @createDate 2023-01-24 14:24:39
*/
public interface TeachplanService extends IService<Teachplan> {

     List<TeachplanDto> findTeachplayTree(long courseId);

     void saveTeachplan(SaveTeachplanDto teachplanDto);

     void deleteTeachplan(long id);

     void moveTeachplan(String moveType,long id);
}
