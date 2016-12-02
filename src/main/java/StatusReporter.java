import org.apache.http.HttpResponse;
import org.apache.http.client.*;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
import java.net.URI;

/**
 * Created by Wit on 12/1/2016 AD.
 */

public class StatusReporter extends Thread{
    boolean stop = false;
    String masterIP;
    Hub h;

    public StatusReporter(){}

    public StatusReporter(String masterIP, Hub h){
        this.h = h;
        this.masterIP = masterIP;

    }

    public void setMasterIP(String masterIP){
        this.masterIP = masterIP;
    }

    public void setHub(Hub h){
        this.h = h;
    }

    public void run(){
        this.stop = false;
        RequestConfig requestConfig  = RequestConfig.custom().setConnectTimeout(2000).setConnectionRequestTimeout(2000).build();
        HttpClient client = HttpClientBuilder.create().setDefaultRequestConfig(requestConfig).build();
        double prev  = Double.valueOf(h.getClientsStatus());
        while (true){
            try{
                String currentS = h.getClientsStatus();
                double current  = Double.valueOf(currentS);
                HttpPost post  = new HttpPost(new URI("http://" + this.masterIP + ":19999/reportClientStatus"));
                post.setHeader("status", currentS);
                post.setConfig(requestConfig);
                HttpResponse res = client.execute(post);
                System.out.println(res.getStatusLine());
                Thread.sleep(5000);
            if (current == 100.0){break;}
            }catch (Exception e){
                e.printStackTrace();
                continue;
            }
        }
    }
    public void stopThread(){
        this.stop = true;
    }
}
