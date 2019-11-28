package com.example.demo.sdk;

import java.io.IOException;

/**
 * Created by wang ming on 2019/4/12.
 */
public interface Data {

    String getURL();

    int download(String filePath)throws IOException;
}
