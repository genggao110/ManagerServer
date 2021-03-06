package com.example.demo.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.example.demo.dao.ComputableModelDao;
import com.example.demo.domain.ComputableModel;
import com.example.demo.domain.support.ContainerInfo;
import com.example.demo.domain.support.TaskAndContainerReturnInfo;
import com.example.demo.domain.support.TaskNodeAndContainerInfo;
import com.example.demo.domain.support.TaskNodeStatusInfo;
import com.example.demo.dto.computableModel.*;
import com.example.demo.dto.taskNode.TaskNodeReceiveDTO;
import com.example.demo.enums.ResultEnum;
import com.example.demo.exception.MyException;
import com.example.demo.sdk.*;
import com.example.demo.utils.MyFileUtils;
import com.example.demo.utils.MyHttpUtils;
import org.apache.commons.codec.DecoderException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Created by wang ming on 2019/3/15.
 */
@Service
public class ComputableService {

    @Autowired
    ComputableModelDao computableModelDao;

    @Autowired
    TaskNodeService taskNodeService;

    private static final Logger log = LoggerFactory.getLogger(ComputableService.class);


    //测试所用的computableModel
    public ComputableModel insert(String name, String pid){
        ComputableModel computableModel = new ComputableModel();
        computableModel.setName(name);
        computableModel.setPid(pid);
        computableModel.setCreateTime(new Date());
        return computableModelDao.insert(computableModel);
    }

    public Task createTask(TaskServiceDTO taskServiceDTO){
        String ip = taskServiceDTO.getIp();
        int port = taskServiceDTO.getPort();
        String pid = taskServiceDTO.getPid();
        String username = taskServiceDTO.getUsername();
        GeoTaskServer taskServer = OGMSService_DEBUG.CreateTaskServer(ip, port);
        Task task;
        //type value contains: 1 - DataExchangeServer, 2 - DataServiceServer
        if(taskServiceDTO.getEx_ip() != null && taskServiceDTO.getType() != 0){
            if(taskServiceDTO.getType() == 1){
                GeoDataExServer dataExServer = OGMSService_DEBUG.CreateDataExchangeServer(taskServiceDTO.getEx_ip(),taskServiceDTO.getEx_port(),username);
                task = taskServer.createTask(pid,dataExServer,username);
            }else{
                GeoDataServiceServer dataServiceServer = OGMSService_DEBUG.CreateDataServiceServer(taskServiceDTO.getEx_ip(),taskServiceDTO.getEx_port(),username);
                task = taskServer.createTask(pid,dataServiceServer,username);
            }
        }else{
            task = taskServer.createTask(pid,null,username);
        }
        return task;
    }

    public ExDataDTO uploadData(UploadDataDTO uploadDataDTO){
        int type = uploadDataDTO.getType();
        MultipartFile[] file = uploadDataDTO.getFile();
        List<MultipartFile> fileList = Arrays.asList(file);
        String filename = uploadDataDTO.getFileName();
        String filenameWithoutExt = filename.substring(0,filename.lastIndexOf("."));
        String ext = filename.substring(filename.lastIndexOf(".") + 1, filename.length());
        String ip = uploadDataDTO.getHost();
        int port = uploadDataDTO.getPort();
        String userId = uploadDataDTO.getUserId();
        ExDataDTO exDataDTO = null;
        if(type == 1){
            //数据交换服务器默认只能上传第一个文件
            exDataDTO = uploadDataToExServer(file[0],ip,port,filename,ext);
        }else if(type == 2){
            exDataDTO = uploadDataToDCServer_Update(fileList,ip,port,filenameWithoutExt,ext,userId);
        }
        return exDataDTO;
    }

