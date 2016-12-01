import com.turn.ttorrent.client.Client;
import com.turn.ttorrent.client.SharedTorrent;

import java.io.IOException;
import java.net.InetAddress;
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

    @Override
    public void run(){
        this.client.share();
        this.client.waitForCompletion();
    }
    public void stop(){
        this.client.stop();
    }
}
