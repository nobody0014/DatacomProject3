/**
 * Created by Wit on 11/22/2016 AD.
 */
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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.channels.UnsupportedAddressTypeException;
import java.net.*;
import java.io.*;
import java.util.*;

//Client used for downloading, right now using ttorent protocol
public class Hub {
    public Hub(){}

    //Downloading the file from people
    //Downloading steps
    //1) need to ip address and the port of the tracker
    //2) go into the tracker and register itself
    //3) start the client download
    public void download(String fileName){
        try{
            startClient(false,0.0,0.0,fileName,".",null);
        }catch (Exception e){e.printStackTrace();}

    }

    //Uploading the file to people
    //Uploading steps
    //1) create torrent of the file
    //2) create tracker server of the file
    //3) sends the tracker server of the file to people
    //4) start the damn client
    public void upload(String fileName){
        try{
            upload(fileName,Inet4Address.getLocalHost().getHostAddress());
        }catch (Exception e){e.printStackTrace();}
    }
    public void upload(String fileName, String url){
        try{
            List<String> announceURLs  = new ArrayList<>();
            announceURLs.add(url);
            generateTorrentFile(".", fileName, announceURLs);
            generateTrackerServer(6969,".");
            startClient(true,0.0,0.0,fileName,".",null);
        }catch (Exception e){e.printStackTrace();}
    }

    //isMaster is telling if you are the downloader
    //ifacevalue --> own ipaddress
    //outputValue is the directory --> "." as default
    //maxupload/maxdownload can be set
    //filename --> is the downloading file name
    public void startClient(boolean isMaster, double maxDownloadRate, double maxUploadRate,
                            String fileName, String outputValue, String ifaceValue){
        try {
            Client c = new Client(
                    getIPv4Address(ifaceValue),
                    SharedTorrent.fromFile(
                            new File(fileName + ".torrent"),
                            new File(outputValue)));
            c.setMaxDownloadRate(maxDownloadRate);
            c.setMaxUploadRate(maxUploadRate);
            if(isMaster){c.share();}
            else{c.download();}
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //src is the folder --> "."
    //fileName is the original file name
    //announceURL would require the damn IP of the tracker file, default port is 6969
    public void generateTorrentFile(String src, String fileName, List<String> announceURLs)
            throws InterruptedException, IOException, NoSuchAlgorithmException {
        OutputStream fos = null;
        try{
            fos = new FileOutputStream(fileName);
            List<URI> announceURIs = new ArrayList<URI>();
            for (String url : announceURLs) {
                announceURIs.add(new URI(url));
            }
            List<List<URI>> announceList = new ArrayList<List<URI>>();
            announceList.add(announceURIs);
            File source = new File(src);
            String creator = String.format("%s (ttorrent)", System.getProperty("user.name"));
            Torrent torrent;
            if (source.isDirectory()) {
                List<File> files = new
                        ArrayList<>(FileUtils.listFiles(source, TrueFileFilter.TRUE, TrueFileFilter.TRUE));
                Collections.sort(files);
                torrent = Torrent.create(source,files,announceList,creator);
            } else {
                torrent = Torrent.create(source,announceList,creator);
            }
            torrent.save(fos);
        }catch(Exception e){e.printStackTrace();}
        finally {if(fos != System.out){IOUtils.closeQuietly(fos);}}
    }
    public void generateTrackerServer(int port, String directory){
        FilenameFilter filter =  (dir, name) -> {return name.endsWith(".torrent");};
        try {
            Tracker t = new Tracker(new InetSocketAddress(port));
            File parent = new File(directory);
            for (File f : parent.listFiles(filter)) {
                t.announce(TrackedTorrent.load(f));
            }

            t.start();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(2);
        }
    }
    private static Inet4Address getIPv4Address(String iface)
            throws SocketException, UnsupportedAddressTypeException, UnknownHostException {
        if (iface != null) {
            Enumeration<InetAddress> addresses =
                    NetworkInterface.getByName(iface).getInetAddresses();
            while (addresses.hasMoreElements()) {
                InetAddress addr = addresses.nextElement();
                if (addr instanceof Inet4Address) {
                    return (Inet4Address)addr;
                }
            }
        }

        InetAddress localhost = InetAddress.getLocalHost();
        if (localhost instanceof Inet4Address) {
            return (Inet4Address)localhost;
        }

        throw new UnsupportedAddressTypeException();
    }

}
