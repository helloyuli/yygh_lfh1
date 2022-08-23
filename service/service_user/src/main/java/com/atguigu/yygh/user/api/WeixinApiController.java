package com.atguigu.yygh.user.api;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.yygh.common.helper.JwtHelper;
import com.atguigu.yygh.common.result.Result;
import com.atguigu.yygh.model.user.UserInfo;
import com.atguigu.yygh.user.service.UserInfoService;
import com.atguigu.yygh.user.utils.ConstantPropertiesUtil;
import com.atguigu.yygh.user.utils.HttpClientUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

//微信操作接口
@Controller
@RequestMapping("/api/ucenter/wx")
public class WeixinApiController {

	@Autowired
	private UserInfoService userInfoService;
	@Autowired
	private RedisTemplate redisTemplate;

	//生成微信扫描的二维码
	/**
	 * 获取微信登录参数
	 */
	@GetMapping("getLoginParam")
	@ResponseBody
	public Result genQrConnect(HttpSession session) throws UnsupportedEncodingException {

		Map<String, Object> map = new HashMap<>();
		map.put("appid", ConstantPropertiesUtil.WX_OPEN_APP_ID);
		map.put("scope","snsapi_login");
		String wxOpenRedirectUrl = ConstantPropertiesUtil.WX_OPEN_REDIRECT_URL;
		//wxOpenRedirectUrl = URLEncoder.encode(wxOpenRedirectUrl, "utf-8");
		map.put("redirect_uri",wxOpenRedirectUrl);
		map.put("state",System.currentTimeMillis()+"");
		return Result.ok(map);

	}
	//微信扫码后回调方法
	@GetMapping("callback")
	public String callback(String code,String state) {
		//1.获取临时票据code
		System.out.println("code:" + code);
		//2.拿着code和微信id和秘钥，请求微信固定地址，得到两个值
		//使用code和appid以及appscrect换取access_token
		StringBuffer baseAccessTokenUrl = new StringBuffer()
				.append("https://api.weixin.qq.com/sns/oauth2/access_token")
				.append("?appid=%s")
				.append("&secret=%s")
				.append("&code=%s")
				.append("&grant_type=authorization_code");

		String accessTokenUrl = String.format(baseAccessTokenUrl.toString(),
				ConstantPropertiesUtil.WX_OPEN_APP_ID,
				ConstantPropertiesUtil.WX_OPEN_APP_SECRET,
				code);

		//使用httpclient请求这个地址
		try {
			String accesstokenInfo = HttpClientUtils.get(accessTokenUrl);
			System.out.println("accesstokenInfo:" + accesstokenInfo);
			JSONObject jsonObject = JSONObject.parseObject(accesstokenInfo);
			String access_token = jsonObject.getString("access_token");
			String openid = jsonObject.getString("openid");
			//判断数据库中是否已经存在微信的扫码人信息
			//根据openid判断
			UserInfo userInfo = userInfoService.selectWxInfoOpenId(openid);
			if (userInfo == null) {//数据库不存在微信信息
				//3.拿着openid和access_token请求微信地址，得到扫码人信息
				String baseUserInfoUrl = "https://api.weixin.qq.com/sns/userinfo" +
						"?access_token=%s" +
						"&openid=%s";
				String userInfoUrl = String.format(baseUserInfoUrl, access_token, openid);
				String resultInfo = HttpClientUtils.get(userInfoUrl);
				System.out.println(resultInfo);
				JSONObject resultUserInfoJson = JSONObject.parseObject(resultInfo);
				//解析用户信息
				String nickname = resultUserInfoJson.getString("nickname");  //昵称
				String headimgurl = resultUserInfoJson.getString("headimgurl");  //头像

				//获取扫码人信息添加到数据库
				userInfo = new UserInfo();
				userInfo.setOpenid(openid);
				userInfo.setNickName(nickname);
				userInfo.setStatus(1);
				userInfoService.save(userInfo);
			}


			//返回name和token的字符串
			Map<String, Object> map = new HashMap<>();
			String name = userInfo.getName();
			if (StringUtils.isEmpty(name)) {
				name = userInfo.getNickName();
			}
			if (StringUtils.isEmpty(name)) {
				name = userInfo.getMail();
			}
			map.put("name", name);
			//判断userInfo是否有手机号，如果手机号为空，返回openid
			//如果为空，返回openid值是空字符串
			//前端判断，如果openid不为空，绑定手机号，反之不绑定手机号
			if (StringUtils.isEmpty(userInfo.getMail())) {
				map.put("openid", "");
			} else {
				map.put("openid", userInfo.getOpenid());
			}
			String token = JwtHelper.createToken(userInfo.getId(), name);
			map.put("token", token);
			//跳转到前端页面
			return "redirect:" + ConstantPropertiesUtil.YYGH_BASE_URL +
					"/weixin/callback?token=" + map.get("token") +
					"&openid=" + map.get("openid") +
					"&name=" + URLEncoder.encode((String) map.get("name"), "utf-8");
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
