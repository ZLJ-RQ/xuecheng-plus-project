package com.xuecheng.content.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xuecheng.content.model.dto.CourseCategoryTreeDto;
import com.xuecheng.content.model.po.CourseCategory;

import java.util.List;

/**
* @author 若倾
* @description 针对表【course_category(课程分类)】的数据库操作Mapper
* @createDate 2023-01-25 15:04:40
* @Entity generator.domain.CourseCategory
*/
public interface CourseCategoryMapper extends BaseMapper<CourseCategory> {

    List<CourseCategoryTreeDto> selectTreeNodes(String id);
}




