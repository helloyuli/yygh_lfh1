package com.atguigu.yygh.order.service;

import java.util.Map;

public interface WXService {
	//生成微信支付二维码
	Map createNative(Long orderId);

	/**
	 * 根据订单号去微信第三方查询支付状态
	 */
	Map queryPayStatus(Long orderId, String paymentType);

	/***
	 * 退款
	 * @param orderId
	 * @return
	 */
	Boolean refund(Long orderId);
}
