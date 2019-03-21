package com.example.demo.sdk;

import java.io.IOException;
import java.net.URISyntaxException;

/**
 * Created by wang ming on 2019/3/20.
 */
public class OGMSService_DEBUG {

    public static Server CreateServer(String ip, int port){
        return new Server(ip, port);
    }

    public static GeoDataExServer CreateDataExchangeServer(String ip, int port){
        GeoDataExServer dataExServer = new GeoDataExServer(ip,port);
        try{
            int status = dataExServer.connect();
            if(status == 1){
                return dataExServer;
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;

    }

    public static GeoTaskServer CreateTaskServer(String ip, int port){
        GeoTaskServer taskServer = new GeoTaskServer(ip, port);
        try{
            int status = taskServer.connect();
            if(status == 1){
                return taskServer;
            }

        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
