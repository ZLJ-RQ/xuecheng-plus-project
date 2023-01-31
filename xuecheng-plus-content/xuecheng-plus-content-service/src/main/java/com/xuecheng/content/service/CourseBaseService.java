package com.xuecheng.content.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.model.dto.AddCourseDto;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.EditCourseDto;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.po.CourseBase;
import org.springframework.web.bind.annotation.RequestBody;

/**
* @author 若倾
* @description 针对表【course_base(课程基本信息)】的数据库操作Service
* @createDate 2023-01-24 14:23:57
*/
public interface CourseBaseService extends IService<CourseBase> {

    /**
     * @description 课程查询
     * @param params 分页参数
     * @param queryCourseParamsDto 查询条件
     * @return com.xuecheng.base.model.PageResult<com.xuecheng.content.model.po.CourseBase>
     * @author 若倾
     * @date 2023/1/24 16:35
    */
    PageResult<CourseBase> queryCourseBaseList(PageParams params, QueryCourseParamsDto queryCourseParamsDto);
    /**
     * @description 新增课程
     * @param companyId 所属机构id
     * @param addCourseDto 新增课程的信息
     * @return com.xuecheng.content.model.dto.CourseBaseInfoDto 课程信息包括基本信息,营销信息
     * @author 若倾
     * @date 2023/1/25 21:16
    */
    CourseBaseInfoDto createCourseBase(Long companyId, AddCourseDto addCourseDto);

    /***
     * @description 根据id查询课程基本信息
     * @param courseId 课程id
     * @return com.xuecheng.content.model.dto.CourseBaseInfoDto
     * @author 若倾
     * @date 2023/1/27 16:37
    */
    CourseBaseInfoDto getCourseBaseInfo(Long courseId);

    /**
     * @description 修改课程信息
     * @param companyId 机构id
     * @param dto 课程信息
     * @return com.xuecheng.content.model.dto.CourseBaseInfoDto
     * @author 若倾
     * @date 2023/1/27 16:37
     */
    CourseBaseInfoDto updateCourseBase(Long companyId, EditCourseDto dto);

    void deleteCourseInfo(Long companyId,Long courseId);
}
