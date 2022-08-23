package com.atguigu.yygh.cmn.service;


import com.atguigu.yygh.model.cmn.Dict;
import com.atguigu.yygh.model.hosp.HospitalSet;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @author Administrator
 */
public interface DictService extends IService<Dict> {

	/**
	 * 根据id查询子数据列表
	 * @param id
	 * @return
	 */
	List<Dict> findChildData(Long id);

	/**
	 * 导出数据字典接口
	 * @param response 响应输出流
	 */
	void exportDictData(HttpServletResponse response);

	/**
	 * 导入数据字典接口
	 * @param multipartFile Excel文件
	 */
	void importDictData(MultipartFile multipartFile);

	/**
	 * 根据dictcode和value查询
	 * @param dictCode
	 * @param value
	 * @return
	 */
	String getDictName(String dictCode, String value);

	/**
	 * //根据dictCode获取下级节点
	 * @param dictCode
	 * @return
	 */
	List<Dict> findByDictCode(String dictCode);
}
