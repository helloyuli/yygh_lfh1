package com.atguigu.yygh.msm.service.impl;


import com.atguigu.yygh.msm.service.MsmService;
import com.atguigu.yygh.vo.msm.MsmVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Properties;

@Service
public class MsmServiceImpl implements MsmService {

    private static String from = "blueyuli@163.com";// 发件人的邮箱地址
    private static String user = "blueyuli@163.com";// 发件人称号，同邮箱地址
    private static String password = "ZVHEVRMBGTMAKFST";// 发件人邮箱的授权码
    private static String name = "医点通平台";

    @Override
    public boolean send(String mail, String code) {
        //判断邮箱是否为空
        if(StringUtils.isEmpty(mail)) {
            return false;
        }
        //判断验证码是否为空
        if(StringUtils.isEmpty(code)) {
            return false;
        }
        Date date = new Date();

        boolean flag = sendMail(mail, makeContent(name,code), "【医点通】电子邮箱验证码");
        return flag;
    }

    @Override
    public boolean send(MsmVo msmVo) {
        if(!StringUtils.isEmpty(msmVo.getMail())) {
            if ("预约下单".equals(msmVo.getTemplateCode())){
                return sendMail(msmVo.getMail(),makeContent(msmVo.getParam(),name),"【"+msmVo.getParam().get("hosname")+"】：待支付提示");
            }else if ("取消订单".equals(msmVo.getTemplateCode())){
                return sendMail(msmVo.getMail(),makeContent(name),"【"+msmVo.getParam().get("hosname")+"】：取消预约成功");
            }else if ("支付成功".equals(msmVo.getTemplateCode())){
                return sendMail(msmVo.getMail(),makeContent(name,msmVo.getParam()),"【"+msmVo.getParam().get("hosname")+"】：预约挂号成功");
            }else if ("退款成功".equals(msmVo.getTemplateCode())){
                return sendMail(msmVo.getMail(),makeContent(name,msmVo.getParam(),1),"【"+msmVo.getParam().get("hosname")+"】：退款申请已收到");
            }else if ("就诊提醒".equals(msmVo.getTemplateCode())){
                return sendMail(msmVo.getMail(),makeContent(name,msmVo.getParam(),""),"【"+msmVo.getParam().get("hosname")+"】：就诊提醒");
            }
        }
        return false;
    }

