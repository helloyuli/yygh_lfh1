package com.atguigu.yygh.order.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.yygh.enums.PaymentStatusEnum;
import com.atguigu.yygh.enums.PaymentTypeEnum;
import com.atguigu.yygh.enums.RefundStatusEnum;
import com.atguigu.yygh.model.order.OrderInfo;
import com.atguigu.yygh.model.order.PaymentInfo;
import com.atguigu.yygh.model.order.RefundInfo;
import com.atguigu.yygh.order.service.OrderService;
import com.atguigu.yygh.order.service.PaymentService;
import com.atguigu.yygh.order.service.RefundInfoService;
import com.atguigu.yygh.order.service.WXService;
import com.atguigu.yygh.order.utils.ConstantPropertiesUtils;
import com.atguigu.yygh.order.utils.HttpClient;
import com.atguigu.yygh.order.utils.SendMsmMq;
import com.atguigu.yygh.rabbit.service.RabbitService;
import com.atguigu.yygh.vo.msm.MsmVo;
import com.atguigu.yygh.vo.order.OrderMqVo;
import com.github.wxpay.sdk.WXPayConstants;
import com.github.wxpay.sdk.WXPayUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class WXServiceImpl implements WXService {

	@Autowired
	private OrderService orderService;

	@Autowired
	private PaymentService paymentService;

	@Autowired
	private RefundInfoService refundInfoService;

	@Autowired
	private RedisTemplate redisTemplate;

	@Autowired
	private RabbitService rabbitService;
	//生成微信支付二维码
	@Override
	public Map createNative(Long orderId) {
		try {
			//先从redis中取，如果有直接返回，没有再添加到数据库中
			Map payMap = (Map) redisTemplate.opsForValue().get(orderId.toString());
			if (payMap!=null){
				return payMap;
			}
			//根据orderId获取订单信息
			OrderInfo order = orderService.getById(orderId);

			//设置参数
			Map paramMap = new HashMap();
			paramMap.put("appid", ConstantPropertiesUtils.APPID);
			paramMap.put("mch_id", ConstantPropertiesUtils.PARTNER);
			paramMap.put("nonce_str", WXPayUtil.generateNonceStr());
			String body = order.getReserveDate() + "就诊"+ order.getDepname();
			paramMap.put("body", body);
			paramMap.put("out_trade_no", order.getOutTradeNo());
			//paramMap.put("total_fee", order.getAmount().multiply(new BigDecimal("100")).longValue()+"");
			paramMap.put("total_fee", "1");//测试0.01元
			paramMap.put("spbill_create_ip", "127.0.0.1");
			paramMap.put("notify_url", "http://guli.shop/api/order/weixinPay/weixinNotify");
			paramMap.put("trade_type", "NATIVE");
			//调用微信生成二维码接口
			HttpClient client = new HttpClient("https://api.mch.weixin.qq.com/pay/unifiedorder");
			//设置Map中的参数
			client.setXmlParam(WXPayUtil.generateSignedXml(paramMap,ConstantPropertiesUtils.PARTNERKEY));
			client.setHttps(true);
			client.post();
			//微信那边返回相关数据
			String xml = client.getContent();
			//xml转换map
			Map<String, String> resultMap = WXPayUtil.xmlToMap(xml);
			System.out.println("resultMap:"+resultMap);
			//4、封装返回结果集
			Map map = new HashMap<>();
			if (resultMap!=null){
				//向支付记录表中添加数据
				paymentService.savePaymentInfo(order, PaymentTypeEnum.WEIXIN.getStatus());
				map.put("orderId", orderId);
				map.put("totalFee", order.getAmount());
				map.put("resultCode", resultMap.get("result_code"));
				map.put("codeUrl", resultMap.get("code_url"));//二维码地址
				//redis缓存
				if (resultMap.get("result_code")!=null){
					redisTemplate.opsForValue().set(orderId.toString(),map,120, TimeUnit.MINUTES);
				}
			}
			return map;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public Map queryPayStatus(Long orderId, String paymentType) {
		try {
			OrderInfo orderInfo = orderService.getById(orderId);
			//1、封装参数
			Map paramMap = new HashMap<>();
			paramMap.put("appid", ConstantPropertiesUtils.APPID);
			paramMap.put("mch_id", ConstantPropertiesUtils.PARTNER);
			paramMap.put("out_trade_no", orderInfo.getOutTradeNo());
			paramMap.put("nonce_str", WXPayUtil.generateNonceStr());
			//2、设置请求
			HttpClient client = new HttpClient("https://api.mch.weixin.qq.com/pay/orderquery");
			client.setXmlParam(WXPayUtil.generateSignedXml(paramMap, ConstantPropertiesUtils.PARTNERKEY));
			client.setHttps(true);
			client.post();
			//3、返回第三方的数据，转成Map
			String xml = client.getContent();
			Map<String, String> resultMap = WXPayUtil.xmlToMap(xml);
			//4、返回
			return resultMap;
		} catch (Exception e) {
			return null;
		}
	}
	@Override
	public Boolean refund(Long orderId) {
		try {
			OrderInfo orderInfo = orderService.getById(orderId);
			PaymentInfo paymentInfoQuery = paymentService.getPaymentInfo(orderId, PaymentTypeEnum.WEIXIN.getStatus());

			RefundInfo refundInfo = refundInfoService.saveRefundInfo(paymentInfoQuery);
			if(refundInfo.getRefundStatus().intValue() == RefundStatusEnum.REFUND.getStatus().intValue()) {
				return true;
			}
			Map<String,String> paramMap = new HashMap<>(8);
			paramMap.put("appid",ConstantPropertiesUtils.APPID);       //公众账号ID
			paramMap.put("mch_id",ConstantPropertiesUtils.PARTNER);   //商户编号
			paramMap.put("nonce_str",WXPayUtil.generateNonceStr());
			paramMap.put("transaction_id",paymentInfoQuery.getTradeNo()); //微信订单号
			paramMap.put("out_trade_no",paymentInfoQuery.getOutTradeNo()); //商户订单编号
			paramMap.put("out_refund_no","tk"+paymentInfoQuery.getOutTradeNo()); //商户退款单号
//       paramMap.put("total_fee",paymentInfoQuery.getTotalAmount().multiply(new BigDecimal("100")).longValue()+"");
//       paramMap.put("refund_fee",paymentInfoQuery.getTotalAmount().multiply(new BigDecimal("100")).longValue()+"");
			paramMap.put("total_fee","1");
			paramMap.put("refund_fee","1");
			String paramXml = WXPayUtil.generateSignedXml(paramMap,ConstantPropertiesUtils.PARTNERKEY);
			HttpClient client = new HttpClient("https://api.mch.weixin.qq.com/secapi/pay/refund");
			client.setXmlParam(paramXml);
			client.setHttps(true);
			client.setCert(true);
			client.setCertPassword(ConstantPropertiesUtils.PARTNER);
			client.post();
			//3、返回第三方的数据
			String xml = client.getContent();
			Map<String, String> resultMap = WXPayUtil.xmlToMap(xml);
			if (WXPayConstants.SUCCESS.equalsIgnoreCase(resultMap.get("result_code"))) {
				refundInfo.setCallbackTime(new Date());
				refundInfo.setTradeNo(resultMap.get("refund_id"));
				refundInfo.setRefundStatus(RefundStatusEnum.REFUND.getStatus());
				refundInfo.setCallbackContent(JSONObject.toJSONString(resultMap));
				refundInfoService.updateById(refundInfo);
				//退款成功（有延迟)
				SendMsmMq.sendMq(orderInfo,refundInfo,"退款成功",rabbitService);
				return true;
			}
			return false;
		}  catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
}
