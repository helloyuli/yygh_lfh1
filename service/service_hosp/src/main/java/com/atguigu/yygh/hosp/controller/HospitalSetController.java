package com.atguigu.yygh.hosp.controller;

import com.atguigu.yygh.common.utils.MD5;
import com.atguigu.yygh.common.result.Result;
import com.atguigu.yygh.hosp.service.HospitalSetService;
import com.atguigu.yygh.model.hosp.HospitalSet;
import com.atguigu.yygh.vo.hosp.HospitalSetQueryVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Random;

@Api(tags = "医院设置管理")
@RestController
@RequestMapping("/admin/hosp/hospitalSet")
public class HospitalSetController {


	/**
	 *responseBody底层是使用Jackson来完成对象到json的转换
	 *访问路径：http://localhost:8201/admin/hosp/hospitalSet/findAll
	 * 注入Service进行调用
	 */
	@Autowired
	private HospitalSetService hospitalSetService;


	/**
	 *查询医院设置表中的所有信息
	 * @return 医院设置列表
	 */
	@ApiOperation(value = "获取所有医院设置")
	@GetMapping("findAll")
	public Result findAllHospitalSet(){
		//调用service中的方法
		List<HospitalSet> list = hospitalSetService.list();
		return Result.ok(list);
	}

	/**
	 * 删除医院设置
	 * @param id 医院id
	 * @return 成功或失败
	 */
	@ApiOperation(value = "逻辑删除医院设置信息")
	@DeleteMapping("{id}")
	public Result removeHospSetById(@PathVariable("id") Long id){
		boolean flag = hospitalSetService.removeById(id);
		if (flag){
			return Result.ok();
		}else {
			return Result.fail();
		}
	}


	/**
	 * 条件查询带分页
	 * 注意：@PostMapping 才能获取@RequestBody传来的json数据
	 *      @RequestBody(required = false) 代表这个数据可以没有
	 * @param current:当前页
	 * @param limit：每页显示几条数据
	 * @param hospitalSetQueryVo：查询条件（医院编号、医院名称（模糊查询））
	 * @return
	 */
	@ApiOperation(value = "条件查询带分页")
	@PostMapping("findPageHospSet/{current}/{limit}")
	public Result findPageHospSet(@PathVariable("current") long current,
								  @PathVariable("limit") long limit,
								  @RequestBody(required = false) HospitalSetQueryVo hospitalSetQueryVo
								  ){

		//创建page对象，传递当前页，每页记录数
		Page<HospitalSet> page = new Page<>(current,limit);
		QueryWrapper<HospitalSet> wrapper = new QueryWrapper<>();
		if (!StringUtils.isEmpty(hospitalSetQueryVo)) {
			String hosname = hospitalSetQueryVo.getHosname();
			String hoscode = hospitalSetQueryVo.getHoscode();
			//构造条件
			if (!StringUtils.isEmpty(hosname)) {
				wrapper.like("hosname", hosname);
			}
			if (!StringUtils.isEmpty(hoscode)) {
				wrapper.eq("hoscode", hoscode);
			}
		}

		//调用方法实现分页的查询
		Page<HospitalSet> hospitalSetPage = hospitalSetService.page(page, wrapper);
		//返回结果
		return Result.ok(hospitalSetPage);
	}

	/**
	 *添加医院设置
	 * @param hospitalSet 入参对象
	 * @return 添加操作是否成功
	 */
	@PostMapping("saveHospitalSet")
	public Result saveHospitalSet(@RequestBody HospitalSet hospitalSet){
		//设置状态 1 使用 0 不使用
		hospitalSet.setStatus(1);
		//签名秘钥
		Random random = new Random();
		hospitalSet.setSignKey(MD5.encrypt(System.currentTimeMillis()+""+random.nextInt(1000)));
		//调用service
		boolean flag = hospitalSetService.save(hospitalSet);
		if (flag){
			return Result.ok();
		}else {
			return Result.fail();
		}

	}

	/**
	 *根据id获取医院设置
	 * @param id 医院id
	 * @return 医院设置信息
	 */
	@GetMapping("getHospitalSetById/{id}")
	public Result getHospitalSetById(@PathVariable("id") Long  id){
		HospitalSet hospitalSet = hospitalSetService.getById(id);
		return Result.ok(hospitalSet);
	}

	/**
	 *修改医院设置
	 * @param hospitalSet 修改后医院设置信息
	 * @return 修改的结果
	 */
	@PostMapping("updateHospitalSet")
	public Result updateHospitalSet(@RequestBody HospitalSet hospitalSet){
		boolean flag = hospitalSetService.updateById(hospitalSet);
		if (flag){
			return Result.ok();
		}else {
			return Result.fail();
		}
	}


	/**
	 * 批量删除医院设置
	 * @param idList 要删除的id集合
	 * @return 返回批量删除的结果
	 */
	@DeleteMapping("batchRemove")
	public Result batchRemoveHospitalSet(@RequestBody List<Long> idList){
		boolean flag = hospitalSetService.removeByIds(idList);
		if (flag){
			return Result.ok();
		}else {
			return Result.fail();
		}
	}


	/**
	 * 医院设置锁定和解锁的
	 * @param id 医院id
	 * @param status 该医院的状态
	 * @return
	 */
	@PutMapping("lockHospitalSet/{id}/{status}")
	public Result lockHospitalSet(@PathVariable Long id,
								  @PathVariable Integer status){
		//先根据id查询医院设置信息
		HospitalSet hospitalSet = hospitalSetService.getById(id);
		//设置状态
		hospitalSet.setStatus(status);
		boolean flag = hospitalSetService.updateById(hospitalSet);
		if (flag){
			return Result.ok();
		}else {
			return Result.fail();
		}

	}


	/**
	 * 发送签名秘钥
	 * @param id 医院id
	 * @return 结果是否成功
	 */
	@PutMapping("sendKey/{id}")
	public Result lockHospitalSet(@PathVariable Long id){
		//先根据id查询医院设置信息
		HospitalSet hospitalSet = hospitalSetService.getById(id);
		String signKey = hospitalSet.getSignKey();
		String hoscode = hospitalSet.getHoscode();
		//TODO 发送短信
		return Result.ok();
	}

}
