package com.atguigu.yygh.user.service.impl;

import com.atguigu.yygh.cmn.client.DictFeignClient;
import com.atguigu.yygh.enums.DictEnum;
import com.atguigu.yygh.model.user.Patient;
import com.atguigu.yygh.user.mapper.PatientMapper;
import com.atguigu.yygh.user.service.PatientService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class PatientServiceImpl extends ServiceImpl<PatientMapper, Patient> implements PatientService {

	@Autowired
	private DictFeignClient dictFeignClient;

	@Override
	public List<Patient> findAllByUserId(Long userId) {
		QueryWrapper<Patient> wrapper = new QueryWrapper<>();
		wrapper.eq("user_id",userId);
		List<Patient> patientList = baseMapper.selectList(wrapper);
		//通过远程调用得到编码对应的具体内容，查询数据字典表中的内容
		//做其他参数的封装
		patientList.forEach(this::packagePatient);
		return patientList;
	}

	@Override
	public Patient getByPatientId(Long id) {
		Patient patient = getById(id);
		packagePatient(patient);
		return patient;
	}

	private void packagePatient(Patient patient) {
		String certificatesTypeString =
		dictFeignClient.getName(DictEnum.CERTIFICATES_TYPE.getDictCode(), patient.getCertificatesType());//联系人证件
		//联系人证件类型
		String contactsCertificatesTypeString =
		dictFeignClient.getName(DictEnum.CERTIFICATES_TYPE.getDictCode(),patient.getContactsCertificatesType());
		//省
		String provinceString = dictFeignClient.getName(patient.getProvinceCode());
		//市
		String cityString = dictFeignClient.getName(patient.getCityCode());
		//区
		String districtString = dictFeignClient.getName(patient.getDistrictCode());
		patient.getParam().put("certificatesTypeString", certificatesTypeString);
		patient.getParam().put("contactsCertificatesTypeString", contactsCertificatesTypeString);
		patient.getParam().put("provinceString", provinceString);
		patient.getParam().put("cityString", cityString);
		patient.getParam().put("districtString", districtString);
		patient.getParam().put("fullAddress", provinceString + cityString + districtString + patient.getAddress());
	}
}
