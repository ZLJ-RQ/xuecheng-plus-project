package com.xuecheng.ucenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.ucenter.feignclient.CheckCodeClient;
import com.xuecheng.ucenter.mapper.XcUserMapper;
import com.xuecheng.ucenter.model.dto.AuthParamsDto;
import com.xuecheng.ucenter.model.dto.XcUserExt;
import com.xuecheng.ucenter.model.po.XcUser;
import com.xuecheng.ucenter.service.AuthService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * @author 若倾
 * @version 1.0
 * @description 账号密码认证
 * @date 2023/2/13 22:36
 */
@Service("password_authservice")
public class PasswordAuthServiceImpl implements AuthService {

    @Autowired
    XcUserMapper xcUserMapper;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    CheckCodeClient checkCodeClient;

    //实现账号和密码登录
    @Override
    public XcUserExt execute(AuthParamsDto authParamsDto) {
//        //得到验证码
//        String checkcode = authParamsDto.getCheckcode();
//        String checkcodekey = authParamsDto.getCheckcodekey();
//        //判断验证码是否为空
//        if (StringUtils.isBlank(checkcode)||StringUtils.isBlank(checkcodekey)){
//            throw new RuntimeException("验证码为空");
//        }
//
//        //校验验证码
//        Boolean verify = checkCodeClient.verify(checkcodekey, checkcode);
//        if (verify==null||!verify){
//            throw new RuntimeException("验证码输入错误");
//        }

        //查询数据库
        String username = authParamsDto.getUsername();
        //通过账号拿到用户信息
        XcUser xcUser = xcUserMapper.selectOne(new LambdaQueryWrapper<XcUser>().eq(XcUser::getUsername, username));
        if (xcUser==null){
            //直接返回null,调用UserDetails这个方法的那个接口会处理,然后抛出异常
            throw  new RuntimeException("账号不存在");
        }
        //数据库中的加密密码
        String passwordDB = xcUser.getPassword();
        //输入的未加密密码
        String passwordInput = authParamsDto.getPassword();
        //比对密码
        boolean matches = passwordEncoder.matches(passwordInput, passwordDB);
        if (!matches){
            throw new RuntimeException("账号或密码错误");
        }
        XcUserExt xcUserExt = new XcUserExt();
        BeanUtils.copyProperties(xcUser,xcUserExt);
        return xcUserExt;
    }
}