    /**
     * 上传数据到数据交换服务器
     * modified by wangming at 2019.11.07: 修改上传的函数参数，添加了文件后缀
     * @param file
     * @param ip
     * @param port
     * @param tag
     * @return com.example.demo.dto.computableModel.ExDataDTO
     * @author wangming
     * @date 2019/11/7 10:01
     */
    private ExDataDTO uploadDataToExServer(MultipartFile file,String ip, int port, String tag, String ext){
        ExDataDTO exDataDTO = new ExDataDTO();
        try {
            InputStream is = file.getInputStream();
            String md5 = MyFileUtils.getMD5(is);
            String url = "http://" + ip + ":" + port + "/data?md5=" + md5;
            String response = MyHttpUtils.GET(url,"UTF-8",null);
            JSONObject jResponse =  JSONObject.parseObject(response);
            if(jResponse.getString("result").equals("suc")){
                int code = jResponse.getIntValue("code");
                if(code == 1){
                    JSONObject jData = jResponse.getJSONObject("data");
                    String pwd = jData.getString("d_pwd");
                    pwd = MyFileUtils.decryption(MyFileUtils.decryption(pwd));
                    String id = jData.getString("id");
                    ExData tempData = new ExData(ip,port,id,pwd);
                    exDataDTO.setTag(tag);
                    exDataDTO.setUrl(tempData.getURL());
                    exDataDTO.setSuffix(ext);
                    is.close();
                    return exDataDTO;
                }else{
                    Map<String, String> params = new HashMap<String, String>();

                    params.put("datatag", tag);
                    params.put("pwd", "true");

                    String actionUrl = "http://" + ip + ":" + port + "/data";
                    Map<String,MultipartFile> fileMap = new HashMap<>();
                    fileMap.put("datafile",file);
                    String result = MyHttpUtils.POSTMultiPartFileToDataServer(actionUrl,"UTF-8",params,fileMap);
                    JSONObject jResult = JSONObject.parseObject(result);
                    if(jResult.getString("result").equals("suc")){
                        JSONObject jData = jResult.getJSONObject("data");
                        String pwd = jData.getString("d_pwd");
                        pwd = MyFileUtils.decryption(MyFileUtils.decryption(pwd));
                        String id = jData.getString("id");
                        ExData tempData = new ExData(ip,port,id,pwd);
                        exDataDTO.setTag(tag);
                        exDataDTO.setUrl(tempData.getURL());
                        exDataDTO.setSuffix(ext);
                        is.close();
                        return exDataDTO;
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e.getMessage());
        } catch (DecoderException e) {
            throw new RuntimeException(e.getMessage());
        }
        return null;
    }

    /**
     * 上传数据到数据服务容器
     * modified by wangming at 2019.11.07: 修改了返回的数据内容
     * @param file
     * @param ip
     * @param port
     * @param fileName
     * @param ext
     * @param userName
     * @return com.example.demo.dto.computableModel.ExDataDTO
     * @author wangming
     * @date 2019/11/7 10:03
     */
    private ExDataDTO uploadDataToDCServer(MultipartFile file, String ip, int port, String fileName, String ext, String userName){
        ExDataDTO exDataDTO = new ExDataDTO();
        try{
            String url = "http://" + ip + ":" + port + "/file/upload/store_dataResource_files";
            Map<String,MultipartFile> fileMap = new HashMap<>();
            fileMap.put("file",file);
            String result = MyHttpUtils.POSTMultiPartFileToDataServer(url,"UTF-8",null,fileMap);
            JSONObject jResponse = JSONObject.parseObject(result);
            if(jResponse.getIntValue("code") == 0){
                JSONObject dataObject = jResponse.getJSONObject("data");
                String dataId = dataObject.getString("source_store_id");
                //拼接post请求
                String dataUrl = "http://" + ip + ":" + port + "/dataResource";
                JSONObject formData = new JSONObject();
                formData.put("author", userName);
                formData.put("fileName", fileName);
                formData.put("sourceStoreId", dataId);
                formData.put("suffix", ext);
                formData.put("type", "OTHER");
                formData.put("fromWhere","MODELCONTAINER");

                String result2 = MyHttpUtils.POSTWithJSON(dataUrl,"UTF-8",null,formData);
                JSONObject jResult = JSONObject.parseObject(result2);
                if(jResult.getIntValue("code") == 0){
                    DCData tempData = new DCData(ip,port,dataId);
                    exDataDTO.setTag(fileName);
                    exDataDTO.setUrl(tempData.getURL());
                    exDataDTO.setSuffix(ext);
                    return exDataDTO;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
        return null;
    }

    /**
     * TODO 处理新版数据容器接口
     * @param fileList
     * @param ip
     * @param port
     * @param fileName
     * @param ext
     * @param userId
     * @return com.example.demo.dto.computableModel.ExDataDTO
     * @author wangming
     * @date 2020/1/4 21:48
     */
    private ExDataDTO uploadDataToDCServer_Update(List<MultipartFile> fileList, String ip, int port,String fileName,String ext,String userId){
        //根据不同的类型，选择不同的上传url
        ExDataDTO exDataDTO = new ExDataDTO();
        try{
            String url = "http://" + ip + ":" + port + "/data";
            Map<String,String> params = new HashMap<>();
            params.put("name", fileName);
            params.put("userId",userId);
            //目前服务节点默认设置为china
            params.put("serverNode","china");
            params.put("origination","portal");
            String result = MyHttpUtils.PostToNewDataContainer(url,"UTF-8",null,params,fileList);
            JSONObject jResponse = JSONObject.parseObject(result);
            if (jResponse.getIntValue("code") == 0){
                JSONObject dataObject = jResponse.getJSONObject("data");
                String dataId = dataObject.getString("source_store_id");
                DCData tempData = new DCData(ip, port,dataId);
                exDataDTO.setTag(fileName);
                exDataDTO.setUrl(tempData.getURL());
                exDataDTO.setSuffix(ext);
                return exDataDTO;
            }

        }catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }
        return null;
    }

    public JSONObject invokeModel(TaskServiceDTO taskServiceDTO){
        //利用taskServiceDTO拼凑提交任务的form表单，从而提交任务
        String ip = taskServiceDTO.getIp();
        int port = taskServiceDTO.getPort();
        String pid = taskServiceDTO.getPid();
        String username = taskServiceDTO.getUsername();
        List<ExDataDTO> inputs = taskServiceDTO.getInputs();
        List<OutputDataDTO> outputs = taskServiceDTO.getOutputs();
        JSONObject params = new JSONObject();
        String inputsArray = convertItems2JSON(inputs);
        String outputsArray = convertOutputItems2JSON(outputs);
        params.put("inputs", inputsArray);
        params.put("username",username);
        params.put("pid", pid);
        params.put("outputs", outputsArray);

        String actionUrl = "http://" + ip + ":" + port + "/task";
        JSONObject result = new JSONObject();

        try{
            String resJson = MyHttpUtils.POSTWithJSON(actionUrl,"UTF-8",null,params);
            JSONObject jResponse = JSONObject.parseObject(resJson);
            if(jResponse.getString("result").equals("suc")){
                String tid = jResponse.getString("data");
                if(tid.equals("")){
                    throw new MyException(ResultEnum.NO_OBJECT);
                }
                result.put("tid",tid);
            }
        }catch (IOException e){
            throw new RuntimeException(e.getMessage());
        }
        return result;
    }

    public TaskResultDTO refreshRecord(TaskResultDTO taskResultDTO){
        String ip = taskResultDTO.getIp();
        int port = taskResultDTO.getPort();
        String tid = taskResultDTO.getTid();

        String url = "http://" + ip + ":" + port + "/task/" + tid;

        try{
            String resJson = MyHttpUtils.GET(url,"UTF-8",null);
            JSONObject jResponse = JSONObject.parseObject(resJson);
            if(jResponse.getString("result").equals("suc")){
                JSONObject jData = jResponse.getJSONObject("data");
                if(jData == null){
                    return null;
                }
                String taskStatus = jData.getString("t_status");
                taskResultDTO.setStatus(convertStatus(taskStatus));
                taskResultDTO.setPid(jData.getString("t_pid"));
                List<ExDataDTO> outputItems = convertJSON2Items(jData.getJSONArray("t_outputs"));
                taskResultDTO.setOutputs(outputItems);
            }else{
                return null;
            }
        } catch (URISyntaxException e) {
            throw new RuntimeException(e.getMessage());
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
        return taskResultDTO;
    }

    public boolean verifyTask(String pid){
        List<TaskNodeStatusInfo> result = getSuitableTaskNode(pid);
        if (result.size() == 0) {
            return false;
        }else {
            return true;
        }
    }

    //根据pid向所有的taskServer发起请求从而获取到所有可用的计算资源
    public List<TaskAndContainerReturnInfo> getAllTaskServerNodeByPid(String pid){
        //首先获取到所有的task server节点
        List<TaskNodeReceiveDTO> taskNodeReceiveDTOList = taskNodeService.listAll();
        List<Future<TaskNodeAndContainerInfo>> futures = new ArrayList<>();
        //开启异步任务
        taskNodeReceiveDTOList.forEach((TaskNodeReceiveDTO obj) -> {
            //根据pid获取到所有符合条件的task Server以及相关的容器信息
            Future<TaskNodeAndContainerInfo> future = taskNodeService.judgeTaskNodeAndContainerByPid(obj, pid);
            futures.add(future);
        });
        //TODO 整理所有可用的计算资源
        List<TaskNodeAndContainerInfo> response = new ArrayList<>();
        futures.forEach((future) -> {
            try{
                TaskNodeAndContainerInfo taskNodeAndContainerInfo = (TaskNodeAndContainerInfo) future.get();
                if(taskNodeAndContainerInfo != null && taskNodeAndContainerInfo.isStatus()){
                    response.add(taskNodeAndContainerInfo);
                }
            }catch (InterruptedException e){
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        });

        //TODO 将可用计算资源整理成合适的list数组返回
        List<TaskAndContainerReturnInfo> returnInfos = new ArrayList<>();
        response.forEach(taskNodeAndContainerInfo -> {
            TaskAndContainerReturnInfo temp = new TaskAndContainerReturnInfo();
            temp.setId(taskNodeAndContainerInfo.getId());
            temp.setHost(taskNodeAndContainerInfo.getHost());
            temp.setPort(taskNodeAndContainerInfo.getPort());
            List<ContainerInfo> containerInfos = taskNodeAndContainerInfo.getContainerInfos();
            for(int i =0; i < containerInfos.size(); i++){
                ContainerInfo containerInfo = containerInfos.get(i);
                temp.setMac(containerInfo.getMac());
                temp.setSid(containerInfo.getSid());
                temp.setCount(containerInfo.getCount());
                temp.setReliability(containerInfo.getReliability());
                returnInfos.add(temp);
            }
        });

        return returnInfos;
    }

    //代码重构，将公用模块抽出来成为一个方法
    public List<TaskNodeStatusInfo> getSuitableTaskNode(String pid){
        List<TaskNodeStatusInfo> result = new ArrayList<>();
        List<TaskNodeReceiveDTO> taskNodeList = taskNodeService.listAll();
        List<Future<TaskNodeStatusInfo>> futures = new ArrayList<>();
        //开启异步任务
        taskNodeList.forEach((TaskNodeReceiveDTO obj) ->{
            Future<TaskNodeStatusInfo> future = taskNodeService.judgeTaskNodeByPid(obj,pid);
            futures.add(future);
        });
        futures.forEach((future) ->{
            try {
                TaskNodeStatusInfo taskNodeStatusInfo = (TaskNodeStatusInfo)future.get();
                //判断
                if(taskNodeStatusInfo != null && taskNodeStatusInfo.isStatus()){
                    result.add(taskNodeStatusInfo);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        });
        return result;
    }

    public JSONObject submitTask(TaskSubmitDTO taskSubmitDTO){
        String pid = taskSubmitDTO.getPid();
        List<TaskNodeStatusInfo> response = getSuitableTaskNode(pid);
        //TODO 根据算法进行择优选择,目前一个字段,所以排序获得就行（得分规则后面完善）
        response.sort(Comparator.comparingInt(TaskNodeStatusInfo::getRunning));
        TaskNodeStatusInfo taskNodeStatusInfo;
        if(response.size() != 0){
            taskNodeStatusInfo = response.get(0);
            TaskServiceDTO taskServiceDTO = new TaskServiceDTO();
            taskServiceDTO.setIp(taskNodeStatusInfo.getHost());
            taskServiceDTO.setPort(Integer.parseInt(taskNodeStatusInfo.getPort()));
            taskServiceDTO.setPid(pid);
            taskServiceDTO.setUsername(taskSubmitDTO.getUserName());
            taskServiceDTO.setInputs(taskSubmitDTO.getInputs());
            taskServiceDTO.setOutputs(taskSubmitDTO.getOuputs());
            JSONObject result = invokeModel(taskServiceDTO);
            return result;
        }else{
            return null;
        }
    }

    private int convertStatus(String taskStatus){
        int status;
        if(taskStatus.equals("Inited")){
            status = 0;
        }else if(taskStatus.equals("Started")){
            status = 1; //started
        }else if(taskStatus.equals("Finished")){
            status = 2; //Finished
        }else {
            status = -1;
        }
        return status;
    }

    private String convertItems2JSON(List<ExDataDTO> inputs){
        JSONArray resultJson = new JSONArray();
        for(ExDataDTO input: inputs){
            JSONObject temp = new JSONObject();
            temp.put("StateName", input.getStatename());
            temp.put("Event", input.getEvent());
            temp.put("Url", input.getUrl());
            temp.put("Tag", input.getTag());
            temp.put("Suffix",input.getSuffix());
            resultJson.add(temp);
        }
        return resultJson.toJSONString();
    }

    private String convertOutputItems2JSON(List<OutputDataDTO> outputs){
        JSONArray resultJson = new JSONArray();
        for(OutputDataDTO output:outputs){
            JSONObject temp = new JSONObject();
            temp.put("StateName",output.getStatename());
            temp.put("Event", output.getEvent());
            temp.put("Type",output.getTemplate().getType());
            temp.put("Value",output.getTemplate().getValue());
            resultJson.add(temp);
        }
        return resultJson.toJSONString();
    }

    private List<ExDataDTO> convertJSON2Items(JSONArray jOutputs){
        List<ExDataDTO> outputItems = new ArrayList<>();
        for(int i = 0; i < jOutputs.size(); i++){
            JSONObject temp = jOutputs.getJSONObject(i);
            ExDataDTO exDataDTO = new ExDataDTO();
            exDataDTO.setStatename(temp.getString("StateName"));
            exDataDTO.setEvent(temp.getString("Event"));
            exDataDTO.setTag(temp.getString("Tag"));
            exDataDTO.setUrl(temp.getString("Url"));
            exDataDTO.setSuffix(temp.getString("Suffix"));
            outputItems.add(exDataDTO);
        }
        return outputItems;
    }

}
