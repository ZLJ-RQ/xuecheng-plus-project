package com.xuecheng.content.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.model.po.Teachplan;

import java.util.List;

/**
* @author 若倾
* @description 针对表【teachplan(课程计划)】的数据库操作Mapper
* @createDate 2023-01-24 14:24:39
* @Entity generator.domain.Teachplan
*/
public interface TeachplanMapper extends BaseMapper<Teachplan> {

    List<TeachplanDto> selectTreeNodes(Long courseId);
}




