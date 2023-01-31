package com.xuecheng.content.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.xuecheng.content.model.dto.AddCourseTeacherDto;
import com.xuecheng.content.model.po.CourseTeacher;
import org.springframework.web.bind.annotation.PathVariable;

/**
* @author 若倾
* @description 针对表【course_teacher(课程-教师关系表)】的数据库操作Service
* @createDate 2023-01-30 21:09:13
*/
public interface CourseTeacherService extends IService<CourseTeacher> {

    CourseTeacher addCourseTeacher(Long companyId,CourseTeacher courseTeacher);

    void deleteCourseTeacher(Long companyId,Long courseId, Long courseTeacherId);
}
