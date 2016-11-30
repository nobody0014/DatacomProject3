/**
 * Created by Wit on 11/22/2016 AD.
 */
import java.lang.reflect.Array;
import java.net.URI;

import com.sun.media.jfxmedia.track.Track;
import com.turn.ttorrent.common.Torrent;
import com.turn.ttorrent.client.*;
import com.turn.ttorrent.tracker.TrackedPeer;
import com.turn.ttorrent.tracker.TrackedTorrent;
import com.turn.ttorrent.tracker.Tracker;
import com.turn.ttorrent.tracker.TrackerService;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.eclipse.jetty.util.thread.ExecutorThreadPool;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.channels.UnsupportedAddressTypeException;
import java.net.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;

public class Hub implements Runnable{

    boolean isUploader;

    CountDownLatch latch;
    ConcurrentLinkedQueue<ExtendedClient> clients;

    ExecutorThreadPool etp;

    String fileName;

    public Hub(boolean isUploader,String fileName, CountDownLatch latch){
        this.isUploader = isUploader;
        this.fileName = fileName;
        this.latch = latch;
        this.clients = new ConcurrentLinkedQueue<>();
        this.etp = new ExecutorThreadPool();
    }

    @Override
    public void run(){
        if(this.isUploader){upload(this.fileName);}
        else{download(fileName);}
        System.out.println("Finished all operations");
        this.latch.countDown();
    }



    //Start the download process
    private void download(String fileName){
        try{
            this.clients.add(createClient(Inet4Address.getLocalHost(), new File(fileName), new File(".")));
            startClients(this.clients);
        }catch (Exception e){e.printStackTrace();}
    }

    //Start the upload process
    private void upload(String fileName){

        try{
            File fn = new File(fileName);
            File tn = new File(fileName + ".torrent");
            File out = new File(".");
            List<InetAddress> ips = getIPs();
            List<Tracker> trackers = new ArrayList<>();
            List<List<URI>> uris = new ArrayList<>();

            System.out.println("Adding the uris");
            uris.add(new ArrayList<>());
            for (InetAddress ip : ips){
                uris.get(0).add(new URI("http://" + ip.getHostAddress() + ":6969/announce"));
                trackers.add(generateTrackerServer(ip));
            }


            System.out.println("Generating torrent file");
            generateTorrentFile(fn,uris,trackers);



            System.out.println("Creating clients");
            SharedTorrent st = SharedTorrent.fromFile(tn, out);
            for (InetAddress ip: ips){
                this.clients.add(createClient(ip, st));
            }

            System.out.println("Starting clients");
            startClients(this.clients);

        }catch (Exception e){e.printStackTrace();}

    }

    //Get all the possible Inet4Address in this computer
    private List<InetAddress> getIPs() throws IOException{

        List<InetAddress> ips = new ArrayList<>();
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        for (NetworkInterface interface_ : Collections.list(interfaces)) {
            Enumeration<InetAddress> addresses = interface_.getInetAddresses();
            for (InetAddress address : Collections.list(addresses)) {
                if (address instanceof  Inet4Address) {
                    ips.add(address);
                }
            }
        }
        return ips;
    }

    //Create and set up client
    private ExtendedClient createClient(InetAddress ip, File fileName, File outputValue) throws IOException{
        ExtendedClient c = new ExtendedClient(ip, SharedTorrent.fromFile(fileName,outputValue));
        c.setMaxDownloadRate(0.0);
        c.setMaxUploadRate(0.0);
        return c;
    }
    private ExtendedClient createClient(InetAddress ip, SharedTorrent st) throws IOException{
        ExtendedClient c = new ExtendedClient(ip, st);
        c.setMaxDownloadRate(0.0);
        c.setMaxUploadRate(0.0);
        return c;
    }

    //Start all the clients created
    private void startClients(ConcurrentLinkedQueue<ExtendedClient> clients) throws InterruptedException{
        CountDownLatch counter = new CountDownLatch(clients.size());
        System.out.println("Number of clients on this computer: " + clients.size());

        for(ExtendedClient client: clients){
            client.setCounter(counter);
            this.etp.execute(client);
        }

        System.out.println("Waiting for all client to shut down");
        counter.await();
    }

    //Generate torrent file
    private void generateTorrentFile(File fileName, List<List<URI>> uris, List<Tracker> trackers)
            throws InterruptedException, IOException, NoSuchAlgorithmException {
        Torrent t = Torrent.create(fileName, uris, "Wit.Jo");
        File f = new File(fileName + ".torrent");
        t.save(new FileOutputStream(f));
        for (Tracker tracker : trackers){
            tracker.announce(TrackedTorrent.load(f));
        }
    }

    //Create the tracker server
    private Tracker generateTrackerServer(InetAddress iddr) throws IOException{

        Tracker t = new Tracker(iddr);
        t.start();
        return t;
    }


}
