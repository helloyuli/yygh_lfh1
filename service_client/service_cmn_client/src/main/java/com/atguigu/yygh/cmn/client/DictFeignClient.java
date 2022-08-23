package com.atguigu.yygh.cmn.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient("service-cmn")
@Component
public interface DictFeignClient {

	//根据dictcode和value查询（查询医院）
	@GetMapping("/admin/cmn/dict/getName/{dictCode}/{value}")
	String getName(@PathVariable("dictCode") String dictCode,
						  @PathVariable("value") String value);


	//根据value查询（查询地区）
	@GetMapping("/admin/cmn/dict/getName/{value}")
	String getName(@PathVariable("value") String value);
}
