import org.apache.http.client.*;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;

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
        HttpClient client = HttpClients.createDefault();
        try{
            HttpPost post  = new HttpPost(new URI("http://" + this.masterIP + ":19999/reportClientStatus"));
            while (!this.stop){
                client.execute(post);
                Thread.sleep(5000);
            }
        }catch (Exception e){System.out.println("Unable to create the post request or unable to thread sleep");}
    }
    public void stopThread(){
        this.stop = true;
    }
}
