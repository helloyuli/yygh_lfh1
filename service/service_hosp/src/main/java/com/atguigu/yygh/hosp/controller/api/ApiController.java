package com.atguigu.yygh.hosp.controller.api;

import com.atguigu.yygh.common.exception.YyghException;
import com.atguigu.yygh.common.helper.HttpRequestHelper;
import com.atguigu.yygh.common.result.Result;
import com.atguigu.yygh.common.result.ResultCodeEnum;
import com.atguigu.yygh.common.utils.MD5;
import com.atguigu.yygh.hosp.service.DepartmentService;
import com.atguigu.yygh.hosp.service.HospitalService;
import com.atguigu.yygh.hosp.service.HospitalSetService;
import com.atguigu.yygh.hosp.service.ScheduleService;
import com.atguigu.yygh.model.hosp.Department;
import com.atguigu.yygh.model.hosp.Hospital;
import com.atguigu.yygh.model.hosp.Schedule;
import com.atguigu.yygh.vo.hosp.DepartmentQueryVo;
import com.atguigu.yygh.vo.hosp.ScheduleQueryVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/api/hosp")
public class ApiController {

	@Autowired
	private HospitalService hospitalService;

	@Autowired
	private HospitalSetService hospitalSetService;

	@Autowired
	private DepartmentService departmentService;

	@Autowired
	private ScheduleService scheduleService;

	//删除排班接口
	@PostMapping("schedule/remove")
	public Result removeSchedule(HttpServletRequest request){
		//获取到传递过来排班的信息
		Map<String, String[]> requestMap = request.getParameterMap();
		Map<String, Object> paramMap = HttpRequestHelper.switchMap(requestMap);
		//获取医院编号和排班编号
		String hoscode = verifySignCode(paramMap);
		String hosScheduleId = (String) paramMap.get("hosScheduleId");

		scheduleService.remove(hoscode,hosScheduleId);
		return Result.ok();

	}

	//查询医院排班的接口
	@PostMapping("schedule/list")
	public Result findSchedule(HttpServletRequest request){
		//获取到传递过来的信息
		Map<String, String[]> requestMap = request.getParameterMap();
		Map<String, Object> paramMap = HttpRequestHelper.switchMap(requestMap);

		//获取当前页和每页记录数
		int page = StringUtils.isEmpty(paramMap.get("page")) ? 1 : Integer.parseInt((String) paramMap.get("page"));
		int limit = StringUtils.isEmpty(paramMap.get("limit")) ? 1 : Integer.parseInt((String) paramMap.get("limit"));
		//签名校验
		String hoscode = verifySignCode(paramMap);
		String depcode = (String) paramMap.get("depcode");
		ScheduleQueryVo scheduleQueryVo = new ScheduleQueryVo();
		scheduleQueryVo.setHoscode(hoscode);
		scheduleQueryVo.setDepcode(depcode);
		Page<Schedule> schedulesModel = scheduleService.findPageSchedule(page,limit,scheduleQueryVo);
		return Result.ok(schedulesModel);
	}

	//上传排班的接口
	@PostMapping("saveSchedule")
	public Result saveSchedule(HttpServletRequest request){
		//获取到传递过来的排班信息
		Map<String, String[]> requestMap = request.getParameterMap();
		Map<String, Object> paramMap = HttpRequestHelper.switchMap(requestMap);
		//签名校验
		verifySignCode(paramMap);

		scheduleService.save(paramMap);

		return Result.ok();
	}

	//删除科室接口
	@PostMapping("department/remove")
	public Result removeDepartment(HttpServletRequest request){
		//获取到传递过来的科室信息
		Map<String, String[]> requestMap = request.getParameterMap();
		Map<String, Object> paramMap = HttpRequestHelper.switchMap(requestMap);

		//获取医院的编号
		String hoscode = (String) paramMap.get("hoscode");
		//获取科室编号
		String depcode = (String) paramMap.get("depcode");
		//签名校验
		verifySignCode(paramMap);
		//删除科室接口
		departmentService.remove(hoscode,depcode);

		return Result.ok();
	}

	//查询科室接口
	@PostMapping("department/list")
	public Result findDepartment(HttpServletRequest request){
		//获取到传递过来的科室信息
		Map<String, String[]> requestMap = request.getParameterMap();
		Map<String, Object> paramMap = HttpRequestHelper.switchMap(requestMap);
		//获取医院的编号
//		String hoscode = (String) paramMap.get("hoscode");
		//获取当前页和每页记录数
		int page = StringUtils.isEmpty(paramMap.get("page")) ? 1 : Integer.parseInt((String) paramMap.get("page"));
		int limit = StringUtils.isEmpty(paramMap.get("limit")) ? 1 : Integer.parseInt((String) paramMap.get("limit"));
		//签名校验
		String hoscode = verifySignCode(paramMap);

		DepartmentQueryVo departmentQueryVo = new DepartmentQueryVo();
		departmentQueryVo.setHoscode(hoscode);

		Page<Department> pageModel = departmentService.findPageDepartment(page,limit,departmentQueryVo);

		return Result.ok(pageModel);
	}

	//上传科室接口
	@PostMapping("saveDepartment")
	public Result saveDepartment(HttpServletRequest request){
		//获取到传递过来的科室信息
		Map<String, String[]> requestMap = request.getParameterMap();
		Map<String, Object> paramMap = HttpRequestHelper.switchMap(requestMap);
		verifySignCode(paramMap);
		//调用service里面的方法
		departmentService.save(paramMap);
		return Result.ok();
	}


	//查询医院的接口
	@PostMapping("hospital/show")
	public Result getHospital(HttpServletRequest request){
		//获取到传递过来的医院信息
		Map<String, String[]> requestMap = request.getParameterMap();
		Map<String, Object> paramMap = HttpRequestHelper.switchMap(requestMap);

		//获取request的信息返回hosCode
		String hosCode = verifySignCode(paramMap);

		//调用service方法实现根据医院编号查询
		Hospital hospital = hospitalService.getByHoscode(hosCode);
		return Result.ok(hospital);
	}

	//验证签名，返回hosCode
	private String verifySignCode(Map<String, Object> paramMap) {

		//获取传递过来的医院编号
		String hosCode = (String) paramMap.get("hoscode");
		//获取医院系统传递过来的签名,签名是进行MD5的加密
		String hospSign = (String) paramMap.get("sign");
		//根据传递过来的医院编码，查询数据库，查询签名
		String signKey = hospitalSetService.getSignKey(hosCode);
//		//把数据库查询签名进行MD5加密
//		String signKeyMD5 = MD5.encrypt(signKey);
		//判断签名是否一致
		if (!hospSign.equals(signKey)){
			throw new YyghException(ResultCodeEnum.SIGN_ERROR);
		}
		return hosCode;
	}

	//上传医院接口
	@PostMapping("saveHospital")
	public Result saveHosp(HttpServletRequest request){
		//获取到传递过来的医院的信息
		Map<String, String[]> requestMap = request.getParameterMap();
		Map<String, Object> paramMap = HttpRequestHelper.switchMap(requestMap);
		verifySignCode(paramMap);
		//传输过程中“+”转换成了“ ”,因此我们需要转换过来
		String logoData = (String) paramMap.get("logoData");
		logoData = logoData.replaceAll(" ","+");
		paramMap.put("logoData",logoData);
		//调用service中的方法
		hospitalService.save(paramMap);
		return Result.ok();
	}
}
