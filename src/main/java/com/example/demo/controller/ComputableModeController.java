package com.example.demo.controller;

import com.alibaba.fastjson.JSONObject;
import com.example.demo.bean.JsonResult;
import com.example.demo.domain.support.TaskAndContainerReturnInfo;
import com.example.demo.dto.computableModel.TaskResultDTO;
import com.example.demo.dto.computableModel.TaskServiceDTO;
import com.example.demo.dto.computableModel.TaskSubmitDTO;
import com.example.demo.dto.computableModel.UploadDataDTO;
import com.example.demo.service.ComputableService;
import com.example.demo.utils.ResultUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.List;

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

    @RequestMapping(value = "/createTask", method = RequestMethod.POST)
    @ApiOperation(value = "创建taskServer,预准备过程")
    public JsonResult createTask(@RequestBody TaskServiceDTO taskServiceDTO){
        return ResultUtils.success(computableService.createTask(taskServiceDTO));
    }

    @RequestMapping(value = "/uploadData", method = RequestMethod.POST)
    @ApiOperation(value = "上传模型运行数据")
    public JsonResult uploadData(UploadDataDTO uploadDataDTO){
        MultipartFile[] file = uploadDataDTO.getFile();
        if(file.length > 0){
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

    @RequestMapping(value = "/refreshTaskRecord", method = RequestMethod.POST)
    @ApiOperation(value = "获取task运行记录信息")
    public JsonResult refreshTaskRecord(@RequestBody TaskResultDTO taskResultDTO){
        TaskResultDTO temp = computableService.refreshRecord(taskResultDTO);
        if (temp == null){
            return ResultUtils.error(-1,"任务服务器出错");
        }
        return ResultUtils.success(temp);
    }

    @RequestMapping(value = "/submitTask", method = RequestMethod.POST)
    @ApiImplicitParam(paramType = "body", dataType = "TaskSubmitDTO", name = "taskSubmitDTO", value = "任务提交实体", required = true)
    @ApiOperation(value = "用户提交模型pid，直接运行模型")
    public JsonResult submitTask(@Valid @RequestBody TaskSubmitDTO taskSubmitDTO){
        //首先根据pid找到最适合的Task-Server节点
        JSONObject result = computableService.submitTask(taskSubmitDTO);
        if (result == null){
            return ResultUtils.error(-1,"找不到可用的地理模型服务运行");
        }else{
            return ResultUtils.success(result);
        }
    }

    @RequestMapping(value = "verify/{pid}",method = RequestMethod.GET)
    @ApiOperation(value = "根据pid来验证是否存在可用的地理模型服务")
    public JsonResult verifyTask(@PathVariable("pid") String pid){
        return ResultUtils.success(computableService.verifyTask(pid));
    }

    @RequestMapping(value = "/getAllTaskServerNode/{pid}", method = RequestMethod.GET)
    @ApiOperation(value = "根据计算模型pid找到所有适合运行的计算资源节点(包含其所承载的task server信息)")
    JsonResult getAllTaskServerNodeByPid(@PathVariable("pid") String pid){
        List<TaskAndContainerReturnInfo> allTaskServerNodeByPid = computableService.getAllTaskServerNodeByPid(pid);
        return ResultUtils.success(allTaskServerNodeByPid);
    }

}
