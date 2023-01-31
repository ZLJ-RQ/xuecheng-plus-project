package com.xuecheng.content.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xuecheng.content.mapper.CourseCategoryMapper;
import com.xuecheng.content.model.dto.CourseCategoryTreeDto;
import com.xuecheng.content.model.po.CourseCategory;
import com.xuecheng.content.service.CourseCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author 若倾
 * @description 针对表【course_category(课程分类)】的数据库操作Service实现
 * @createDate 2023-01-25 15:04:40
 */
@Service
public class CourseCategoryServiceImpl extends ServiceImpl<CourseCategoryMapper, CourseCategory>
        implements CourseCategoryService {

    @Autowired
    CourseCategoryMapper courseCategoryMapper;

    @Override
    public List<CourseCategoryTreeDto> selectTreeNodes(String id) {
        //首先调用mapper中的方法,获取所有的节点数据
        List<CourseCategoryTreeDto> courseCategoryTreeDtos = courseCategoryMapper.selectTreeNodes(id);
        //创建一个新的List来接收提供给前端的格式
        List<CourseCategoryTreeDto> list=new ArrayList<>();
        //创建一个HashMap来快速通过id来找到节点
        HashMap<String,CourseCategoryTreeDto> nodeMap=new HashMap<>();
        //遍历所有的节点数据,进行数据处理
        courseCategoryTreeDtos.stream().forEach(item->{
            //将每个节点的id和对象都放入到nodeMap中
            nodeMap.put(item.getId(),item);
            //如果遍历出来的节点是id的直接子节点,则直接添加到list中
            if (item.getParentid().equals(id)){
                list.add(item);
            }
            //获取当前要遍历节点的父id
            String parentid = item.getParentid();
            //获取到当前节点的父节点
            CourseCategoryTreeDto parentNode = nodeMap.get(parentid);
            //简述这块内容就是,找到你爹,你爹在的话,就看看你爹有没有孩子列表,没有孩子列表就建个空列表,再把你放到孩子列表里
            if (parentNode!=null){
                List childrenTreeNodes = parentNode.getChildrenTreeNodes();
                if (childrenTreeNodes==null){
                    parentNode.setChildrenTreeNodes(new ArrayList<CourseCategoryTreeDto>());
                }
                parentNode.getChildrenTreeNodes().add(item);
            }
        });
        return list;
    }
}




