package utils;

import javax.servlet.http.HttpServletRequest;
import java.net.InetAddress;

/**
 * 获取客户端IP地址
 * Created by wang ming on 2019/2/21.
 */
public class IPUtils {

    public static String getIpAddress(HttpServletRequest request){
        String ip = request.getHeader("x-forwared-for");
        if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)){
            ip = request.getHeader("Proxy-Client-IP");
        }
        if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)){
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)){
            ip = request.getRemoteAddr();
            if(ip.equals("127.0.0.1")){
                //根据网卡取本机配置的IP
                InetAddress inet = null;
                try{
                    inet = InetAddress.getLocalHost();
                }catch (Exception e){
                    e.printStackTrace();
                }
                ip = inet.getHostAddress();
            }
        }
        //多个代理的情况，第一个IP为客户端真实IP,多个IP按照','分割
        if(ip != null && ip.length() > 15){
            if(ip.indexOf(",") > 0){
                ip = ip.substring(0,ip.indexOf(","));
            }
        }
        return ip;
    }
}
