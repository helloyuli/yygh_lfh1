package com.atguigu.yygh.oss.service.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.PutObjectResult;
import com.atguigu.yygh.oss.service.FileService;
import com.atguigu.yygh.oss.utils.ConstantOssPropertiesUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

@Service
public class FileServiceImpl implements FileService {


	//上传文件到阿里云oss
	@Override
	public String upload(MultipartFile file) {
		// yourEndpoint填写Bucket所在地域对应的Endpoint。以华东1（杭州）为例，Endpoint填写为https://oss-cn-hangzhou.aliyuncs.com。
		String endpoint = ConstantOssPropertiesUtils.EDNPOINT;
		// 阿里云账号AccessKey拥有所有API的访问权限，风险很高。强烈建议您创建并使用RAM用户进行API访问或日常运维，请登录RAM控制台创建RAM用户。
		String accessKeyId = ConstantOssPropertiesUtils.ACCESS_KEY_ID;
		String accessKeySecret = ConstantOssPropertiesUtils.SECRECT;
		// 填写Bucket名称，例如examplebucket。
		String bucketName = ConstantOssPropertiesUtils.BUCKET;

		// 创建OSSClient实例。
		OSS ossClient = null;
		// 创建存储空间。
		try {
			ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
			InputStream inputStream = file.getInputStream();
			String fileName = new DateTime().toString("yyyy-MM-dd")+"/"+UUID.randomUUID().toString().replaceAll("-","")+""+file.getOriginalFilename();
			ossClient.putObject(bucketName, fileName, inputStream);

			String url = "https://"+bucketName+"."+endpoint+"/"+fileName;
			return url;
		} catch (IOException e) {
			e.printStackTrace();
		}finally {
			// 关闭OSSClient。
			ossClient.shutdown();
		}
		return null;
	}
}