    /**
     * 发送邮件
     * @param to  目标邮箱
     * @param text  发送内容
     * @param title  邮件的名称
     * @return
     */
    public static boolean sendMail(String to, String text, String title) {
        Properties props = new Properties();
        props.setProperty("mail.smtp.host", "smtp.163.com"); // 设置发送邮件的邮件服务器的属性（这里使用网易的smtp服务器）
        props.put("mail.smtp.host", "smtp.163.com"); // 需要经过授权，也就是有户名和密码的校验，这样才能通过验证（一定要有这一条）
        props.put("mail.smtp.auth", "true"); // 用刚刚设置好的props对象构建一个session
        Session session = Session.getDefaultInstance(props); // 有了这句便可以在发送邮件的过程中在console处显示过程信息，供调试使
        // 用（你可以在控制台（console)上看到发送邮件的过程）
        session.setDebug(true); // 用session为参数定义消息对象
        MimeMessage message = new MimeMessage(session); // 加载发件人地址
        try {
            message.setFrom(new InternetAddress(from));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(to)); // 加载收件人地址
            message.setSubject(title); // 加载标题
            Multipart multipart = new MimeMultipart(); // 向multipart对象中添加邮件的各个部分内容，包括文本内容和附件
            BodyPart contentPart = new MimeBodyPart(); // 设置邮件的文本内容
            contentPart.setContent(text, "text/html;charset=utf-8");
            multipart.addBodyPart(contentPart);
            message.setContent(multipart);
            message.saveChanges(); // 保存变化
            Transport transport = session.getTransport("smtp"); // 连接服务器的邮箱
            transport.connect("smtp.163.com", user, password); // 把邮件发送出去
            transport.sendMessage(message, message.getAllRecipients());
            transport.close();
        } catch (MessagingException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
    //就诊提醒
    public String makeContent(String name,Map<String, Object> params,String m){
        //获取当前时间
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String date = simpleDateFormat.format(new Date());

        String content = "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "<title></title>\n" +
                "<style>\n" +
                ".qmbox {\n" +
                "\tpadding: 0;\n" +
                "}\n" +
                ".qm_con_body_content {\n" +
                "\theight: auto;\n" +
                "\tmin-height: 100px;\n" +
                "\t_height: 100px;\n" +
                "\tword-wrap: break-word;\n" +
                "\tfont-size: 14px;\n" +
                "\tfont-family: \"lucida Grande\", Verdana, \"Microsoft YaHei\";\n" +
                "}\n" +
                ".body {\n" +
                "\tline-height: 170%;\n" +
                "}\n" +
                "BODY {\n" +
                "\tfont-family: \"lucida Grande\", Verdana, \"Microsoft YaHei\";\n" +
                "\tfont-size: 12px;\n" +
                "\t-webkit-font-smoothing: subpixel-antialiased;\n" +
                "}\n" +
                "BODY {\n" +
                "\tmargin: 0;\n" +
                "\tpadding: 0;\n" +
                "}\n" +
                "BODY {\n" +
                "\tbackground-color: #fff;\n" +
                "\tfont-size: 12px;\n" +
                "}\n" +
                "BODY {\n" +
                "\tbackground: #fff;\n" +
                "}\n" +
                "BODY {\n" +
                "\tbackground: #fff;\n" +
                "\tcolor: #000;\n" +
                "\tfont-weight: normal;\n" +
                "\tfont-family: \"lucida Grande\", Verdana, \"Microsoft YaHei\";\n" +
                "\tpadding: 0 7px 6px 4px;\n" +
                "\tmargin: 0;\n" +
                "}\n" +
                "HTML {\n" +
                "\ttop: 0px;\n" +
                "}\n" +
                ".body P {\n" +
                "\tline-height: 170%;\n" +
                "}\n" +
                "</style>\n" +
                "</head>\n" +
                "\n" +
                "<BODY mu=\"mu\" md=\"md\" module=\"qmReadMail\" context=\"ZC1912-rQ7uXSy7P7HThxdLFCOuY92\"><DIV class=mailcontainer id=qqmail_mailcontainer>\n" +
                "<DIV id=mainmail style=\"MARGIN-BOTTOM: 12px; POSITION: relative; Z-INDEX: 1\">\n" +
                "<DIV class=body id=contentDiv style=\"FONT-SIZE: 14px; HEIGHT: auto; POSITION: relative; ZOOM: 1; PADDING-BOTTOM: 10px; PADDING-TOP: 15px; PADDING-LEFT: 15px; Z-INDEX: 1; LINE-HEIGHT: 1.7; PADDING-RIGHT: 15px\" onmouseover=getTop().stopPropagation(event); onclick=\"getTop().preSwapLink(event, 'html', 'ZC1912-rQ7uXSy7P7HThxdLFCOuY92');\">\n" +
                "<DIV id=qm_con_body>\n" +
                "<DIV class=\"qmbox qm_con_body_content qqmail_webmail_only\" id=mailContentContainer>\n" +
                "<DIV class=main style=\"OVERFLOW: hidden; WIDTH: 100%; BACKGROUND-COLOR: #f7f7f7\">\n" +
                "<DIV class=content style=\"BORDER-TOP: #cccccc 1px solid; BORDER-RIGHT: #cccccc 1px solid; BACKGROUND: #ffffff; BORDER-BOTTOM: #cccccc 1px solid; PADDING-BOTTOM: 10px; PADDING-TOP: 10px; PADDING-LEFT: 25px; BORDER-LEFT: #cccccc 1px solid; MARGIN: 50px; PADDING-RIGHT: 25px\">\n" +
                "<DIV class=header style=\"MARGIN-BOTTOM: 30px\">\n" +
                "<P>"+params.get("name")+"：</P></DIV>\n" +
                "<P>您好！您预约的【"+params.get("title")+"】"+params.get("reserveDate")+"的号源，请及时就诊</P>\n" +
                "<P><SPAN style=\"FONT-SIZE: 16px; FONT-WEIGHT: bold; COLOR: #f90\">注意事项</SPAN></P>\n" +
                "<P>1、请确认就诊人信息是否准确，若填写错误将无法取号就诊，损失由本人承担；<br/>" +
                "<span style=\"color: red\"\n" +
                "              >2、【取号】就诊当天需在\n" +
                params.get("fetchTime")+"\n" +
                "              在医院取号，未取号视为爽约，该号不退不换；</span\n" +
                "            ><br />\n" +
                "            3、【退号】在"+params.get("quitTime")+"前可在线退号\n" +
                "            ，逾期将不可办理退号退费；<br />\n" +
                "            4、医点通在线预约挂号平台支持自费患者使用身份证预约，同时支持医保患者使用社保卡在平台预约挂号。请于就诊当日，携带预约挂号所使用的有效身份证件到院取号；<br />\n" +
                "            5、请注意医保患者在住院期间不能使用社保卡在门诊取号。</P>\n" +

                "<DIV class=footer style=\"MARGIN-TOP: 30px\">\n" +
                "<P>"+name+"</P>\n" +
                "<P><SPAN style=\"BORDER-BOTTOM: #ccc 1px dashed; POSITION: relative; _display: inline-block\" t=\"5\" times=\"\" isout=\"0\">"+date+"</SPAN></P></DIV>\n" +
                "<DIV class=tip style=\"COLOR: #cccccc; TEXT-ALIGN: center\">该邮件为系统自动发送，请勿进行回复 </DIV></DIV></DIV></DIV></DIV></DIV></DIV></DIV></BODY>\n" +
                "</html>\n";

        return content;
    }
    //退款成功制作发送内容
    private String makeContent(String name,Map<String, Object> params,Integer i){
        //获取当前时间
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String date = simpleDateFormat.format(new Date());

        String content = "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "<title></title>\n" +
                "<style>\n" +
                ".qmbox {\n" +
                "\tpadding: 0;\n" +
                "}\n" +
                ".qm_con_body_content {\n" +
                "\theight: auto;\n" +
                "\tmin-height: 100px;\n" +
                "\t_height: 100px;\n" +
                "\tword-wrap: break-word;\n" +
                "\tfont-size: 14px;\n" +
                "\tfont-family: \"lucida Grande\", Verdana, \"Microsoft YaHei\";\n" +
                "}\n" +
                ".body {\n" +
                "\tline-height: 170%;\n" +
                "}\n" +
                "BODY {\n" +
                "\tfont-family: \"lucida Grande\", Verdana, \"Microsoft YaHei\";\n" +
                "\tfont-size: 12px;\n" +
                "\t-webkit-font-smoothing: subpixel-antialiased;\n" +
                "}\n" +
                "BODY {\n" +
                "\tmargin: 0;\n" +
                "\tpadding: 0;\n" +
                "}\n" +
                "BODY {\n" +
                "\tbackground-color: #fff;\n" +
                "\tfont-size: 12px;\n" +
                "}\n" +
                "BODY {\n" +
                "\tbackground: #fff;\n" +
                "}\n" +
                "BODY {\n" +
                "\tbackground: #fff;\n" +
                "\tcolor: #000;\n" +
                "\tfont-weight: normal;\n" +
                "\tfont-family: \"lucida Grande\", Verdana, \"Microsoft YaHei\";\n" +
                "\tpadding: 0 7px 6px 4px;\n" +
                "\tmargin: 0;\n" +
                "}\n" +
                "HTML {\n" +
                "\ttop: 0px;\n" +
                "}\n" +
                ".body P {\n" +
                "\tline-height: 170%;\n" +
                "}\n" +
                "</style>\n" +
                "</head>\n" +
                "\n" +
                "<BODY mu=\"mu\" md=\"md\" module=\"qmReadMail\" context=\"ZC1912-rQ7uXSy7P7HThxdLFCOuY92\"><DIV class=mailcontainer id=qqmail_mailcontainer>\n" +
                "<DIV id=mainmail style=\"MARGIN-BOTTOM: 12px; POSITION: relative; Z-INDEX: 1\">\n" +
                "<DIV class=body id=contentDiv style=\"FONT-SIZE: 14px; HEIGHT: auto; POSITION: relative; ZOOM: 1; PADDING-BOTTOM: 10px; PADDING-TOP: 15px; PADDING-LEFT: 15px; Z-INDEX: 1; LINE-HEIGHT: 1.7; PADDING-RIGHT: 15px\" onmouseover=getTop().stopPropagation(event); onclick=\"getTop().preSwapLink(event, 'html', 'ZC1912-rQ7uXSy7P7HThxdLFCOuY92');\">\n" +
                "<DIV id=qm_con_body>\n" +
                "<DIV class=\"qmbox qm_con_body_content qqmail_webmail_only\" id=mailContentContainer>\n" +
                "<DIV class=main style=\"OVERFLOW: hidden; WIDTH: 100%; BACKGROUND-COLOR: #f7f7f7\">\n" +
                "<DIV class=content style=\"BORDER-TOP: #cccccc 1px solid; BORDER-RIGHT: #cccccc 1px solid; BACKGROUND: #ffffff; BORDER-BOTTOM: #cccccc 1px solid; PADDING-BOTTOM: 10px; PADDING-TOP: 10px; PADDING-LEFT: 25px; BORDER-LEFT: #cccccc 1px solid; MARGIN: 50px; PADDING-RIGHT: 25px\">\n" +
                "<DIV class=header style=\"MARGIN-BOTTOM: 30px\">\n" +
                "<P>"+params.get("name")+"：</P></DIV>\n" +
                "<P>您好！您预约的【"+params.get("title")+"】"+params.get("reserveDate")+"的挂号订单已取消</P>\n" +
                "<P>费用将在2小时内退还给您的账户,请注意查收</P>\n"+
                "<DIV class=footer style=\"MARGIN-TOP: 30px\">\n" +
                "<P>"+name+"</P>\n" +
                "<P><SPAN style=\"BORDER-BOTTOM: #ccc 1px dashed; POSITION: relative; _display: inline-block\" t=\"5\" times=\"\" isout=\"0\">"+date+"</SPAN></P></DIV>\n" +
                "<DIV class=tip style=\"COLOR: #cccccc; TEXT-ALIGN: center\">该邮件为系统自动发送，请勿进行回复 </DIV></DIV></DIV></DIV></DIV></DIV></DIV></DIV></BODY>\n" +
                "</html>\n";

        return content;
    }
    //支付成功制作发送内容
    private String makeContent(String name,Map<String, Object> params){
        //获取当前时间
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String date = simpleDateFormat.format(new Date());

        String content = "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "<title></title>\n" +
                "<style>\n" +
                ".qmbox {\n" +
                "\tpadding: 0;\n" +
                "}\n" +
                ".qm_con_body_content {\n" +
                "\theight: auto;\n" +
                "\tmin-height: 100px;\n" +
                "\t_height: 100px;\n" +
                "\tword-wrap: break-word;\n" +
                "\tfont-size: 14px;\n" +
                "\tfont-family: \"lucida Grande\", Verdana, \"Microsoft YaHei\";\n" +
                "}\n" +
                ".body {\n" +
                "\tline-height: 170%;\n" +
                "}\n" +
                "BODY {\n" +
                "\tfont-family: \"lucida Grande\", Verdana, \"Microsoft YaHei\";\n" +
                "\tfont-size: 12px;\n" +
                "\t-webkit-font-smoothing: subpixel-antialiased;\n" +
                "}\n" +
                "BODY {\n" +
                "\tmargin: 0;\n" +
                "\tpadding: 0;\n" +
                "}\n" +
                "BODY {\n" +
                "\tbackground-color: #fff;\n" +
                "\tfont-size: 12px;\n" +
                "}\n" +
                "BODY {\n" +
                "\tbackground: #fff;\n" +
                "}\n" +
                "BODY {\n" +
                "\tbackground: #fff;\n" +
                "\tcolor: #000;\n" +
                "\tfont-weight: normal;\n" +
                "\tfont-family: \"lucida Grande\", Verdana, \"Microsoft YaHei\";\n" +
                "\tpadding: 0 7px 6px 4px;\n" +
                "\tmargin: 0;\n" +
                "}\n" +
                "HTML {\n" +
                "\ttop: 0px;\n" +
                "}\n" +
                ".body P {\n" +
                "\tline-height: 170%;\n" +
                "}\n" +
                "</style>\n" +
                "</head>\n" +
                "\n" +
                "<BODY mu=\"mu\" md=\"md\" module=\"qmReadMail\" context=\"ZC1912-rQ7uXSy7P7HThxdLFCOuY92\"><DIV class=mailcontainer id=qqmail_mailcontainer>\n" +
                "<DIV id=mainmail style=\"MARGIN-BOTTOM: 12px; POSITION: relative; Z-INDEX: 1\">\n" +
                "<DIV class=body id=contentDiv style=\"FONT-SIZE: 14px; HEIGHT: auto; POSITION: relative; ZOOM: 1; PADDING-BOTTOM: 10px; PADDING-TOP: 15px; PADDING-LEFT: 15px; Z-INDEX: 1; LINE-HEIGHT: 1.7; PADDING-RIGHT: 15px\" onmouseover=getTop().stopPropagation(event); onclick=\"getTop().preSwapLink(event, 'html', 'ZC1912-rQ7uXSy7P7HThxdLFCOuY92');\">\n" +
                "<DIV id=qm_con_body>\n" +
                "<DIV class=\"qmbox qm_con_body_content qqmail_webmail_only\" id=mailContentContainer>\n" +
                "<DIV class=main style=\"OVERFLOW: hidden; WIDTH: 100%; BACKGROUND-COLOR: #f7f7f7\">\n" +
                "<DIV class=content style=\"BORDER-TOP: #cccccc 1px solid; BORDER-RIGHT: #cccccc 1px solid; BACKGROUND: #ffffff; BORDER-BOTTOM: #cccccc 1px solid; PADDING-BOTTOM: 10px; PADDING-TOP: 10px; PADDING-LEFT: 25px; BORDER-LEFT: #cccccc 1px solid; MARGIN: 50px; PADDING-RIGHT: 25px\">\n" +
                "<DIV class=header style=\"MARGIN-BOTTOM: 30px\">\n" +
                "<P>"+params.get("name")+"：</P></DIV>\n" +
                "<P>您好！您已预约【"+params.get("title")+"】"+params.get("reserveDate")+"的号源</P>\n" +
                "<P><SPAN style=\"FONT-SIZE: 16px; FONT-WEIGHT: bold; COLOR: #f90\">注意事项</SPAN></P>\n" +
                "<P>1、请确认就诊人信息是否准确，若填写错误将无法取号就诊，损失由本人承担；<br/>" +
                "<span style=\"color: red\"\n" +
                "              >2、【取号】就诊当天需在\n" +
                params.get("fetchTime")+"\n" +
                "              在医院取号，未取号视为爽约，该号不退不换；</span\n" +
                "            ><br />\n" +
                "            3、【退号】在"+params.get("quitTime")+"前可在线退号\n" +
                "            ，逾期将不可办理退号退费；<br />\n" +
                "            4、医点通在线预约挂号平台支持自费患者使用身份证预约，同时支持医保患者使用社保卡在平台预约挂号。请于就诊当日，携带预约挂号所使用的有效身份证件到院取号；<br />\n" +
                "            5、请注意医保患者在住院期间不能使用社保卡在门诊取号。</P>\n" +

                "<DIV class=footer style=\"MARGIN-TOP: 30px\">\n" +
                "<P>"+name+"</P>\n" +
                "<P><SPAN style=\"BORDER-BOTTOM: #ccc 1px dashed; POSITION: relative; _display: inline-block\" t=\"5\" times=\"\" isout=\"0\">"+date+"</SPAN></P></DIV>\n" +
                "<DIV class=tip style=\"COLOR: #cccccc; TEXT-ALIGN: center\">该邮件为系统自动发送，请勿进行回复 </DIV></DIV></DIV></DIV></DIV></DIV></DIV></DIV></BODY>\n" +
                "</html>\n";

        return content;
    }
    //取消预约制作发送内容
    private String makeContent(String name){
        //获取当前时间
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String date = simpleDateFormat.format(new Date());

        String content = "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "<title></title>\n" +
                "<style>\n" +
                ".qmbox {\n" +
                "\tpadding: 0;\n" +
                "}\n" +
                ".qm_con_body_content {\n" +
                "\theight: auto;\n" +
                "\tmin-height: 100px;\n" +
                "\t_height: 100px;\n" +
                "\tword-wrap: break-word;\n" +
                "\tfont-size: 14px;\n" +
                "\tfont-family: \"lucida Grande\", Verdana, \"Microsoft YaHei\";\n" +
                "}\n" +
                ".body {\n" +
                "\tline-height: 170%;\n" +
                "}\n" +
                "BODY {\n" +
                "\tfont-family: \"lucida Grande\", Verdana, \"Microsoft YaHei\";\n" +
                "\tfont-size: 12px;\n" +
                "\t-webkit-font-smoothing: subpixel-antialiased;\n" +
                "}\n" +
                "BODY {\n" +
                "\tmargin: 0;\n" +
                "\tpadding: 0;\n" +
                "}\n" +
                "BODY {\n" +
                "\tbackground-color: #fff;\n" +
                "\tfont-size: 12px;\n" +
                "}\n" +
                "BODY {\n" +
                "\tbackground: #fff;\n" +
                "}\n" +
                "BODY {\n" +
                "\tbackground: #fff;\n" +
                "\tcolor: #000;\n" +
                "\tfont-weight: normal;\n" +
                "\tfont-family: \"lucida Grande\", Verdana, \"Microsoft YaHei\";\n" +
                "\tpadding: 0 7px 6px 4px;\n" +
                "\tmargin: 0;\n" +
                "}\n" +
                "HTML {\n" +
                "\ttop: 0px;\n" +
                "}\n" +
                ".body P {\n" +
                "\tline-height: 170%;\n" +
                "}\n" +
                "</style>\n" +
                "</head>\n" +
                "\n" +
                "<BODY mu=\"mu\" md=\"md\" module=\"qmReadMail\" context=\"ZC1912-rQ7uXSy7P7HThxdLFCOuY92\"><DIV class=mailcontainer id=qqmail_mailcontainer>\n" +
                "<DIV id=mainmail style=\"MARGIN-BOTTOM: 12px; POSITION: relative; Z-INDEX: 1\">\n" +
                "<DIV class=body id=contentDiv style=\"FONT-SIZE: 14px; HEIGHT: auto; POSITION: relative; ZOOM: 1; PADDING-BOTTOM: 10px; PADDING-TOP: 15px; PADDING-LEFT: 15px; Z-INDEX: 1; LINE-HEIGHT: 1.7; PADDING-RIGHT: 15px\" onmouseover=getTop().stopPropagation(event); onclick=\"getTop().preSwapLink(event, 'html', 'ZC1912-rQ7uXSy7P7HThxdLFCOuY92');\">\n" +
                "<DIV id=qm_con_body>\n" +
                "<DIV class=\"qmbox qm_con_body_content qqmail_webmail_only\" id=mailContentContainer>\n" +
                "<DIV class=main style=\"OVERFLOW: hidden; WIDTH: 100%; BACKGROUND-COLOR: #f7f7f7\">\n" +
                "<DIV class=content style=\"BORDER-TOP: #cccccc 1px solid; BORDER-RIGHT: #cccccc 1px solid; BACKGROUND: #ffffff; BORDER-BOTTOM: #cccccc 1px solid; PADDING-BOTTOM: 10px; PADDING-TOP: 10px; PADDING-LEFT: 25px; BORDER-LEFT: #cccccc 1px solid; MARGIN: 50px; PADDING-RIGHT: 25px\">\n" +
                "<DIV class=header style=\"MARGIN-BOTTOM: 30px\">\n" +
                "<P>"+name+"：</P></DIV>\n" +
                "<P><SPAN style=\"FONT-SIZE: 16px; FONT-WEIGHT: bold; COLOR: #f90\">您的预约已取消！</SPAN><SPAN style=\"COLOR: #000000\"></P>\n" +
                "<DIV class=footer style=\"MARGIN-TOP: 30px\">\n" +
                "<P><SPAN style=\"BORDER-BOTTOM: #ccc 1px dashed; POSITION: relative; _display: inline-block\" t=\"5\" times=\"\" isout=\"0\">"+date+"</SPAN></P></DIV>\n" +
                "<DIV class=tip style=\"COLOR: #cccccc; TEXT-ALIGN: center\">该邮件为系统自动发送，请勿进行回复 </DIV></DIV></DIV></DIV></DIV></DIV></DIV></DIV></BODY>\n" +
                "</html>\n";

        return content;
    }

    //预约下单制作发送内容
    private String makeContent(Map<String,Object> params, String name){
        //获取当前时间
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String date = simpleDateFormat.format(new Date());

        String content = "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "<title></title>\n" +
                "<style>\n" +
                ".qmbox {\n" +
                "\tpadding: 0;\n" +
                "}\n" +
                ".qm_con_body_content {\n" +
                "\theight: auto;\n" +
                "\tmin-height: 100px;\n" +
                "\t_height: 100px;\n" +
                "\tword-wrap: break-word;\n" +
                "\tfont-size: 14px;\n" +
                "\tfont-family: \"lucida Grande\", Verdana, \"Microsoft YaHei\";\n" +
                "}\n" +
                ".body {\n" +
                "\tline-height: 170%;\n" +
                "}\n" +
                "BODY {\n" +
                "\tfont-family: \"lucida Grande\", Verdana, \"Microsoft YaHei\";\n" +
                "\tfont-size: 12px;\n" +
                "\t-webkit-font-smoothing: subpixel-antialiased;\n" +
                "}\n" +
                "BODY {\n" +
                "\tmargin: 0;\n" +
                "\tpadding: 0;\n" +
                "}\n" +
                "BODY {\n" +
                "\tbackground-color: #fff;\n" +
                "\tfont-size: 12px;\n" +
                "}\n" +
                "BODY {\n" +
                "\tbackground: #fff;\n" +
                "}\n" +
                "BODY {\n" +
                "\tbackground: #fff;\n" +
                "\tcolor: #000;\n" +
                "\tfont-weight: normal;\n" +
                "\tfont-family: \"lucida Grande\", Verdana, \"Microsoft YaHei\";\n" +
                "\tpadding: 0 7px 6px 4px;\n" +
                "\tmargin: 0;\n" +
                "}\n" +
                "HTML {\n" +
                "\ttop: 0px;\n" +
                "}\n" +
                ".body P {\n" +
                "\tline-height: 170%;\n" +
                "}\n" +
                "</style>\n" +
                "</head>\n" +
                "\n" +
                "<BODY mu=\"mu\" md=\"md\" module=\"qmReadMail\" context=\"ZC1912-rQ7uXSy7P7HThxdLFCOuY92\"><DIV class=mailcontainer id=qqmail_mailcontainer>\n" +
                "<DIV id=mainmail style=\"MARGIN-BOTTOM: 12px; POSITION: relative; Z-INDEX: 1\">\n" +
                "<DIV class=body id=contentDiv style=\"FONT-SIZE: 14px; HEIGHT: auto; POSITION: relative; ZOOM: 1; PADDING-BOTTOM: 10px; PADDING-TOP: 15px; PADDING-LEFT: 15px; Z-INDEX: 1; LINE-HEIGHT: 1.7; PADDING-RIGHT: 15px\" onmouseover=getTop().stopPropagation(event); onclick=\"getTop().preSwapLink(event, 'html', 'ZC1912-rQ7uXSy7P7HThxdLFCOuY92');\">\n" +
                "<DIV id=qm_con_body>\n" +
                "<DIV class=\"qmbox qm_con_body_content qqmail_webmail_only\" id=mailContentContainer>\n" +
                "<DIV class=main style=\"OVERFLOW: hidden; WIDTH: 100%; BACKGROUND-COLOR: #f7f7f7\">\n" +
                "<DIV class=content style=\"BORDER-TOP: #cccccc 1px solid; BORDER-RIGHT: #cccccc 1px solid; BACKGROUND: #ffffff; BORDER-BOTTOM: #cccccc 1px solid; PADDING-BOTTOM: 10px; PADDING-TOP: 10px; PADDING-LEFT: 25px; BORDER-LEFT: #cccccc 1px solid; MARGIN: 50px; PADDING-RIGHT: 25px\">\n" +
                "<DIV class=header style=\"MARGIN-BOTTOM: 30px\">\n" +
                "<P>"+params.get("name")+"：</P></DIV>\n" +
                "<P>您好！您正在预约【"+params.get("title")+"】"+params.get("reserveDate")+"的号源，价格为：</P>\n" +
                "<P><SPAN style=\"FONT-SIZE: 16px; FONT-WEIGHT: bold; COLOR: #f90\">"+params.get("amount")+"元</SPAN><SPAN style=\"COLOR: #000000\">(为了保障您能预约成功，请您尽快支付费用)</SPAN></P>\n" +
                "<DIV class=footer style=\"MARGIN-TOP: 30px\">\n" +
                "<P>"+name+"</P>\n" +
                "<P><SPAN style=\"BORDER-BOTTOM: #ccc 1px dashed; POSITION: relative; _display: inline-block\" t=\"5\" times=\"\" isout=\"0\">"+date+"</SPAN></P></DIV>\n" +
                "<DIV class=tip style=\"COLOR: #cccccc; TEXT-ALIGN: center\">该邮件为系统自动发送，请勿进行回复 </DIV></DIV></DIV></DIV></DIV></DIV></DIV></DIV></BODY>\n" +
                "</html>\n";

        return content;
    }
    //登录制作发送内容
    private String makeContent(String name,String code){
        //获取当前时间
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String date = simpleDateFormat.format(new Date());

        String content = "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "<title></title>\n" +
                "<style>\n" +
                ".qmbox {\n" +
                "\tpadding: 0;\n" +
                "}\n" +
                ".qm_con_body_content {\n" +
                "\theight: auto;\n" +
                "\tmin-height: 100px;\n" +
                "\t_height: 100px;\n" +
                "\tword-wrap: break-word;\n" +
                "\tfont-size: 14px;\n" +
                "\tfont-family: \"lucida Grande\", Verdana, \"Microsoft YaHei\";\n" +
                "}\n" +
                ".body {\n" +
                "\tline-height: 170%;\n" +
                "}\n" +
                "BODY {\n" +
                "\tfont-family: \"lucida Grande\", Verdana, \"Microsoft YaHei\";\n" +
                "\tfont-size: 12px;\n" +
                "\t-webkit-font-smoothing: subpixel-antialiased;\n" +
                "}\n" +
                "BODY {\n" +
                "\tmargin: 0;\n" +
                "\tpadding: 0;\n" +
                "}\n" +
                "BODY {\n" +
                "\tbackground-color: #fff;\n" +
                "\tfont-size: 12px;\n" +
                "}\n" +
                "BODY {\n" +
                "\tbackground: #fff;\n" +
                "}\n" +
                "BODY {\n" +
                "\tbackground: #fff;\n" +
                "\tcolor: #000;\n" +
                "\tfont-weight: normal;\n" +
                "\tfont-family: \"lucida Grande\", Verdana, \"Microsoft YaHei\";\n" +
                "\tpadding: 0 7px 6px 4px;\n" +
                "\tmargin: 0;\n" +
                "}\n" +
                "HTML {\n" +
                "\ttop: 0px;\n" +
                "}\n" +
                ".body P {\n" +
                "\tline-height: 170%;\n" +
                "}\n" +
                "</style>\n" +
                "</head>\n" +
                "\n" +
                "<BODY mu=\"mu\" md=\"md\" module=\"qmReadMail\" context=\"ZC1912-rQ7uXSy7P7HThxdLFCOuY92\"><DIV class=mailcontainer id=qqmail_mailcontainer>\n" +
                "<DIV id=mainmail style=\"MARGIN-BOTTOM: 12px; POSITION: relative; Z-INDEX: 1\">\n" +
                "<DIV class=body id=contentDiv style=\"FONT-SIZE: 14px; HEIGHT: auto; POSITION: relative; ZOOM: 1; PADDING-BOTTOM: 10px; PADDING-TOP: 15px; PADDING-LEFT: 15px; Z-INDEX: 1; LINE-HEIGHT: 1.7; PADDING-RIGHT: 15px\" onmouseover=getTop().stopPropagation(event); onclick=\"getTop().preSwapLink(event, 'html', 'ZC1912-rQ7uXSy7P7HThxdLFCOuY92');\">\n" +
                "<DIV id=qm_con_body>\n" +
                "<DIV class=\"qmbox qm_con_body_content qqmail_webmail_only\" id=mailContentContainer>\n" +
                "<DIV class=main style=\"OVERFLOW: hidden; WIDTH: 100%; BACKGROUND-COLOR: #f7f7f7\">\n" +
                "<DIV class=content style=\"BORDER-TOP: #cccccc 1px solid; BORDER-RIGHT: #cccccc 1px solid; BACKGROUND: #ffffff; BORDER-BOTTOM: #cccccc 1px solid; PADDING-BOTTOM: 10px; PADDING-TOP: 10px; PADDING-LEFT: 25px; BORDER-LEFT: #cccccc 1px solid; MARGIN: 50px; PADDING-RIGHT: 25px\">\n" +
                "<DIV class=header style=\"MARGIN-BOTTOM: 30px\">\n" +
                "<P>亲爱的用户：</P></DIV>\n" +
                "<P>您好！您正在进行邮箱验证，本次请求的验证码为：</P>\n" +
                "<P><SPAN style=\"FONT-SIZE: 18px; FONT-WEIGHT: bold; COLOR: #f90\">"+code+"</SPAN><SPAN style=\"COLOR: #000000\">(为了保障您帐号的安全性，请在10分钟内完成验证)</SPAN></P>\n" +
                "<DIV class=footer style=\"MARGIN-TOP: 30px\">\n" +
                "<P>"+name+"</P>\n" +
                "<P><SPAN style=\"BORDER-BOTTOM: #ccc 1px dashed; POSITION: relative; _display: inline-block\" t=\"5\" times=\"\" isout=\"0\">"+date+"</SPAN></P></DIV>\n" +
                "<DIV class=tip style=\"COLOR: #cccccc; TEXT-ALIGN: center\">该邮件为系统自动发送，请勿进行回复 </DIV></DIV></DIV></DIV></DIV></DIV></DIV></DIV></BODY>\n" +
                "</html>\n";

        return content;
    }
}