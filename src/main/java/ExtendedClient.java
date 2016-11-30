import com.turn.ttorrent.client.Client;
import com.turn.ttorrent.client.SharedTorrent;

import java.io.IOException;
import java.net.InetAddress;
import java.util.concurrent.CountDownLatch;


public class ExtendedClient extends Client{
    CountDownLatch counter;

    public ExtendedClient(InetAddress ip, SharedTorrent torrent) throws IOException{
        super(ip,torrent);
    }

    public void setCounter(CountDownLatch latch){
        this.counter = latch;
    }

    @Override
    public void run(){
        super.run();
        this.share();
        this.waitForCompletion();
        this.counter.countDown();
    }
}
