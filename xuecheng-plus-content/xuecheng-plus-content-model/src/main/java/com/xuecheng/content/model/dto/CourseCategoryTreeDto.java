package com.xuecheng.content.model.dto;

import com.xuecheng.content.model.po.CourseCategory;
import lombok.Data;

import java.util.List;

/**
 * @author 若倾
 * @version 1.0
 * @description TODO
 * @date 2023/1/24 21:48
 */
@Data
public class CourseCategoryTreeDto extends CourseCategory {
    List childrenTreeNodes;
}
