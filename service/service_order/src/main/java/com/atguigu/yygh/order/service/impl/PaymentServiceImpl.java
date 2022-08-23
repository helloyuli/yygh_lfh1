package com.atguigu.yygh.order.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.yygh.common.exception.YyghException;
import com.atguigu.yygh.common.helper.HttpRequestHelper;
import com.atguigu.yygh.common.result.ResultCodeEnum;
import com.atguigu.yygh.enums.OrderStatusEnum;
import com.atguigu.yygh.enums.PaymentStatusEnum;
import com.atguigu.yygh.hosp.client.HospitalFeignClient;
import com.atguigu.yygh.model.order.OrderInfo;
import com.atguigu.yygh.model.order.PaymentInfo;
import com.atguigu.yygh.order.mapper.PaymentMapper;
import com.atguigu.yygh.order.service.OrderService;
import com.atguigu.yygh.order.service.PaymentService;
import com.atguigu.yygh.order.utils.SendMsmMq;
import com.atguigu.yygh.rabbit.service.RabbitService;
import com.atguigu.yygh.vo.order.SignInfoVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class PaymentServiceImpl extends ServiceImpl<PaymentMapper, PaymentInfo> implements PaymentService{


	@Autowired
	private OrderService orderService;
	@Autowired
	private RabbitService rabbitService;

	@Autowired
	private HospitalFeignClient hospitalFeignClient;

	//向支付记录表中添加数据
	@Override
	public void savePaymentInfo(OrderInfo order, Integer status) {
		//先根据订单id和支付类型查询支付记录表中是否有相同订单
		QueryWrapper<PaymentInfo> wrapper = new QueryWrapper<>();
		wrapper.eq("order_id",order.getId());
		wrapper.eq("payment_type",status);
		Integer count = baseMapper.selectCount(wrapper);
		//有数据直接返回
		if (count>0){
			return;
		}
		//添加记录
		PaymentInfo paymentInfo = new PaymentInfo();
		paymentInfo.setCreateTime(new Date());
		paymentInfo.setOrderId(order.getId());
		paymentInfo.setPaymentType(status);
		paymentInfo.setOutTradeNo(order.getOutTradeNo());
		paymentInfo.setPaymentStatus(PaymentStatusEnum.UNPAID.getStatus());
		String subject = new DateTime(order.getReserveDate()).toString("yyyy-MM-dd")+"|"+order.getHosname()+"|"+order.getDepname()+"|"+order.getTitle();
		paymentInfo.setSubject(subject);
		paymentInfo.setTotalAmount(order.getAmount());
		baseMapper.insert(paymentInfo);
	}

	/**
	 * 支付成功
	 */
	@Override
	public void paySuccess(String outTradeNo,Integer paymentType, Map<String,String> paramMap) {
		PaymentInfo paymentInfo = this.getPaymentInfo(outTradeNo, paymentType);
		if (null == paymentInfo) {
			throw new YyghException(ResultCodeEnum.PARAM_ERROR);
		}
		if (paymentInfo.getPaymentStatus() != PaymentStatusEnum.UNPAID.getStatus()) {
			return;
		}
		//修改支付状态
		PaymentInfo paymentInfoUpd = new PaymentInfo();
		paymentInfoUpd.setPaymentStatus(PaymentStatusEnum.PAID.getStatus());
		paymentInfoUpd.setTradeNo(paramMap.get("transaction_id"));
		paymentInfoUpd.setCallbackTime(new Date());
		paymentInfoUpd.setCallbackContent(paramMap.toString());
		this.updatePaymentInfo(outTradeNo, paymentInfoUpd);
		//修改订单状态
		OrderInfo orderInfo = orderService.getById(paymentInfo.getOrderId());
		orderInfo.setOrderStatus(OrderStatusEnum.PAID.getStatus());
		orderService.updateById(orderInfo);
		// 调用医院接口，通知更新支付状态
		SignInfoVo signInfoVo
				= hospitalFeignClient.getSignInfoVo(orderInfo.getHoscode());
		if(null == signInfoVo) {
			throw new YyghException(ResultCodeEnum.PARAM_ERROR);
		}
		Map<String, Object> reqMap = new HashMap<>();
		reqMap.put("hoscode",orderInfo.getHoscode());
		reqMap.put("hosRecordId",orderInfo.getHosRecordId());
		reqMap.put("timestamp", HttpRequestHelper.getTimestamp());
		reqMap.put("sign", signInfoVo.getSignKey());
		JSONObject result = HttpRequestHelper.sendRequest(reqMap, signInfoVo.getApiUrl()+"/order/updatePayStatus");
		SendMsmMq.sendMq(orderInfo,"支付成功",rabbitService);
		if(result.getInteger("code") != 200) {
			throw new YyghException(result.getString("message"), ResultCodeEnum.FAIL.getCode());
		}
	}
	@Override
	public PaymentInfo getPaymentInfo(Long orderId, Integer paymentType) {
		QueryWrapper<PaymentInfo> queryWrapper = new QueryWrapper<>();
		queryWrapper.eq("order_id", orderId);
		queryWrapper.eq("payment_type", paymentType);
		return baseMapper.selectOne(queryWrapper);
	}
	/**
	 * 获取支付记录
	 */
	private PaymentInfo getPaymentInfo(String outTradeNo, Integer paymentType) {
		QueryWrapper<PaymentInfo> queryWrapper = new QueryWrapper<>();
		queryWrapper.eq("out_trade_no", outTradeNo);
		queryWrapper.eq("payment_type", paymentType);
		return baseMapper.selectOne(queryWrapper);
	}
	/**
	 * 更改支付记录
	 */
	private void updatePaymentInfo(String outTradeNo, PaymentInfo paymentInfoUpd) {
		QueryWrapper<PaymentInfo> queryWrapper = new QueryWrapper<>();
		queryWrapper.eq("out_trade_no", outTradeNo);
		baseMapper.update(paymentInfoUpd, queryWrapper);
	}
}
