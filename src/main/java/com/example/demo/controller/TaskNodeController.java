package com.example.demo.controller;

import com.example.demo.bean.JsonResult;
import com.example.demo.domain.support.TaskNodeStatusInfo;
import com.example.demo.dto.taskNode.TaskNodeAddDTO;
import com.example.demo.dto.taskNode.TaskNodeCalDTO;
import com.example.demo.dto.taskNode.TaskNodeFindDTO;
import com.example.demo.dto.taskNode.TaskNodeReceiveDTO;
import com.example.demo.service.TaskNodeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.WebAsyncTask;
import utils.ResultUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;

/**
 * Created by wang ming on 2019/2/18.
 */
@RestController
@Api(value = "任务服务器模块")
@RequestMapping(value = "/taskNode")
public class TaskNodeController {

    private static final Logger log = LoggerFactory.getLogger(TaskNodeController.class);

    @Autowired
    TaskNodeService taskNodeService;


    //测试Callable异步请求处理
//    @RequestMapping(value = "", method = RequestMethod.POST)
//    @ApiOperation(value = "add TaskNode")
//    public Callable<JsonResult> callable(@RequestBody TaskNodeAddDTO taskNodeAddDTO){
//        log.info("外部线程： " + Thread.currentThread().getName());
//        return new Callable<JsonResult>() {
//            @Override
//            public JsonResult call() throws Exception {
//                log.info("内部线程： " + Thread.currentThread().getName());
//                if(taskNodeService.judgeTaskNode(taskNodeAddDTO.getHost(),taskNodeAddDTO.getPort())){
//                    return ResultUtils.error(-1,"the task node has been registered");
//                }else{
//                    return ResultUtils.success(taskNodeService.insert(taskNodeAddDTO));
//                }
//            }
//        };
//    }

    //WebAsyncTask异步请求处理
    @RequestMapping(value = "", method = RequestMethod.POST)
    public WebAsyncTask<JsonResult> asyncTask(@RequestBody TaskNodeAddDTO taskNodeAddDTO){

        WebAsyncTask<JsonResult> webAsyncTask = new WebAsyncTask<JsonResult>(10000, new Callable<JsonResult>() {
            @Override
            public JsonResult call() throws Exception {
                if(taskNodeService.judgeTaskNode(taskNodeAddDTO.getHost(),taskNodeAddDTO.getPort())){
                    return ResultUtils.error(-1,"the task node has been registered");
                }else{
                    return ResultUtils.success(taskNodeService.insert(taskNodeAddDTO));
                }
            }
        });

        webAsyncTask.onCompletion(() ->{
            log.info("内部线程： " + Thread.currentThread().getName() + "执行完毕");
        });

        webAsyncTask.onTimeout(() ->{
            log.info("内部线程： " + Thread.currentThread().getName() + "onTimeout");
            throw new TimeoutException("调用超时");
        });
        return webAsyncTask;

    }


    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    @ApiOperation(value = "delete TaskNode By Id")
    JsonResult delete(@PathVariable("id") String id){
        taskNodeService.delete(id);
        return ResultUtils.success();
    }

    @RequestMapping(value = "/{id}",method = RequestMethod.GET)
    @ApiOperation(value = "get TaskNode By Id")
    JsonResult get(@PathVariable String id) {
        return ResultUtils.success(taskNodeService.getById(id));
    }


    @RequestMapping(value = "", method = RequestMethod.GET)
    @ApiOperation(value = "get TaskNode list by page and pagesize")
    JsonResult list(TaskNodeFindDTO taskNodeFindDTO)
    {
        return ResultUtils.success(taskNodeService.list(taskNodeFindDTO));
    }

    @RequestMapping(value = "/all", method = RequestMethod.GET)
    @ApiOperation(value = "get all TaskNode record")
    JsonResult listAll(){
        return ResultUtils.success(taskNodeService.listAll());
    }

    @RequestMapping(value = "/getTaskForRegister", method = RequestMethod.POST)
    @ApiOperation(value = "根据各个任务服务器的延迟信息进行运算，获取最适合注册的Task服务器")
    JsonResult getTaskServer(@RequestBody TaskNodeCalDTO taskNodeCalDTO){

        return ResultUtils.success(taskNodeService.getTaskServerForRegister(taskNodeCalDTO));
    }

    @RequestMapping(value = "/getServiceTask/{pid}", method = RequestMethod.GET)
    @ApiOperation(value = "根据模型pid找到最适合的任务服务器节点")
    JsonResult getTaskServerByPid(@PathVariable("pid") String pid){
        List<TaskNodeReceiveDTO> taskNodeList = taskNodeService.listAll();
        List<Future<TaskNodeStatusInfo>> futures = new ArrayList<>();
        //开启异步任务
        taskNodeList.forEach((TaskNodeReceiveDTO obj) ->{
            Future<TaskNodeStatusInfo> future = taskNodeService.judgeTaskNodeByPid(obj,pid);
            futures.add(future);
        });
        List<TaskNodeStatusInfo> response = new ArrayList<>();
        futures.forEach((future) ->{
            try {
                TaskNodeStatusInfo taskNodeStatusInfo = (TaskNodeStatusInfo)future.get();
                //判断
                if(taskNodeStatusInfo != null && taskNodeStatusInfo.isStatus()){
                    response.add(taskNodeStatusInfo);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        });

        //TODO 根据算法进行择优选择,目前一个字段,所以排序获得就行（得分规则后面完善）
        response.sort((o1,o2) ->{
            return o1.getRunning() - o2.getRunning();
        });
        if(response.size() != 0){
            return ResultUtils.success(response.get(0));
        }else{
            return ResultUtils.success();
        }
    }


}
