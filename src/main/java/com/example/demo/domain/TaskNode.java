package com.example.demo.domain;

import com.example.demo.domain.support.GeoInfoMeta;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

/**
 * Created by wang ming on 2019/2/18.
 */
@Document
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskNode {

    @Id
    String id;
    String name;
    String host;
    String port;
    String system;
    Date createDate;

    GeoInfoMeta geoInfo;
    String register;

}
