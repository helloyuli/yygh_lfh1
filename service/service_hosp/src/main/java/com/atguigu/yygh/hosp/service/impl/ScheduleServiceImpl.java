package com.atguigu.yygh.hosp.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.yygh.common.exception.YyghException;
import com.atguigu.yygh.common.result.ResultCodeEnum;
import com.atguigu.yygh.hosp.repository.ScheduleRepository;
import com.atguigu.yygh.hosp.service.DepartmentService;
import com.atguigu.yygh.hosp.service.HospitalService;
import com.atguigu.yygh.hosp.service.ScheduleService;
import com.atguigu.yygh.model.hosp.BookingRule;
import com.atguigu.yygh.model.hosp.Department;
import com.atguigu.yygh.model.hosp.Hospital;
import com.atguigu.yygh.model.hosp.Schedule;
import com.atguigu.yygh.vo.hosp.BookingScheduleRuleVo;
import com.atguigu.yygh.vo.hosp.ScheduleOrderVo;
import com.atguigu.yygh.vo.hosp.ScheduleQueryVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.format.DateTimeFormat;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ScheduleServiceImpl implements ScheduleService {

	@Autowired
	private ScheduleRepository scheduleRepository;

	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private HospitalService hospitalService;

	@Autowired
	private DepartmentService departmentService;

	@Override
	public void save(Map<String, Object> paramMap) {
		String jsonString = JSONObject.toJSONString(paramMap);
		Schedule schedule = JSONObject.parseObject(jsonString, Schedule.class);
		//先查询数据库中是否有该schedule，有进行修改，没有进行添加
		Schedule scheduleExists = scheduleRepository.getScheduleByHoscodeAndHosScheduleId(schedule.getHoscode(),schedule.getHosScheduleId());

		if (scheduleExists!=null){
			scheduleExists.setUpdateTime(new Date());
			scheduleExists.setIsDeleted(0);
			//可约
			scheduleExists.setStatus(1);
			scheduleRepository.save(scheduleExists);
		}else {
			schedule.setCreateTime(new Date());
			schedule.setUpdateTime(new Date());
			schedule.setIsDeleted(0);
			//可约
			schedule.setStatus(1);
			scheduleRepository.save(schedule);
		}

	}

	@Override
	public Page<Schedule> findPageSchedule(int page, int limit, ScheduleQueryVo scheduleQueryVo) {
		Schedule schedule = new Schedule();
		BeanUtils.copyProperties(scheduleQueryVo,schedule);
		schedule.setStatus(1);
		schedule.setIsDeleted(0);

		ExampleMatcher matcher = ExampleMatcher.matching()
				.withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING)
				.withIgnoreCase(true);
		Example<Schedule> example = Example.of(schedule, matcher);

		Pageable pageable = PageRequest.of(page - 1, limit);
		Page<Schedule> all = scheduleRepository.findAll(example, pageable);
		return all;
	}

	@Override
	public void remove(String hoscode, String hosScheduleId) {
		//先查看数据库中是否有该排班信息
		Schedule schedule = scheduleRepository.getScheduleByHoscodeAndHosScheduleId(hoscode, hosScheduleId);
		if (schedule!=null){
			scheduleRepository.deleteById(schedule.getId());
		}
	}

	//根据医院编号和科室编号查询排版规则数据
	@Override
	public Map<String, Object> getRuleSchedule(long page, long limit, String hoscode, String depcode) {
		//根据医院编号和科室编号查询
		Object o;
		Criteria criteria = Criteria.where("hoscode").is(hoscode).and("depcode").is(depcode);
		//根据工作日workDate日期进行分组
		Aggregation agg = Aggregation.newAggregation(
				Aggregation.match(criteria),//匹配条件
				Aggregation.group("workDate")//分组字段
				.first("workDate").as("workDate")//分组之后的名字
				//统计号源数量
				.count().as("docCount")// as("docCount")起的名字
				.sum("reservedNumber").as("reservedNumber")//sum函数做运算
				.sum("availableNumber").as("availableNumber"),
				//排序
				Aggregation.sort(Sort.Direction.DESC,"workDate"),
				//实现分页
				Aggregation.skip((page-1)*limit),//起始页
				Aggregation.limit(limit)//每页显示几条
		);
		//调用方法最终去执行
		AggregationResults<BookingScheduleRuleVo> aggResult = mongoTemplate.aggregate(agg, Schedule.class, BookingScheduleRuleVo.class);
		List<BookingScheduleRuleVo> bookingScheduleRuleVoList = aggResult.getMappedResults();
		//分组查询的总记录数
		Aggregation totalsAgg = Aggregation.newAggregation(
				Aggregation.match(criteria),
				Aggregation.group("workDate")
		);
		AggregationResults<BookingScheduleRuleVo> totalAggregate =
				mongoTemplate.aggregate(totalsAgg, Schedule.class, BookingScheduleRuleVo.class);
		int total = totalAggregate.getMappedResults().size();

		//把日期对应的星期获取
		for (BookingScheduleRuleVo bookingScheduleRuleVo : bookingScheduleRuleVoList){
			bookingScheduleRuleVo.setDayOfWeek(getDayOfWeek(new DateTime(bookingScheduleRuleVo.getWorkDate())));
		}
		//获取医院名称
		String hosName = hospitalService.getHospName(hoscode);
		//其他基础数据
		HashMap<String, String> baseMap = new HashMap<>();
		baseMap.put("hosname",hosName);


		Map<String, Object> result = new HashMap<>();
		result.put("bookingScheduleRuleList",bookingScheduleRuleVoList);
		result.put("total",total);
		result.put("baseMap",baseMap);
		return result;
	}

	//根据医院编号、科室编号和工作日期，查询排班详细信息
	@Override
	public List<Schedule> getDetailSchedule(String hoscode, String depcode, String workDate) {
		Date date = new DateTime(workDate).toDate();
		List<Schedule> scheduleList = scheduleRepository.findScheduleByHoscodeAndDepcodeAndWorkDate(hoscode,depcode, date);
		//把得到的list集合进行遍历，包装
		scheduleList.forEach(this::packageSchedule);
		return scheduleList;
	}

	//封装排班中其他的值
	private void packageSchedule(Schedule schedule) {
		//设置医院名称
		schedule.getParam().put("hosname",hospitalService.getHospName(schedule.getHoscode()));
		//设置科室名称
		schedule.getParam().put("depname",departmentService.getDepName(schedule.getHoscode(),schedule.getDepcode()));
		//设置日期对应的星期
		schedule.getParam().put("dayOfWeek",getDayOfWeek(new DateTime(schedule.getWorkDate())));
	}

	@Override
	public Map<String, Object> getBookingScheduleRule(int page, int limit, String hoscode, String depcode) {
		Map<String,Object> result = new HashMap<>();
		//根据医院编号获取预约规则
		Hospital hospital = hospitalService.getByHoscode(hoscode);
		if(hospital == null){
			throw new YyghException(ResultCodeEnum.DATA_ERROR);
		}
		BookingRule bookingRule = hospital.getBookingRule();

		//根据预约规则获取可预约数据（分页）
		IPage iPage = this.getListDate(page,limit,bookingRule);
		//获取当前可预约日期
		List<Date> dateList = iPage.getRecords();
		//获取可预约日期里面科室的剩余数据
		Criteria criteria = Criteria.where("hoscode").is(hoscode).and("depcode").is(depcode).and("workDate").in(dateList);
		Aggregation agg = Aggregation.newAggregation(
				Aggregation.match(criteria),
				Aggregation.group("workDate").first("workDate").as("workDate")
						.count().as("docCount")
						.sum("availableNumber").as("availableNumber")
						.sum("reservedNumber").as("reservedNumber")
		);

		AggregationResults<BookingScheduleRuleVo> aggregateResult = mongoTemplate.aggregate(agg, Schedule.class, BookingScheduleRuleVo.class);
		List<BookingScheduleRuleVo> scheduleVoList = aggregateResult.getMappedResults();

		//合并数据  map集合 key：日期  value：预约规则和剩余数量等
		Map<Date, BookingScheduleRuleVo> scheduleVoMap = new HashMap<>();
		if(!CollectionUtils.isEmpty(scheduleVoList)) {
			scheduleVoMap = scheduleVoList.stream().collect(Collectors.toMap(BookingScheduleRuleVo::getWorkDate, BookingScheduleRuleVo -> BookingScheduleRuleVo));
		}

		//获取可预约排版规则
		List<BookingScheduleRuleVo> bookingScheduleRuleVoList = new ArrayList<>();
		for(int i=0, len=dateList.size(); i<len; i++) {
			Date date = dateList.get(i);
			//从map集合中根据key日期获取value值
			BookingScheduleRuleVo bookingScheduleRuleVo = scheduleVoMap.get(date);
			if(bookingScheduleRuleVo == null){
				bookingScheduleRuleVo = new BookingScheduleRuleVo();
				//就诊医生人数
				bookingScheduleRuleVo.setDocCount(0);
				//科室剩余预约数  -1表示无号
				bookingScheduleRuleVo.setAvailableNumber(-1);
			}

			bookingScheduleRuleVo.setWorkDate(date);
			bookingScheduleRuleVo.setWorkDateMd(date);
			//计算当前预约日期对应的星期
			String dayOfWeek = this.getDayOfWeek(new DateTime(date));
			bookingScheduleRuleVo.setDayOfWeek(dayOfWeek);

			//最后一页最后一条记录为即将预约   状态 0：正常 1：即将放号 -1：当天已停止挂号
			if(i == len-1 && page == iPage.getPages()) {
				bookingScheduleRuleVo.setStatus(1);
			} else {
				bookingScheduleRuleVo.setStatus(0);
			}
			//当天预约如果过了停号时间， 不能预约
			if(i == 0 && page == 1) {
				DateTime stopTime = this.getDateTime(new Date(), bookingRule.getStopTime());
				if(stopTime.isBeforeNow()) {
					//停止预约
					bookingScheduleRuleVo.setStatus(-1);
				}
			}
			bookingScheduleRuleVoList.add(bookingScheduleRuleVo);
		}
		//可预约日期规则数据
		result.put("bookingScheduleList", bookingScheduleRuleVoList);
		result.put("total", iPage.getTotal());
		//其他基础数据
		Map<String, String> baseMap = new HashMap<>();
		//医院名称
		baseMap.put("hosname", hospitalService.getHospName(hoscode));
		//科室
		Department department =departmentService.getDepartment(hoscode, depcode);
		//大科室名称
		baseMap.put("bigname", department.getBigname());
		//科室名称
		baseMap.put("depname", department.getDepname());
		//月
		baseMap.put("workDateString", new DateTime().toString("yyyy年MM月"));
		//放号时间
		baseMap.put("releaseTime", bookingRule.getReleaseTime());
		//停号时间
		baseMap.put("stopTime", bookingRule.getStopTime());
		result.put("baseMap", baseMap);
		return result;
	}

	@Override
	public Schedule getById(String id) {
		Schedule schedule = scheduleRepository.findById(id).get();
		packageSchedule(schedule);
		return schedule;
	}

	@Override
	public Schedule getScheduleByHosScheduleId(String id){
		Schedule schedule = scheduleRepository.getScheduleByHosScheduleId(id);
		packageSchedule(schedule);
		return schedule;
	}



	//根据排班id获取预约下单数据
	@Override
	public ScheduleOrderVo getScheduleOrderVo(String scheduleId) {
		ScheduleOrderVo scheduleOrderVo = new ScheduleOrderVo();
		//排班信息
		Schedule schedule = scheduleRepository.findById(scheduleId).get();
		if(null == schedule) {
			throw new YyghException(ResultCodeEnum.PARAM_ERROR);
		}

		//获取预约规则信息
		Hospital hospital = hospitalService.getByHoscode(schedule.getHoscode());
		if(null == hospital) {
			throw new YyghException(ResultCodeEnum.DATA_ERROR);
		}
		BookingRule bookingRule = hospital.getBookingRule();
		if(null == bookingRule) {
			throw new YyghException(ResultCodeEnum.PARAM_ERROR);
		}

		scheduleOrderVo.setHoscode(schedule.getHoscode());
		scheduleOrderVo.setHosname(hospitalService.getHospName(schedule.getHoscode()));
		scheduleOrderVo.setDepcode(schedule.getDepcode());
		scheduleOrderVo.setDepname(departmentService.getDepName(schedule.getHoscode(), schedule.getDepcode()));
		scheduleOrderVo.setHosScheduleId(schedule.getHosScheduleId());
		scheduleOrderVo.setAvailableNumber(schedule.getAvailableNumber());
		scheduleOrderVo.setTitle(schedule.getTitle());
		scheduleOrderVo.setReserveDate(schedule.getWorkDate());
		scheduleOrderVo.setReserveTime(schedule.getWorkTime());
		scheduleOrderVo.setAmount(schedule.getAmount());

		//退号截止天数（如：就诊前一天为-1，当天为0）
		int quitDay = bookingRule.getQuitDay();
		DateTime quitTime = this.getDateTime(new DateTime(schedule.getWorkDate()).plusDays(quitDay).toDate(), bookingRule.getQuitTime());
		scheduleOrderVo.setQuitTime(quitTime.toDate());

		//预约开始时间
		DateTime startTime = this.getDateTime(new Date(), bookingRule.getReleaseTime());
		scheduleOrderVo.setStartTime(startTime.toDate());

		//预约截止时间
		DateTime endTime = this.getDateTime(new DateTime().plusDays(bookingRule.getCycle()).toDate(), bookingRule.getStopTime());
		scheduleOrderVo.setEndTime(endTime.toDate());

		//当天停止挂号时间
		DateTime stopTime = this.getDateTime(new Date(), bookingRule.getStopTime());
		scheduleOrderVo.setStartTime(startTime.toDate());
		return scheduleOrderVo;
	}

	@Override
	public void update(Schedule schedule) {
		schedule.setUpdateTime(new Date());
		//主键一致就是更新
		scheduleRepository.save(schedule);
	}

	/**
	 * 获取可预约日期分页数据
	 */
	private IPage<Date> getListDate(int page, int limit, BookingRule bookingRule) {
		//当天放号时间
		DateTime releaseTime = this.getDateTime(new Date(), bookingRule.getReleaseTime());
		//预约周期
		int cycle = bookingRule.getCycle();
		//如果当天放号时间已过，则预约周期后一天为即将放号时间，周期加1
		if(releaseTime.isBeforeNow()) cycle += 1;
		//可预约所有日期，最后一天显示即将放号倒计时
		List<Date> dateList = new ArrayList<>();
		for (int i = 0; i < cycle; i++) {
			//计算当前预约日期
			DateTime curDateTime = new DateTime().plusDays(i);
			String dateString = curDateTime.toString("yyyy-MM-dd");
			dateList.add(new DateTime(dateString).toDate());
		}
		//日期分页，由于预约周期不一样，页面一排最多显示7天数据，多了就要分页显示
		List<Date> pageDateList = new ArrayList<>();
		int start = (page-1)*limit;
		int end = (page-1)*limit+limit;
		if(end >dateList.size()) end = dateList.size();
		for (int i = start; i < end; i++) {
			pageDateList.add(dateList.get(i));
		}
		IPage<Date> iPage = new com.baomidou.mybatisplus.extension.plugins.pagination.Page(page, 7, dateList.size());
		iPage.setRecords(pageDateList);
		return iPage;
	}
	/**
	 * 将Date日期（yyyy-MM-dd HH:mm）转换为DateTime
	 */
	private DateTime getDateTime(Date date, String timeString) {
		String dateTimeString = new DateTime(date).toString("yyyy-MM-dd") + " "+ timeString;
		DateTime dateTime = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm").parseDateTime(dateTimeString);
		return dateTime;
	}

	/**
	 * 根据日期获取周几数据
	 * @param dateTime
	 * @return
	 */
	private String getDayOfWeek(DateTime dateTime) {
		String dayOfWeek = "";
		switch (dateTime.getDayOfWeek()) {
			case DateTimeConstants.SUNDAY:
				dayOfWeek = "周日";
				break;
			case DateTimeConstants.MONDAY:
				dayOfWeek = "周一";
				break;
			case DateTimeConstants.TUESDAY:
				dayOfWeek = "周二";
				break;
			case DateTimeConstants.WEDNESDAY:
				dayOfWeek = "周三";
				break;
			case DateTimeConstants.THURSDAY:
				dayOfWeek = "周四";
				break;
			case DateTimeConstants.FRIDAY:
				dayOfWeek = "周五";
				break;
			case DateTimeConstants.SATURDAY:
				dayOfWeek = "周六";
			default:
				break;
		}
		return dayOfWeek;
	}
}
