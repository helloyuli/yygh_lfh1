package com.atguigu.yygh.order.utils;

import com.atguigu.yygh.model.order.OrderInfo;
import com.atguigu.yygh.model.order.RefundInfo;
import com.atguigu.yygh.rabbit.constant.MqConst;
import com.atguigu.yygh.rabbit.service.RabbitService;
import com.atguigu.yygh.vo.msm.MsmVo;
import com.atguigu.yygh.vo.order.OrderMqVo;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;

public class SendMsmMq {

	private SendMsmMq() {
	}

	//取消订单/支付成功
	public static void sendMq(OrderInfo orderInfo,String TemplateCode, RabbitService rabbitService) {
		//发送mq信息更新预约数 我们与下单成功更新预约数使用相同的mq信息，不设置可预约数与剩余预约数，接收端可预约数减1即可
		OrderMqVo orderMqVo = new OrderMqVo();
		orderMqVo.setScheduleId(orderInfo.getScheduleId());
		//短信提示
		MsmVo msmVo = new MsmVo();
		msmVo.setMail(orderInfo.getPatientMail());
		msmVo.setTemplateCode(TemplateCode);
		String reserveDate = new DateTime(orderInfo.getReserveDate()).toString("yyyy-MM-dd") + (orderInfo.getReserveTime()==0 ? "上午": "下午");
		Map<String,Object> param = new HashMap<String,Object>(){{
			put("title", orderInfo.getHosname()+"|"+orderInfo.getDepname()+"|"+orderInfo.getTitle());
			put("reserveDate", reserveDate);
			put("hosname",orderInfo.getHosname());
			put("name", orderInfo.getPatientName());
			put("fetchTime",orderInfo.getFetchTime());
			put("quitTime",orderInfo.getQuitTime());
		}};
		msmVo.setParam(param);
		orderMqVo.setMsmVo(msmVo);
		rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_ORDER, MqConst.ROUTING_ORDER, orderMqVo);
	}

	//退款成功
	public static void sendMq(OrderInfo orderInfo,RefundInfo refundInfo, String TemplateCode, RabbitService rabbitService) {
		//发送mq信息更新预约数 我们与下单成功更新预约数使用相同的mq信息，不设置可预约数与剩余预约数，接收端可预约数减1即可
		OrderMqVo orderMqVo = new OrderMqVo();
		orderMqVo.setScheduleId(orderInfo.getScheduleId());
		//短信提示
		MsmVo msmVo = new MsmVo();
		msmVo.setMail(orderInfo.getPatientMail());
		msmVo.setTemplateCode(TemplateCode);
		String reserveDate = new DateTime(orderInfo.getReserveDate()).toString("yyyy-MM-dd") + (orderInfo.getReserveTime()==0 ? "上午": "下午");
		Map<String,Object> param = new HashMap<String,Object>(){{
			put("title", orderInfo.getHosname()+"|"+orderInfo.getDepname()+"|"+orderInfo.getTitle());
			put("hosname",orderInfo.getHosname());
			put("reserveDate", reserveDate);
			put("name", orderInfo.getPatientName());
			put("totalAmount",refundInfo.getTotalAmount());
		}};
		msmVo.setParam(param);
		orderMqVo.setMsmVo(msmVo);
		rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_ORDER, MqConst.ROUTING_ORDER, orderMqVo);
	}

	//预约下单
	public static void sendMq(String scheduleId, OrderInfo orderInfo, Integer reservedNumber, Integer availableNumber, RabbitService rabbitService) {
		//发送mq信息更新号源和短信通知
		//发送mq信息更新号源
		OrderMqVo orderMqVo = new OrderMqVo();
		orderMqVo.setScheduleId(scheduleId);
		orderMqVo.setReservedNumber(reservedNumber);
		orderMqVo.setAvailableNumber(availableNumber);

		//短信提示
		MsmVo msmVo = new MsmVo();
		msmVo.setMail(orderInfo.getPatientMail());
		msmVo.setTemplateCode("预约下单");
		String reserveDate =
				new DateTime(orderInfo.getReserveDate()).toString("yyyy-MM-dd")
						+ (orderInfo.getReserveTime()==0 ? "上午": "下午");
		Map<String,Object> param = new HashMap<String,Object>(){{
			//医院名称|科室名称|医生职称
			put("title", orderInfo.getHosname()+"|"+orderInfo.getDepname());
			put("hosname",orderInfo.getHosname());
			//医事服务费
			put("amount", orderInfo.getAmount());
			//安排日期
			put("reserveDate", reserveDate);
			//就诊人姓名
			put("name", orderInfo.getPatientName());
			//退号时间
			put("quitTime", new DateTime(orderInfo.getQuitTime()).toString("yyyy-MM-dd HH:mm"));
		}};
		msmVo.setParam(param);

		orderMqVo.setMsmVo(msmVo);
		rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_ORDER, MqConst.ROUTING_ORDER, orderMqVo);
	}

	//支付成功


	//退款成功
}
