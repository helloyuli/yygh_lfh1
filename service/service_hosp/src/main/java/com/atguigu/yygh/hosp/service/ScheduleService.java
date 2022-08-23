package com.atguigu.yygh.hosp.service;

import com.atguigu.yygh.model.hosp.Schedule;
import com.atguigu.yygh.vo.hosp.ScheduleOrderVo;
import com.atguigu.yygh.vo.hosp.ScheduleQueryVo;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public interface ScheduleService {

	//保存排班信息
	void save(Map<String, Object> paramMap);
	//查询排班信息
	Page<Schedule> findPageSchedule(int page, int limit, ScheduleQueryVo scheduleQueryVo);
	//删除排班
	void remove(String hoscode, String hosScheduleId);

	//根据医院编号和科室编号查询排版规则数据
	Map<String, Object> getRuleSchedule(long page, long limit, String hoscode, String depcode);

	//根据医院编号、科室编号和工作日期，查询排班详细信息
	List<Schedule> getDetailSchedule(String hoscode, String depcode, String workDate);

	//获取排班可预约日期数据
	Map<String, Object> getBookingScheduleRule(int page, int limit, String hoscode, String depcode);

	//根据id获取排班
	Schedule getById(String id);

	//根据hos
	Schedule getScheduleByHosScheduleId(String id);

	//根据排班id获取预约下单数据
	ScheduleOrderVo getScheduleOrderVo(String scheduleId);

	/**
	 * 修改排班
	 */
	void update(Schedule schedule);
}
