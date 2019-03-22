package com.example.demo.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.example.demo.dao.ComputableModelDao;
import com.example.demo.domain.ComputableModel;
import com.example.demo.dto.computableModel.ExDataDTO;
import com.example.demo.dto.computableModel.TaskResultDTO;
import com.example.demo.dto.computableModel.TaskServiceDTO;
import com.example.demo.dto.computableModel.UploadDataDTO;
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

/**
 * Created by wang ming on 2019/3/15.
 */
@Service
public class ComputableService {

    @Autowired
    ComputableModelDao computableModelDao;

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
        if(taskServiceDTO.getEx_ip() != null){
            GeoDataExServer dataExServer = OGMSService_DEBUG.CreateDataExchangeServer(taskServiceDTO.getEx_ip(),taskServiceDTO.getEx_port());
            task = taskServer.createTask(pid,dataExServer,username);
        }else{
            task = taskServer.createTask(pid,null,username);
        }
        return task;
    }

    public ExDataDTO uploadData(UploadDataDTO uploadDataDTO){
        MultipartFile file = uploadDataDTO.getFile();
        String filename = file.getOriginalFilename();
        String filenameWithoutExt = filename.substring(0,filename.lastIndexOf("."));
        String ip = uploadDataDTO.getHost();
        int port = uploadDataDTO.getPort();
        String tag = uploadDataDTO.getTag();
        if(tag.equals(""))
            tag = filenameWithoutExt;
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
                    is.close();
                    return exDataDTO;
                }else{
                    Map<String, String> params = new HashMap<String, String>();

                    params.put("datatag", tag);
                    params.put("pwd", "true");

                    String actionUrl = "http://" + ip + ":" + port + "/data";
                    String result = MyHttpUtils.POSTInputStreamToDataExServer(actionUrl,"UTF-8",params,is,filename);
                    JSONObject jResult = JSONObject.parseObject(result);
                    if(jResult.getString("result").equals("suc")){
                        JSONObject jData = jResult.getJSONObject("data");
                        String pwd = jData.getString("d_pwd");
                        pwd = MyFileUtils.decryption(MyFileUtils.decryption(pwd));
                        String id = jData.getString("id");
                        ExData tempData = new ExData(ip,port,id,pwd);
                        exDataDTO.setTag(tag);
                        exDataDTO.setUrl(tempData.getURL());
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

    public JSONObject invokeModel(TaskServiceDTO taskServiceDTO){
        //利用taskServiceDTO拼凑提交任务的form表单，从而提交任务
        String ip = taskServiceDTO.getIp();
        int port = taskServiceDTO.getPort();
        String pid = taskServiceDTO.getPid();
        String username = taskServiceDTO.getUsername();
        List<ExDataDTO> inputs = taskServiceDTO.getInputs();
        JSONObject params = new JSONObject();
        String inputsArray = convertItems2JSON(inputs);
        params.put("inputs", inputsArray);
        params.put("username",username);
        params.put("pid", pid);

        String actionUrl = "http://" + ip + ":" + port + "/task";
        JSONObject result = new JSONObject();

        try{
            String resJson = MyHttpUtils.POSTWithJSON(actionUrl,"UTF-8",null,params);
            JSONObject jResponse = JSONObject.parseObject(resJson);
            if(jResponse.getString("result").equals("suc")){
                String tid = jResponse.getString("data");
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
            }
        } catch (URISyntaxException e) {
            throw new RuntimeException(e.getMessage());
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
        return taskResultDTO;
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
            outputItems.add(exDataDTO);
        }
        return outputItems;
    }

}
