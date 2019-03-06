package com.example.demo.domain;

import com.example.demo.domain.support.TaskContainerInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;

/**
 * Created by wang ming on 2019/2/26.
 */
@Document
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComputableModel {

    @Id
    String id;
    String pid; //唯一标识计算模型服务id
    List<TaskContainerInfo> containerId;  //相关的任务服务器id
    String name;
    String provider;
    String modelItemId;
    String diagramModelId;
    String description;
    Date createTime;
    int viewCount;
    int shareCount;
}
