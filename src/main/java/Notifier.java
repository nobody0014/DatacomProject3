import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.nio.client.HttpAsyncClient;

import java.io.IOException;
import java.net.*;
import java.util.Calendar;
import java.util.Set;

public class Notifier implements Runnable {
    String fileName;
    Set<String> ips;
    int port;

    public Notifier(String fileName, Set<String> ips, int port){
        this.fileName = fileName;
        this.ips = ips;
        this.port = port;
    }

    public void run(){
        try{notifyEveryone();}catch (Exception e){e.printStackTrace();};
    }

    //Just keep sending to everyone
    private void notifyEveryone() throws URISyntaxException{

        HttpClient client = HttpClients.createDefault();
        for(String ip: this.ips){
            HttpPost post = new HttpPost(new URI("http://" + ip + ":" + this.port + "/download"));
            post.setHeader("fileName",this.fileName);
            try{
                client.execute(post);
            }catch (Exception e){System.out.println("Connection refused by " + ip);}

        }
    }
}
