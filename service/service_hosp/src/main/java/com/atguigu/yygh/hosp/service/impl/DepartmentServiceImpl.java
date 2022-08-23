package com.atguigu.yygh.hosp.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.yygh.hosp.repository.DepartmentRepository;
import com.atguigu.yygh.hosp.service.DepartmentService;
import com.atguigu.yygh.model.hosp.Department;
import com.atguigu.yygh.vo.hosp.DepartmentQueryVo;
import com.atguigu.yygh.vo.hosp.DepartmentVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DepartmentServiceImpl implements DepartmentService {

	@Autowired
	private DepartmentRepository departmentRepository;

	//上传科室接口
	@Override
	public void save(Map<String, Object> paramMap) {
		//把paramMap转换为Department对象
		String jsonString = JSONObject.toJSONString(paramMap);
		Department department = JSONObject.parseObject(jsonString,Department.class);
		//根据医院编号和科室编号进行查询
		Department departmentExist = departmentRepository.getDepartmentByHoscodeAndDepcode(department.getHoscode(),department.getDepcode());
		//判断,不为空进行修改，为空进行添加
		if (departmentExist!=null){
			departmentExist.setUpdateTime(new Date());
			departmentExist.setIsDeleted(0);
			departmentRepository.save(departmentExist);
		}else {
			department.setCreateTime(new Date());
			department.setUpdateTime(new Date());
			departmentRepository.save(department);
		}
	}

	@Override
	public Page<Department> findPageDepartment(Integer page, Integer limit, DepartmentQueryVo departmentQueryVo) {
		//创建pageable对象，设置当前页和每页记录数 因为PageRequest里的0代表的是第一页，所以要减一
		Pageable pageable = PageRequest.of(page-1, limit);
		//创建Example对象(模糊查询+忽略大小写)
		ExampleMatcher matcher  = ExampleMatcher.matching()
				.withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING)
				.withIgnoreCase(true);

		Department department = new Department();
		BeanUtils.copyProperties(departmentQueryVo,department);
//		department.setIsDeleted(0);

		Example<Department> example = Example.of(department,matcher);
		Page<Department> result = departmentRepository.findAll(example, pageable);
		return result;
	}

	@Override
	public void remove(String hoscode, String depcode) {
		//根据医院编号和科室编号查询出科室信息
		Department department = departmentRepository.getDepartmentByHoscodeAndDepcode(hoscode, depcode);
		if (department==null){
			return;
		}
		departmentRepository.deleteById(department.getId());
	}

	//根据医院编号查询医院所有科室列表
	@Override
	public List<DepartmentVo> findDeptTree(String hoscode) {
		//创建list集合，用于最终数据的封装
		List<DepartmentVo> result = new ArrayList<>();
		//根据医院编号查询医院所有科室信息
		Department departmentQuery = new Department();
		departmentQuery.setHoscode(hoscode);
		Example example = Example.of(departmentQuery);
		//所有科室列表信息
		List<Department> departmentList = departmentRepository.findAll(example);
		Map<String, List<Department>> departmentMap =
				departmentList.stream().collect(Collectors.groupingBy(Department::getBigcode));
		//遍历map集合 departmentMap
		for (Map.Entry<String, List<Department>> entry : departmentMap.entrySet()) {
			//大科室编号
			String bigcode = entry.getKey();
			//大科室编号对应的全部数据
			List<Department> bigdepartment = entry.getValue();
			//开始封装
			DepartmentVo departmentVo = new DepartmentVo();
			//封装大科室
			departmentVo.setDepcode(bigcode);
			departmentVo.setDepname(bigdepartment.get(0).getDepname());
			//封装小科室
			List<DepartmentVo> children = new ArrayList<>();
			for (Department department : bigdepartment){
				DepartmentVo departmentVo1 = new DepartmentVo();
				departmentVo1.setDepcode(department.getDepcode());
				departmentVo1.setDepname(department.getDepname());
				//封装到list集合中去
				children.add(departmentVo1);
			}
			departmentVo.setChildren(children);

			result.add(departmentVo);
		}

		return result;
	}

	@Override
	public String getDepName(String hoscode, String depcode) {
		Department department = departmentRepository.getDepartmentByHoscodeAndDepcode(hoscode, depcode);
		if (department!=null){
			return department.getDepname();
		}
		return null;
	}

	@Override
	public Department getDepartment(String hoscode, String depcode) {
		Department department = departmentRepository.getDepartmentByHoscodeAndDepcode(hoscode, depcode);
		if (department!=null){
			return department;
		}
		return null;
	}
}
