package com.xuecheng.content.model.dto;

import lombok.Data;

/**
 * @author 若倾
 * @version 1.0
 * @description TODO
 * @date 2023/1/30 22:15
 */
@Data
public class AddCourseTeacherDto {
    /**
     * 课程标识
     */
    private Long courseId;

    /**
     * 教师标识
     */
    private String teacherName;

    /**
     * 教师职位
     */
    private String position;

    /**
     * 教师简介
     */
    private String introduction;

    /**
     * 照片
     */
    private String photograph;
}
