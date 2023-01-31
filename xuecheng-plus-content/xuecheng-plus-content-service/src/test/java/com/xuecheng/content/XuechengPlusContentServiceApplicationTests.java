package com.xuecheng.content;

import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.model.dto.CourseCategoryTreeDto;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.service.CourseBaseService;
import com.xuecheng.content.service.CourseCategoryService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
class XuechengPlusContentServiceApplicationTests {

    @Autowired
    CourseBaseMapper courseBaseMapper;

    @Autowired
    CourseBaseService courseBaseService;

    @Autowired
    CourseCategoryService courseCategoryService;

    @Test
    void testCourseBaseMapper() {
        CourseBase courseBase = courseBaseMapper.selectById(22);
        Assertions.assertNotNull(courseBase);
    }

    @Test
    void testCourseBaseService() {
        PageParams params = new PageParams();
        PageResult<CourseBase> courseBase = courseBaseService.queryCourseBaseList(params, new  QueryCourseParamsDto());
        System.out.println(courseBase);
    }

    @Test
    void testCourseCategoryService() {
        List<CourseCategoryTreeDto> courseCategoryTreeDtos = courseCategoryService.selectTreeNodes("1");
        System.out.println(courseCategoryTreeDtos);
    }

}
