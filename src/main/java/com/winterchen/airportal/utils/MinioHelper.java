package com.winterchen.airportal.utils;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.UUID;
import com.winterchen.airportal.base.FileUploadResult;
import com.winterchen.airportal.configuration.MinioProperties;
import io.minio.*;
import io.minio.errors.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

/**
 * @author winterchen
 * @version 1.0
 * @date 2022/4/12 10:21
 * @description minio 操作类
 **/
@Slf4j
@Component
public class MinioHelper {

    @Autowired
    private MinioClient client;

    @Autowired
    public MinioProperties minioProperties;


    /**
     * 上传单个文件
     * @param multipartFile
     * @return
     */
    public FileUploadResult uploadFile(MultipartFile multipartFile) throws IOException, ServerException, InsufficientDataException, ErrorResponseException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {

        boolean found = client.bucketExists(BucketExistsArgs.builder().bucket(minioProperties.getBucketName()).build());
        if (!found) {
            log.info("create bucket: [{}]", minioProperties.getBucketName());
            client.makeBucket(MakeBucketArgs.builder().bucket(minioProperties.getBucketName()).build());
        } else {
            log.info("bucket '{}' already exists.", minioProperties.getBucketName());
        }

        try (InputStream inputStream = multipartFile.getInputStream()) {

            // 上传文件的名称
            String uploadName =  UUID.fastUUID().toString(true) + "_" + DateUtil.format(new Date(), "yyyy_MM_dd_HH_mm_ss") + "_" +
                    multipartFile.getOriginalFilename().substring(multipartFile.getOriginalFilename().lastIndexOf("."));

            // PutObjectOptions，上传配置(文件大小，内存中文件分片大小)
            PutObjectArgs putObjectOptions = PutObjectArgs.builder()
                    .bucket(minioProperties.getBucketName())
                    .object(uploadName)
                    .contentType(multipartFile.getContentType())
                    .stream(inputStream, multipartFile.getSize(), -1)
                    .build();
            client.putObject(putObjectOptions);

            final String url = minioProperties.getEndpoint() + "/" + minioProperties.getBucketName() + "/" + UriUtils.encode(uploadName, StandardCharsets.UTF_8);

            // 返回访问路径
            return FileUploadResult.builder()
                    .uploadName(uploadName)
                    .url(url)
                    .realName(multipartFile.getOriginalFilename())
                    .size(multipartFile.getSize())
                    .bucket(minioProperties.getBucketName())
                    .build();
        }
    }

    public void removeFile(String fileName) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        client.removeObject(RemoveObjectArgs.builder()
                .bucket(minioProperties.getBucketName())
                .object(fileName)
                .build());
    }

    /**
     * 文件下载
     * @param response
     * @param fileName
     */
    public void download(HttpServletResponse response, String fileName, String realName) throws Exception {
        InputStream in=null;
        try {
            //获取文件对象 stat原信息
            StatObjectResponse stat =client.statObject(StatObjectArgs.builder().bucket(minioProperties.getBucketName()).object(fileName).build());
            response.setContentType(stat.contentType());
            response.setHeader("Content-disposition", "attachment;filename="+new String(realName.getBytes("gb2312"), "ISO8859-1" ));
            in =   client.getObject(GetObjectArgs.builder().bucket(minioProperties.getBucketName()).object(fileName).build());
            IOUtils.copy(in,response.getOutputStream());
        }finally {
            if(in!=null){
                try {
                    in.close();
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                }
            }
        }
    }



}