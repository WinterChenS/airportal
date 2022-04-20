package com.winterchen.airportal.rest;

/**
 * @author winterchen
 * @version 1.0
 * @date 2022/4/13 10:16
 * @description TODO
 **/

import com.winterchen.airportal.annotation.NotLoginAccess;
import com.winterchen.airportal.enums.UploadType;
import com.winterchen.airportal.response.FileInfoResponse;
import com.winterchen.airportal.response.ShareResponse;
import com.winterchen.airportal.service.ShareService;
import io.swagger.annotations.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;

@Api(tags = "分享管理")
@RequestMapping("/share")
@RestController
public class ShareController {


    private final ShareService shareService;

    public ShareController(ShareService shareService) {
        this.shareService = shareService;
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
        String takeCode
    ) {
        takeCode = takeCode.trim().substring(0, 6);
        return shareService.findFileInfo(takeCode);
    }

}