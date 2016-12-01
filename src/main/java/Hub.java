/**
 * Created by Wit on 11/22/2016 AD.
 */
import java.lang.reflect.Array;
import java.net.URI;

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

public class Hub extends Thread{

    boolean isUploader;

    ConcurrentLinkedQueue<ExtendedClient> clients;

    List<Tracker> trackers;

    ExecutorThreadPool etp;

    String fileName;

    CountDownLatch torrentFileWaiter;

    String iddr;



    public Hub(){
        this.clients = new ConcurrentLinkedQueue<>();
        this.etp = new ExecutorThreadPool();
        this.trackers = new ArrayList<>();
    }


    public Hub(boolean isUploader,String fileName){
        this();
        this.isUploader = isUploader;
        this.fileName = fileName;
    }

    public void run(){
        if(this.isUploader){upload(this.fileName);}
        else{download(fileName+".torrent");}
        System.out.println("finished the hub run");
    }








    public void setSetting(boolean isUploader, String fileName){
        this.fileName = fileName;
        this.isUploader = isUploader;
    }

    public void setLatch(CountDownLatch latch){
        this.torrentFileWaiter = latch;
    }

    public void setHostIddr(String iddr){
        this.iddr = iddr;
    }




    public synchronized String getClientsStatus(){
        boolean done = false;
        for (ExtendedClient ec : this.clients){
            if(ec.client.getState() == Client.ClientState.SEEDING){done = true;}
        }
        return String.valueOf(done);
    }








    public void cleanUp(){
        for (ExtendedClient ec : this.clients){
            ec.stop();
        }
        for (Tracker tr : trackers){
            tr.stop();
        }
        this.trackers = new ArrayList<>();
        this.clients = new ConcurrentLinkedQueue<>();
        this.isUploader = false;
        this.fileName = null;
    }











    //Start the download process
    private void download(String fileName){
        try{
            System.out.println("Creating clients");
            List<InetAddress> ips = getIPs();
            if(iddr != null){
                ips = getIPs(this.iddr);
            }
            for(InetAddress iddr: ips){
                this.clients.add(createClient(iddr, SharedTorrent.fromFile(new File(fileName), new File("."))));
            }

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
            if(iddr != null){
                 ips = getIPs(this.iddr);
            }

            List<List<URI>> uris = new ArrayList<>();

            System.out.println("Adding the uris");
            uris.add(new ArrayList<>());
            for (InetAddress ip : ips){
                List<URI> uri  = new ArrayList<>();
                uri.add(new URI("http://" + ip.getHostAddress() + ":6969/announce"));
                uri.add(new URI("http://" + ip.getHostName() + ":6969/announce"));
                uris.add(uri);
                this.trackers.add(generateTrackerServer(ip));
            }


            System.out.println("Generating torrent file");
            generateTorrentFile(fn,uris,this.trackers);



            System.out.println("Creating clients");
            for (InetAddress ip: ips){
                System.out.println(ip.getHostAddress());
                this.clients.add(createClient(ip, SharedTorrent.fromFile(tn, out)));
            }

            System.out.println("Starting clients");
            startClients(this.clients);


            this.torrentFileWaiter.countDown();

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

    private List<InetAddress> getIPs(String iddr) throws IOException{

        List<InetAddress> ips = new ArrayList<>();
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        for (NetworkInterface interface_ : Collections.list(interfaces)) {
            Enumeration<InetAddress> addresses = interface_.getInetAddresses();
            for (InetAddress address : Collections.list(addresses)) {
                if (address instanceof  Inet4Address) {
                    if(address.getHostAddress().equals(iddr)){ips.add(address);}
                }
            }
        }
        return ips;
    }






    //Create and set up client
    private ExtendedClient createClient(InetAddress ip, SharedTorrent st) throws IOException{
        ExtendedClient c = new ExtendedClient(ip, st);
        c.setClientSpeed(0.0,0.0);
        return c;
    }

    //Start all the clients created
    private void startClients(ConcurrentLinkedQueue<ExtendedClient> clients) throws InterruptedException{
        System.out.println("Number of clients on this computer: " + clients.size());

        for(ExtendedClient client: clients){
            this.etp.execute(client);
        }
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

    //Create and start the tracker server
    private Tracker generateTrackerServer(InetAddress iddr) throws IOException{
        Tracker t = new Tracker(new InetSocketAddress(iddr,6969));
        t.start();
        return t;
    }

}
