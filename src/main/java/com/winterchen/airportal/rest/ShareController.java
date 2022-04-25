package com.winterchen.airportal.rest;

/**
 * @author winterchen
 * @version 1.0
 * @date 2022/4/13 10:16
 * @description TODO
 **/

import com.winterchen.airportal.annotation.NotLoginAccess;
import com.winterchen.airportal.enums.UploadType;
import com.winterchen.airportal.request.CompleteMultipartUploadRequest;
import com.winterchen.airportal.request.MultipartUploadCreateRequest;
import com.winterchen.airportal.response.FileInfoResponse;
import com.winterchen.airportal.response.MultipartUploadCreateResponse;
import com.winterchen.airportal.response.ShareResponse;
import com.winterchen.airportal.service.FileUploadService;
import com.winterchen.airportal.service.ShareService;
import io.swagger.annotations.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

@Api(tags = "分享管理")
@RequestMapping("/share")
@RestController
public class ShareController {


    private final ShareService shareService;

    private final FileUploadService fileUploadService;

    public ShareController(ShareService shareService, FileUploadService fileUploadService) {
        this.shareService = shareService;
        this.fileUploadService = fileUploadService;
    }

    @ApiImplicitParams({@ApiImplicitParam(name = "file",
            value = "文件对象",
            dataType = "MultipartFile",
            required = true,
            allowMultiple = true)})
    @ApiOperation("文件上传")
    @PostMapping("/upload/file")
    public ShareResponse uploadFile(
            @ApiParam(value = "file",required = true)
                    @RequestParam(value = "file", required = true)
                    MultipartFile file,
            @RequestParam(name = "pass", required = false)
            @ApiParam(value = "pass",required = false)
            String pass,
            @RequestParam(name = "expire", required = true)
            @ApiParam(value = "expire",required = false)
            Integer expire,
            @RequestParam(name = "maxGetCount", required = true)
            @ApiParam(value = "maxGetCount",required = false)
            Integer maxGetCount
    ) {
        return shareService.upload(file, pass, expire, maxGetCount, UploadType.FILE.name());
    }

    @ApiOperation("内容上传")
    @PostMapping("/upload/content")
    public ShareResponse uploadContent(
            @RequestParam(name = "content", required = true)
            @ApiParam(value = "content",required = true)
                    String content,
            @RequestParam(name = "pass", required = false)
            @ApiParam(value = "pass",required = false)
                    String pass,
            @RequestParam(name = "expire", required = true)
            @ApiParam(value = "expire",required = false)
                    Integer expire,
            @RequestParam(name = "maxGetCount", required = true)
            @ApiParam(value = "maxGetCount",required = false)
                    Integer maxGetCount
    ) {
        return shareService.upload(content, pass, expire, maxGetCount, UploadType.STRING.name());
    }


    @NotLoginAccess
    @ApiOperation("提取")
    @GetMapping("/take/{takeCode}")
    public String tack(
            @PathVariable("takeCode")
            String takeCode,
            @RequestParam(name = "pass", required = false)
            @ApiParam(value = "pass",required = false)
            String pass,
            HttpServletResponse response
    ) {
        takeCode = takeCode.trim().substring(0, 6);
        return shareService.get(takeCode, pass, response);
    }

    @ApiOperation("检查提取是否需要密码")
    @GetMapping("/check/need-pass/{takeCode}")
    public Boolean checkNeedPass(
            @PathVariable("takeCode")
            String takeCode
    ) {
        takeCode = takeCode.trim().substring(0, 6);
        return shareService.checkNeedPass(takeCode);
    }

    @ApiOperation("查询基本信息")
    @GetMapping("/info/{takeCode}")
    public FileInfoResponse getInfo(
        @PathVariable("takeCode")
        String takeCode,
        @RequestParam(name = "pass", required = false)
        @ApiParam(value = "pass",required = false)
                String pass
    ) {
        takeCode = takeCode.trim().substring(0, 6);
        return shareService.findFileInfo(takeCode, pass);
    }


    @ApiOperation("查询当前用户的文件列表")
    @GetMapping("/list/current")
    public List<FileInfoResponse> listByCurrent() {
        return shareService.listByCurrent();
    }

    @ApiOperation("创建分片上传")
    @PostMapping("/multipart/create")
    public MultipartUploadCreateResponse createMultipartUpload(
            @RequestBody
            @Validated
                    MultipartUploadCreateRequest multipartUploadCreateRequest
    ) {
        return fileUploadService.createMultipartUpload(multipartUploadCreateRequest);
    }

    @ApiOperation("合并分片")
    @PostMapping("/multipart/complete")
    public ShareResponse completeMultipartUpload(
            @RequestBody
            @Validated
                    CompleteMultipartUploadRequest uploadRequest
    ) {
        return fileUploadService.completeMultipartUpload(uploadRequest);
    }

    @ApiOperation("移除分享")
    @PostMapping("/remove/{takeCode}")
    public void remove(
            @PathVariable("takeCode")
            String takeCode
    ) {
        shareService.remove(takeCode);
    }

}