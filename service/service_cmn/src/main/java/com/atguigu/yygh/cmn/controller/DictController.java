package com.atguigu.yygh.cmn.controller;

import com.atguigu.yygh.cmn.service.DictService;
import com.atguigu.yygh.common.result.Result;
import com.atguigu.yygh.model.cmn.Dict;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

@Api(value = "数据字典接口")
@RestController
@RequestMapping("/admin/cmn/dict")
public class DictController {

	@Autowired
	private DictService dictService;



	/**
	 * 导入数据
	 * @param file Excel文件
	 * @return 响应结果
	 */
	@PostMapping("importData")
	//@CacheEvict(value = "dict", allEntries=true)//修改更新缓存
	public Result importDict(MultipartFile file){
		if (file!=null){
			dictService.importDictData(file);
			return Result.ok();
		}
		System.out.println("multipartFile 为空");
		return Result.fail();
	}

	/**
	 * 导出数据字典接口
	 * @param response 响应输出流，将Excel返回
	 * @return 导出结果
	 */
	@GetMapping("exportData")
	public void exportDict(HttpServletResponse response){
		dictService.exportDictData(response);
	}

	/**
	 * 根据数据id查询它下面的子数据列表
	 * @param id 数据id
	 * @return 子数据列表
	 */
	@GetMapping("findChildData/{id}")
	//@Cacheable(value = "dict",keyGenerator = "keyGenerator")//将第一次查询结果放入缓存当中
	public Result findChildData(@PathVariable("id") Long id){
		List<Dict> list = dictService.findChildData(id);
		return Result.ok(list);
	}

	//根据dictcode和value查询（查询医院）
	@GetMapping("getName/{dictCode}/{value}")
	public String getName(@PathVariable("dictCode") String dictCode,
						  @PathVariable("value") String value){
		String dictName = dictService.getDictName(dictCode,value);
		return dictName;
	}
	//根据value查询（查询地区）
	@GetMapping("getName/{value}")
	public String getName(@PathVariable("value") String value){
		String dictName = dictService.getDictName("",value);
		return dictName;
	}

	//根据dictCode获取下级节点
	@ApiOperation(value="根据dictCode获取下级节点")
	@GetMapping("findByDictCode/{dictCode}")
	public Result<List<Dict>> findByDictCode(@PathVariable("dictCode") String dictCode){
		List<Dict> list = dictService.findByDictCode(dictCode);
		return Result.ok(list);
	}
}
