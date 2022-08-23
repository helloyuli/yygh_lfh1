package com.atguigu.yygh.hosp.service;

import com.atguigu.yygh.model.hosp.Hospital;
import com.atguigu.yygh.vo.hosp.HospitalQueryVo;
import com.atguigu.yygh.vo.hosp.HospitalSetQueryVo;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public interface HospitalService {
	void save(Map<String, Object> paramMap);

	//根据医院编号查询
	Hospital getByHoscode(String hosCode);

	//分页查询医院列表
	Page<Hospital> selectHospitalPage(Integer page, Integer limit, HospitalQueryVo hospitalQueryVo);

	//更新医院上线状态
	void updateStatus(String id, Integer status);

	//查看医院详情信息
	Map<String, Object> getHospById(String id);

	//根据医院编号获取医院名称
	String getHospName(String hoscode);

	//根据医院名称进行模糊查询
	List<Hospital> findByHosName(String hosname);

//	根据医院编号获取医院预约挂号详情
	Map<String, Object> item(String hoscode);
}
