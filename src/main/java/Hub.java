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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.channels.UnsupportedAddressTypeException;
import java.net.*;
import java.io.*;
import java.util.*;

public class Hub {


    public void download(String fileName){

        try{
            startClient(Inet4Address.getLocalHost(), new File(fileName), new File("."));
        }catch (Exception e){e.printStackTrace();}
    }


    public void upload(String fileName){

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

            System.out.println("Starting clients");
            for (InetAddress ip: ips){
                startClient(ip,tn,out);
            }
            System.out.println("Finishing starting clients");

        }catch (Exception e){e.printStackTrace();}

    }

    public List<InetAddress> getIPs() throws IOException{

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


    public void startClient(InetAddress ip, File fileName, File outputValue){

        try {
            Client c = new Client(ip, SharedTorrent.fromFile(fileName, outputValue));
            c.setMaxDownloadRate(0.0);
            c.setMaxUploadRate(0.0);
            c.share();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void generateTorrentFile(File fileName, List<List<URI>> uris, List<Tracker> trackers)
            throws InterruptedException, IOException, NoSuchAlgorithmException {
        Torrent t = Torrent.create(fileName, uris, "Wit.Jo");
        File f = new File(fileName + ".torrent");
        t.save(new FileOutputStream(f));
        for (Tracker tracker : trackers){
            tracker.announce(TrackedTorrent.load(f));
        }
    }

    public Tracker generateTrackerServer(InetAddress iddr) throws IOException{

        Tracker t = new Tracker(iddr);
        t.start();
        return t;
    }


}
