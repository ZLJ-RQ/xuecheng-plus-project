package com.xuecheng.content.api;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.content.model.dto.AddCourseTeacherDto;
import com.xuecheng.content.model.po.CourseTeacher;
import com.xuecheng.content.service.CourseTeacherService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author 若倾
 * @version 1.0
 * @description TODO
 * @date 2023/1/30 21:59
 */
@Slf4j
@RestController
public class CourseTeacherController {

    @Autowired
    CourseTeacherService courseTeacherService;

    @GetMapping("/courseTeacher/list/{courseId}")
    public List<CourseTeacher> list(@PathVariable Long courseId){
        LambdaQueryWrapper<CourseTeacher> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CourseTeacher::getCourseId,courseId);
        return courseTeacherService.list(queryWrapper);
    }

    @PostMapping("/courseTeacher")
    public CourseTeacher addOrUpdateCourseTeacher(@RequestBody CourseTeacher courseTeacher){
        Long companyId=22L;
        return courseTeacherService.addCourseTeacher(companyId,courseTeacher);
    }

    //delete /courseTeacher/course/75/26

    @DeleteMapping("/courseTeacher/course/{courseId}/{courseTeacherId}")
    public void deleteCourseTeacher(@PathVariable Long courseId,@PathVariable Long courseTeacherId){
        Long companyId=22L;
        courseTeacherService.deleteCourseTeacher(companyId,courseId,courseTeacherId);
    }
}
