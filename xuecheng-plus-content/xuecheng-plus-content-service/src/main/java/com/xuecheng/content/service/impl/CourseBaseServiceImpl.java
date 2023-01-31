package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.mapper.*;
import com.xuecheng.content.model.dto.AddCourseDto;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.EditCourseDto;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.po.*;
import com.xuecheng.content.service.CourseBaseService;
import com.xuecheng.content.service.CourseMarketService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
* @author 若倾
* @description 针对表【course_base(课程基本信息)】的数据库操作Service实现
* @createDate 2023-01-24 14:23:57
*/
@Service
public class CourseBaseServiceImpl extends ServiceImpl<CourseBaseMapper, CourseBase>
    implements CourseBaseService {

    @Autowired
    CourseBaseMapper courseBaseMapper;

    @Autowired
    CourseMarketMapper courseMarketMapper;

    @Autowired
    CourseCategoryMapper courseCategoryMapper;

    @Autowired
    CourseTeacherMapper courseTeacherMapper;

    @Autowired
    TeachplanMapper teachplanMapper;

    @Autowired
    TeachplanMediaMapper teachplanMediaMapper;

    @Autowired
    CourseMarketService courseMarketService;

    @Override
    public PageResult<CourseBase> queryCourseBaseList(PageParams params, QueryCourseParamsDto queryCourseParamsDto) {
        LambdaQueryWrapper<CourseBase> queryWrapper = new LambdaQueryWrapper<>();
        //添加查询条件
        //根据课程名查询
        queryWrapper.like(StringUtils.isNotEmpty(queryCourseParamsDto.getCourseName()),CourseBase::getName,queryCourseParamsDto.getCourseName());
        //根据课程审核状态
        queryWrapper.eq(StringUtils.isNotEmpty(queryCourseParamsDto.getAuditStatus()),CourseBase::getAuditStatus,queryCourseParamsDto.getAuditStatus());
        //根据课程发布状态
        queryWrapper.eq(StringUtils.isNotEmpty(queryCourseParamsDto.getPublishStatus()),CourseBase::getStatus,queryCourseParamsDto.getPublishStatus());
        //创建分页对象
        Page<CourseBase> page = new Page<>(params.getPageNo(),params.getPageSize());
        //分页查询结果(根据分页对象条件和查询条件)
        Page<CourseBase> pageResult = courseBaseMapper.selectPage(page, queryWrapper);
        //查询数据列表
        List<CourseBase> records = pageResult.getRecords();
        //总条数
        long total = pageResult.getTotal();
        //返回分页结果
        PageResult<CourseBase> courseBasePageResult = new PageResult<CourseBase>(records,total,params.getPageNo(),params.getPageSize());
        return courseBasePageResult;
    }

    @Transactional
    @Override
    public CourseBaseInfoDto createCourseBase(Long companyId, AddCourseDto dto) {

        //对参数合法性校验
//        if (StringUtils.isBlank(dto.getName())) {
//            throw new XueChengPlusException("课程名称为空");
//        }
//        if (StringUtils.isBlank(dto.getMt())) {
//            throw new XueChengPlusException("课程分类为空");
//        }
//        if (StringUtils.isBlank(dto.getSt())) {
//            throw new XueChengPlusException("课程分类为空");
//        }
//        if (StringUtils.isBlank(dto.getGrade())) {
//            throw new XueChengPlusException("课程等级为空");
//        }
//        if (StringUtils.isBlank(dto.getTeachmode())) {
//            throw new XueChengPlusException("教育模式为空");
//        }
//        if (StringUtils.isBlank(dto.getUsers())) {
//            throw new XueChengPlusException("适应人群");
//        }
//        if (StringUtils.isBlank(dto.getCharge())) {
//            throw new XueChengPlusException("收费规则为空");
//        }
        //对数据进行封装,调用mapper进行数据持久化
        CourseBase courseBase = new CourseBase();
        //将dto中和courseBase属性名一样的属性值拷贝到courseBase
        BeanUtils.copyProperties(dto,courseBase);
        //设置机构id
        courseBase.setCompanyId(companyId);
        //设置创建时间
        courseBase.setCreateDate(LocalDateTime.now());
        //设置审核状态为未提交
        courseBase.setAuditStatus("202002");
        //设置发布状态为未发布
        courseBase.setStatus("203001");
        //课程基本表插入一条记录
        int insert = courseBaseMapper.insert(courseBase);
        //获取课程id,两个表的id是相同的
        Long courseId = courseBase.getId();
        CourseMarket courseMarket = new CourseMarket();
        //将dto中和courseMarket属性名一样的属性值拷贝到courseMarket
        BeanUtils.copyProperties(dto,courseMarket);
        courseMarket.setId(courseId);
//        String charge = dto.getCharge();
//        if (charge.equals("201001")){//收费
//            Float price = dto.getPrice();
//            if (price==null||price.floatValue()<=0){
//                XueChengPlusException.cast("课程设置了收费价格不能为空且必须大于0");
//            }
//        }
//        //课程营销表插入一条记录
//        int insert1 = courseMarketMapper.insert(courseMarket);
        int insert1 = saveCourseMarket(courseMarket);
        if (insert<=0||insert1<=0){
            //抛出异常,spring才会检测到然后rollback
            XueChengPlusException.cast("添加课程失败");
        }
        //组装要返回的结果

        return getCourseBaseInfo(courseId);
    }

    /**
     * @description 根据课程id查询课程信息(基本信息和营销信息)
     * @param courseId 课程id
     * @return com.xuecheng.content.model.dto.CourseBaseInfoDto
     * @author 若倾
     * @date 2023/1/25 22:08
    */
    public CourseBaseInfoDto getCourseBaseInfo(Long courseId){
        CourseBase courseBase = courseBaseMapper.selectById(courseId);

        CourseMarket courseMarket = courseMarketMapper.selectById(courseId);

        CourseBaseInfoDto courseBaseInfoDto = new CourseBaseInfoDto();
        BeanUtils.copyProperties(courseBase,courseBaseInfoDto);
        BeanUtils.copyProperties(courseMarket,courseBaseInfoDto);

        CourseCategory mtCategory = courseCategoryMapper.selectById(courseBase.getMt());

        CourseCategory stCategory = courseCategoryMapper.selectById(courseBase.getSt());

        if (mtCategory!=null){
            courseBaseInfoDto.setMtName(mtCategory.getName());
        }

        if (stCategory!=null){
            courseBaseInfoDto.setStName(stCategory.getName());
        }
        return courseBaseInfoDto;
    }

    @Transactional
    @Override
    public CourseBaseInfoDto updateCourseBase(Long companyId, EditCourseDto dto) {
        Long courseId = dto.getId();
        CourseBase courseBase = courseBaseMapper.selectById(dto.getId());
        if (courseBase==null){
            XueChengPlusException.cast("课程不存在");
        }

        if (!courseBase.getCompanyId().equals(companyId)){
            XueChengPlusException.cast("只允许修改本机构的课程");
        }
        //封装课程基本信息表
        BeanUtils.copyProperties(dto,courseBase);
        courseBase.setChangeDate(LocalDateTime.now());
        int i = courseBaseMapper.updateById(courseBase);
        //封装课程营销表,但由于个别原因可能不存在,所以有则修改无则增加
        CourseMarket courseMarket = courseMarketMapper.selectById(courseId);
        if (courseMarket==null){
            courseMarket= new CourseMarket();
        }
            BeanUtils.copyProperties(dto,courseMarket);
            courseMarket.setId(courseId);
        int i1 = saveCourseMarket(courseMarket);
        if (i<=0||i1<=0)
            XueChengPlusException.cast("更新失败");
        return getCourseBaseInfo(courseId);
    }

    @Transactional
    @Override
    public void deleteCourseInfo(Long companyId,Long courseId) {
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        if (!courseBase.getCompanyId().equals(companyId)){
            XueChengPlusException.cast("只允许修改本机构的课程");
        }
        if (courseBase.getAuditStatus().equals("202002")){
            courseBaseMapper.deleteById(courseId);
            courseMarketMapper.deleteById(courseId);
            LambdaQueryWrapper<Teachplan> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Teachplan::getCourseId,courseId);
            teachplanMapper.delete(queryWrapper);
            LambdaQueryWrapper<TeachplanMedia> queryWrapper1 = new LambdaQueryWrapper<>();
            queryWrapper1.eq(TeachplanMedia::getCourseId,courseId);
            teachplanMediaMapper.delete(queryWrapper1);
            LambdaQueryWrapper<CourseTeacher> queryWrapper2 = new LambdaQueryWrapper<>();
            queryWrapper2.eq(CourseTeacher::getCourseId,courseId);
            courseTeacherMapper.delete(queryWrapper2);
        }
    }

    /**
     * @description 抽取课程营销校验及保存功能
     * @param courseMarket
     * @return int
     * @author 若倾
     * @date 2023/1/27 17:11
     */
    private int saveCourseMarket(CourseMarket courseMarket){
        String charge = courseMarket.getCharge();
        if(StringUtils.isBlank(charge)){
            XueChengPlusException.cast("请设置收费规则");
        }
        if(charge.equals("201001")){
            Float price = courseMarket.getPrice();
            if(price == null || price.floatValue()<=0){
                XueChengPlusException.cast("课程设置了收费价格不能为空且必须大于0");
            }
        }
        boolean b = courseMarketService.saveOrUpdate(courseMarket);
        return b?1:-1;
    }
}




