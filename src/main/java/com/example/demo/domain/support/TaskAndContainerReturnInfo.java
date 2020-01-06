package com.example.demo.domain.support;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 返回给我的管理后台的实体类
 * @Author: wangming
 * @Date: 2020-05-25 10:15
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskAndContainerReturnInfo {
    String id;
    String host;
    String port;
    String mac;
    /**
     * 模型容器在task server数据库中存储的id
     */
    String sid;
    int count;
    double reliability;
}
