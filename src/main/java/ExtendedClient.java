import com.turn.ttorrent.client.Client;
import com.turn.ttorrent.client.SharedTorrent;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.CountDownLatch;


public class ExtendedClient implements Runnable{
    Client client;

    public ExtendedClient(InetAddress ip, SharedTorrent torrent) throws IOException{
        this.client = new Client(ip,torrent);
    }

    public void setClientSpeed(double up, double down){
        this.client.setMaxUploadRate(up);
        this.client.setMaxDownloadRate(down);
    }

    public void addObserver(){
        this.client.addObserver(new Observer() {
            @Override
            public void update(Observable o, Object arg) {
                Client client = (Client) o;
                float progress = client.getTorrent().getCompletion();
                System.out.println(client.getTorrent().getName() + "'s download progress: " + progress);
            }
        });
    }

    @Override
    public void run(){
        addObserver();
        this.client.share();
        this.client.waitForCompletion();
    }


    public void stop(){
        this.client.stop();
    }
}
