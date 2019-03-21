package com.example.demo.dto.computableModel;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

/**
 * Created by wang ming on 2019/3/21.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UploadDataDTO {
    String host;
    int port;
    String tag;
    MultipartFile file;
}
