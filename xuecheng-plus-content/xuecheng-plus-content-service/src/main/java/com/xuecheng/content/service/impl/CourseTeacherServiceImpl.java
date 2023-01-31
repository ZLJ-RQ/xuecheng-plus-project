package com.xuecheng.content.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.mapper.CourseTeacherMapper;
import com.xuecheng.content.model.dto.AddCourseTeacherDto;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.model.po.CourseTeacher;
import com.xuecheng.content.service.CourseTeacherService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
* @author 若倾
* @description 针对表【course_teacher(课程-教师关系表)】的数据库操作Service实现
* @createDate 2023-01-30 21:09:13
*/
@Service
public class CourseTeacherServiceImpl extends ServiceImpl<CourseTeacherMapper, CourseTeacher>
    implements CourseTeacherService {

    @Autowired
    CourseTeacherMapper courseTeacherMapper;

    @Autowired
    CourseBaseMapper courseBaseMapper;

    @Override
    public CourseTeacher addCourseTeacher(Long companyId, CourseTeacher courseTeacher) {
        Long courseId = courseTeacher.getCourseId();
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        if (!courseBase.getCompanyId().equals(companyId)){
            XueChengPlusException.cast("只允许修改本机构的课程");
        }
        boolean b = this.saveOrUpdate(courseTeacher);
        if (!b&&courseTeacher.getId()==null){
            XueChengPlusException.cast("添加课程教师失败");
        }else if (!b){
            XueChengPlusException.cast("修改课程教师失败");
        }
        CourseTeacher teacher = new CourseTeacher();
        if (courseTeacher.getId()==null){
            LambdaQueryWrapper<CourseTeacher> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(CourseTeacher::getCourseId,courseTeacher.getCourseId());
            queryWrapper.eq(CourseTeacher::getTeacherName,courseTeacher.getTeacherName());
            teacher = courseTeacherMapper.selectOne(queryWrapper);
        }else{
            teacher = courseTeacherMapper.selectById(courseTeacher.getId());
        }
        return teacher;
    }

    @Override
    public void deleteCourseTeacher(Long companyId,Long courseId, Long courseTeacherId) {
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        if (!courseBase.getCompanyId().equals(companyId)){
            XueChengPlusException.cast("只允许修改本机构的课程");
        }
        courseTeacherMapper.deleteById(courseTeacherId);
    }



}




