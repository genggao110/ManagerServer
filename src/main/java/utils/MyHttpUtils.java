package utils;

import org.apache.http.HttpEntity;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;

/**
 * Created by wang ming on 2019/2/18.
 */
public class MyHttpUtils {

    public static String GET(String urlString, String encode, Map<String, String> headers, String... m)throws IOException, URISyntaxException{
        String body = "";
        //考虑Http身份验证的情况
        CloseableHttpClient client = checkAuth(m);
        if(client == null){
            return "Input Auth parameter error";
        }
        URL url = new URL(urlString);
        URI uri = new URI(url.getProtocol(), url.getHost() + ":" + url.getPort(),url.getPath(),url.getQuery(), null);

        HttpGet httpGet = new HttpGet(uri);
        //设置header
        if(headers != null && headers.size() > 0){
            for(Map.Entry<String,String> entry: headers.entrySet()){
                httpGet.setHeader(entry.getKey(), entry.getValue());
            }
        }
        CloseableHttpResponse response = client.execute(httpGet);
        HttpEntity entity = response.getEntity();

        if(entity != null){
            body = EntityUtils.toString(entity,encode);
        }
        EntityUtils.consume(entity);

        response.close();
        client.close();
        return body;
    }
    /**
    * @Description:  Http身份验证
    * @Param: [m]
    * @return: org.apache.http.impl.client.CloseableHttpClient
    * @Author: WangMing
    * @Date: 2019/2/18
    */
    public static CloseableHttpClient checkAuth(String... m){
        if(m.length == 2){
            //需要验证
            CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(m[0],m[1]));
            return HttpClients.custom().setDefaultCredentialsProvider(credentialsProvider).build();
        }else if (m.length == 0){
            return HttpClients.createDefault();
        }else{
            return null;
        }
    }
}
