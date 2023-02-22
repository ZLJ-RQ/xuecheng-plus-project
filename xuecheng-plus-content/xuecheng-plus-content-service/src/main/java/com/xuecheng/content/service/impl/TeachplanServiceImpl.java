package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.content.mapper.TeachplanMapper;
import com.xuecheng.content.mapper.TeachplanMediaMapper;
import com.xuecheng.content.model.dto.BindTeachplanMediaDto;
import com.xuecheng.content.model.dto.SaveTeachplanDto;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.model.po.Teachplan;
import com.xuecheng.content.model.po.TeachplanMedia;
import com.xuecheng.content.service.TeachplanMediaService;
import com.xuecheng.content.service.TeachplanService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
* @author 若倾
* @description 针对表【teachplan(课程计划)】的数据库操作Service实现
* @createDate 2023-01-24 14:24:39
*/
@Service
public class TeachplanServiceImpl extends ServiceImpl<TeachplanMapper, Teachplan>
    implements TeachplanService {

    @Autowired
    TeachplanMapper teachplanMapper;

    @Autowired
    TeachplanMediaMapper teachplanMediaMapper;

    @Override
    public List<TeachplanDto> findTeachplanTree(long courseId) {
        return teachplanMapper.selectTreeNodes(courseId);
    }

    @Override
    public void saveTeachplan(SaveTeachplanDto teachplanDto) {
        //获取课程计划id
        Long id = teachplanDto.getId();
        //查询数据库是否有这个课程计划
        Teachplan teachplan = teachplanMapper.selectById(id);
        if (teachplan==null){
            //新增课程计划
            teachplan=new Teachplan();
            BeanUtils.copyProperties(teachplanDto,teachplan);
            //前端没有给排序,自己计算
            int teachplanCount = getTeachplanCount(teachplanDto.getCourseId(), teachplanDto.getParentid());
            teachplan.setOrderby(teachplanCount+1);
            int insert = teachplanMapper.insert(teachplan);
            if (insert<=0)
                XueChengPlusException.cast("添加课程计划失败");
        }else{
            BeanUtils.copyProperties(teachplanDto,teachplan);
            int i = teachplanMapper.updateById(teachplan);
            if (i<=0)
                XueChengPlusException.cast("修改课程计划失败");
        }
    }

    @Transactional
    @Override
    public void deleteTeachplan(long id) {
        Teachplan teachplan = teachplanMapper.selectById(id);
        Long parentid = teachplan.getParentid();
        Integer orderby = teachplan.getOrderby();
        if (parentid==0){
            int count = getTeachplanCount(teachplan.getCourseId(), id);
            if (count!=0)
                XueChengPlusException.cast("课程计划信息还有子级信息，无法操作");
            teachplanMapper.deleteById(id);
        }else{
            int i = teachplanMapper.deleteById(id);
            //删除跟课程计划绑定的课程媒资
            LambdaQueryWrapper<TeachplanMedia> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(TeachplanMedia::getTeachplanId,id);
            TeachplanMedia teachplanMedia = teachplanMediaMapper.selectOne(queryWrapper);
            if (teachplanMedia!=null) {
                int i1 = teachplanMediaMapper.delete(queryWrapper);
                if (i1<=0)
                    XueChengPlusException.cast("删除课程媒资失败");
            }
            if (i<=0)
                XueChengPlusException.cast("删除课程计划失败");
        }
        reorderTeachPlan(parentid,orderby);
    }

    @Override
    public void moveTeachplan(String moveType,long id) {
        if (moveType.equals("movedown")){
            Teachplan teachplan = teachplanMapper.selectById(id);
            int count = getTeachplanCount(teachplan.getCourseId(), teachplan.getParentid());
            Integer orderby = teachplan.getOrderby();
            if (orderby==count){
                return;
            }else{
                UpdateWrapper<Teachplan> updateWrapper = new UpdateWrapper<>();
                updateWrapper.set("orderby",orderby);
                updateWrapper.eq("course_id",teachplan.getCourseId());
                updateWrapper.eq("parentid",teachplan.getParentid());
                updateWrapper.eq("orderby",orderby+1);
                this.update(updateWrapper);
                teachplan.setOrderby(orderby+1);
                teachplanMapper.updateById(teachplan);
            }
        }else{
            Teachplan teachplan = teachplanMapper.selectById(id);
            Integer orderby = teachplan.getOrderby();
            if (orderby==1){
                return;
            }else{
                UpdateWrapper<Teachplan> updateWrapper = new UpdateWrapper<>();
                updateWrapper.set("orderby",orderby);
                updateWrapper.eq("course_id",teachplan.getCourseId());
                updateWrapper.eq("parentid",teachplan.getParentid());
                updateWrapper.eq("orderby",orderby-1);
                this.update(updateWrapper);
                teachplan.setOrderby(orderby-1);
                teachplanMapper.updateById(teachplan);
            }
        }
    }

    @Override
    public TeachplanMedia associationMedia(BindTeachplanMediaDto bindTeachplanMediaDto) {
        //参数判断
        Long teachplanId = bindTeachplanMediaDto.getTeachplanId();
        //判断教学计划是否存在
        Teachplan teachplan = teachplanMapper.selectById(teachplanId);
        if (teachplan==null){
            XueChengPlusException.cast("教学计划不存在");
        }
        //只允许二级目录绑定视频
        Integer grade = teachplan.getGrade();
        if (grade!=2){
            XueChengPlusException.cast("只允许第二级教学计划绑定媒资文件");
        }
        //如果有绑定,先删除绑定
        LambdaQueryWrapper<TeachplanMedia> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TeachplanMedia::getTeachplanId,teachplanId);
        teachplanMediaMapper.delete(queryWrapper);
        //添加绑定
        TeachplanMedia teachplanMedia = new TeachplanMedia();
        teachplanMedia.setTeachplanId(teachplanId);
        teachplanMedia.setMediaId(bindTeachplanMediaDto.getMediaId());
        teachplanMedia.setMediaFilename(bindTeachplanMediaDto.getFileName());
        teachplanMedia.setCourseId(teachplan.getCourseId());
        teachplanMedia.setCreateDate(LocalDateTime.now());
        teachplanMediaMapper.insert(teachplanMedia);
        return teachplanMedia;
    }

    private int getTeachplanCount(long courseId,long parentId){
        LambdaQueryWrapper<Teachplan> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Teachplan::getCourseId,courseId);
        queryWrapper.eq(Teachplan::getParentid,parentId);
        Integer i = teachplanMapper.selectCount(queryWrapper);
        return i;
    }

    //删除之后重新对字段进行排序
    private void reorderTeachPlan(Long parentid,Integer orderby){
        LambdaQueryWrapper<Teachplan> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Teachplan::getParentid,parentid);
        queryWrapper.gt(Teachplan::getOrderby,orderby);
        List<Teachplan> list = this.list(queryWrapper);
        list.forEach(item->{
            item.setOrderby(item.getOrderby()-1);
        });
        updateBatchById(list);
    }
}




