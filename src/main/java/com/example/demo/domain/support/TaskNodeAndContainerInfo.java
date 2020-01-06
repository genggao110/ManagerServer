package com.example.demo.domain.support;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 包含Task Server相关信息、以及拥有特定模型的模型服务容器信息
 * @Author: wangming
 * @Date: 2020-05-24 22:31
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TaskNodeAndContainerInfo{
    String id;
    String host;
    String port;
    boolean status;
    /**
     * 包含该模型的模型容器mac地址，mac地址为唯一标识符
     */
    List<ContainerInfo> containerInfos;
}
