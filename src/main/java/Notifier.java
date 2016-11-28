import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.nio.client.HttpAsyncClient;

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
        notifyEveryone();
    }

    //Just keep sending to everyone
    private void notifyEveryone(){
        HttpAsyncClient client = HttpAsyncClients.createDefault();
        HttpPost post = new HttpPost();
        post.setHeader("fileName",this.fileName);
        for(String ip: this.ips){
            HttpHost host = new HttpHost(ip + "/download",this.port);
            client.execute(host, post, new FutureCallback<HttpResponse>() {
                @Override
                public void completed(HttpResponse httpResponse) {
                }
                @Override
                public void failed(Exception e) {
                }
                @Override
                public void cancelled() {
                }
            });
        }
    }
}
