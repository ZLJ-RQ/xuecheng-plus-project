package com.xuecheng.content.api;

import com.xuecheng.content.model.dto.SaveTeachplanDto;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.service.TeachplanService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author 若倾
 * @version 1.0
 * @description TODO
 * @date 2023/1/27 19:32
 */
@Slf4j
@RestController
public class TeachplanController {

    @Autowired
    TeachplanService teachplanService;

    @GetMapping("/teachplan/{courseId}/tree-nodes")
    public List<TeachplanDto> getTreeNodes(@PathVariable Long courseId){
        return teachplanService.findTeachplayTree(courseId);
    }

    @PostMapping("/teachplan")
    public void saveTeachplan( @RequestBody SaveTeachplanDto teachplan){
        teachplanService.saveTeachplan(teachplan);
    }

    @DeleteMapping("/teachplan/{id}")
    public void deleteTeachplan(@PathVariable Long id){
        teachplanService.deleteTeachplan(id);
    }

    @PostMapping("/teachplan/{moveType}/{id}")
    public void moveTeachplan(@PathVariable String moveType,@PathVariable Long id){
        teachplanService.moveTeachplan(moveType,id);
    }
}
