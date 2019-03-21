package com.example.demo.controller;

import com.example.demo.bean.JsonResult;
import com.example.demo.dto.computableModel.TaskServiceDTO;
import com.example.demo.dto.computableModel.UploadDataDTO;
import com.example.demo.service.ComputableService;
import com.example.demo.utils.ResultUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * Created by wang ming on 2019/2/26.
 */
@RestController
@Api(value = "计算模型模块")
@RequestMapping(value = "/computableModel")
public class ComputableModeController {

    private static final Logger log = LoggerFactory.getLogger(ComputableModeController.class);

    @Autowired
    ComputableService computableService;
    @RequestMapping(value = "", method = RequestMethod.POST)
    @ApiOperation(value = "上传计算模型")
    public JsonResult upload(@RequestParam("name") String name, @RequestParam("pid") String pid){
        return ResultUtils.success(computableService.insert(name, pid));
    }

    @RequestMapping(value = "/createTask", method = RequestMethod.POST)
    @ApiOperation(value = "创建taskServer,预准备过程")
    public JsonResult createTask(@RequestBody TaskServiceDTO taskServiceDTO){
        return ResultUtils.success(computableService.createTask(taskServiceDTO));
    }

    @RequestMapping(value = "/uploadData", method = RequestMethod.POST)
    @ApiOperation(value = "上传模型运行数据")
    public JsonResult uploadData(UploadDataDTO uploadDataDTO){
        MultipartFile file = uploadDataDTO.getFile();
        if(!file.isEmpty()){
            return ResultUtils.success(computableService.uploadData(uploadDataDTO));
        }else{
            return ResultUtils.error(-1,"上传文件为空");
        }

    }

    @RequestMapping(value = "/invoke", method = RequestMethod.POST)
    @ApiOperation(value = "运行模型服务，提交task任务")
    public JsonResult invokeModel(@RequestBody TaskServiceDTO taskServiceDTO){
        return ResultUtils.success(computableService.invokeModel(taskServiceDTO));
    }

}
