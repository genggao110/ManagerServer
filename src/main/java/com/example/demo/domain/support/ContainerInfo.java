package com.example.demo.domain.support;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 模型容器mac地址、模型容器在task server上的id、以及已开展和待开展任务数量实体类
 * @Author: wangming
 * @Date: 2020-05-24 23:15
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ContainerInfo {
    String mac;
    /**
     * 模型容器在task server数据库中存储的id
     */
    String sid;
    int count;
    /**
     * 服务的可靠性
     */
    double reliability;
}
