package com.xuecheng.content.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.xuecheng.content.model.dto.CourseCategoryTreeDto;
import com.xuecheng.content.model.po.CourseCategory;

import java.util.List;

/**
* @author 若倾
* @description 针对表【course_category(课程分类)】的数据库操作Service
* @createDate 2023-01-25 15:04:40
*/
public interface CourseCategoryService extends IService<CourseCategory> {

    List<CourseCategoryTreeDto> selectTreeNodes(String id);


}
