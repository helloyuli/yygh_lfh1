package com.atguigu.yygh.msm.service;

import com.atguigu.yygh.vo.msm.MsmVo;
import org.springframework.stereotype.Service;

public interface MsmService {
    //发送邮箱验证码
    boolean send(String mail, String code);

    //利用消息队列发送
    boolean send(MsmVo msmVo);
}