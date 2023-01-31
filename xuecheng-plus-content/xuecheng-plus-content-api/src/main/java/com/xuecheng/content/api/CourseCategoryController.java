package com.xuecheng.content.api;

import com.xuecheng.content.model.dto.CourseCategoryTreeDto;
import com.xuecheng.content.service.CourseCategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author 若倾
 * @version 1.0
 * @description TODO
 * @date 2023/1/24 21:42
 */
@Slf4j
@RestController
public class CourseCategoryController {


    @Autowired
    CourseCategoryService courseCategoryService;

    @GetMapping("/course-category/tree-nodes")
    public List<CourseCategoryTreeDto> queryTreeNodes(){
        List<CourseCategoryTreeDto> courseCategoryTreeDtos = courseCategoryService.selectTreeNodes("1");
        return courseCategoryTreeDtos;
    }
}
