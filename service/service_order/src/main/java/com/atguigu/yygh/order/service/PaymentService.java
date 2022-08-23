package com.atguigu.yygh.order.service;

import com.atguigu.yygh.model.order.OrderInfo;
import com.atguigu.yygh.model.order.PaymentInfo;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Map;

public interface PaymentService extends IService<PaymentInfo> {
	//向支付记录表中添加数据
	void savePaymentInfo(OrderInfo order, Integer status);

	/**
	 * 支付成功
	 */
	void paySuccess(String outTradeNo, Integer paymentType, Map<String, String> paramMap);

	/**
	 * 获取支付记录
	 * @param orderId
	 * @param paymentType
	 * @return
	 */
	PaymentInfo getPaymentInfo(Long orderId, Integer paymentType);
}
