import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;

import java.net.Inet4Address;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Wit on 12/1/2016 AD.
 */
public class ResetNotifier extends Thread {
    public static ConcurrentHashMap<String,Boolean> ipStatus;

    public ResetNotifier(ConcurrentHashMap<String,Boolean> ipStatus){
        this.ipStatus = ipStatus;
    }

    public void run(){
        while (checkStatus()){
            try{Thread.sleep(10000);}catch (Exception e){}
        }
        sendResetNotification();
    }

    public void sendResetNotification() {
        HttpClient client  = HttpClients.createDefault();
        List<URI> uris = new ArrayList<>();
        for(String ip : ipStatus.keySet()){
            try{
            uris.add(new URI("http://" + ip  + ":19999/resetSystem"));
            }catch (Exception e){System.out.println("Unable to form uri for " + ip);}
        }
        try{
            uris.add(new URI("http://" + Inet4Address.getLocalHost().getHostAddress() + ":19999/resetSystem"));
        }catch (Exception e){System.out.println("Unable to form uri for loca host");}
        for(URI uri : uris){
            HttpPost post  = new HttpPost(uri);
            try{client.execute(post);}catch (Exception e){System.out.print("Connection refused by " + uri.getHost());}
        }
    }

    public boolean checkStatus(){
        if (ipStatus == null){return false;}
        for(String ip : ipStatus.keySet()){
            if (!ipStatus.get(ip)) {
                return false;
            }
        }
        return true;
    }
}
