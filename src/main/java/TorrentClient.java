/**
 * Created by Wit on 11/26/2016 AD.
 */
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;


public class TorrentClient {

    String fileName;
    String client;
    int port;


    public TorrentClient(String host, String fileName, int port){
        this.fileName = fileName + ".torrent";
        this.client = host;
        this.port = port;
    }

    public void download() throws URISyntaxException, IOException{
        HttpClient c = HttpClients.createDefault();

        //First get the size of the file
        System.out.println("Getting the torrent file size");
        HttpGet req = new HttpGet(new URI("http://" + this.client + ":" + this.port + "/downloadTorrentFileSize"));
        HttpResponse  res = c.execute(req);
        long size  = Long.valueOf(res.getLastHeader("size").getValue());
        System.out.println("torrent file size: " + size);

        //Second create the file with that size we have just got
        System.out.println("Creating torrent file: " + this.fileName);
        File f = new File(this.fileName);
        System.out.println("checking if file exists");
        if(!f.exists()){f.createNewFile();}
        System.out.println("Creating random access file");
        RandomAccessFile raf = new RandomAccessFile(f, "rw");


        raf.setLength(size);

        //Time for downloading
        System.out.println("Downloading the torrent file");
        int byteSize = 10000;

        int i = 0;
        while (i < size/byteSize){
            HttpGet reqFile = new HttpGet(new URI("http://" + this.client + ":" + this.port + "/downloadTorrentFile"));
            reqFile.setHeader("bytes", (i*byteSize)+ "-" + ((i+1)*byteSize));
            res = c.execute(reqFile);
            System.out.println("bytes" + ": " +  (i*byteSize)+ "-" + ((i+1)*byteSize));
            System.out.println(res.getStatusLine().getStatusCode());
            if(res.getStatusLine().getStatusCode() >= 200 && res.getStatusLine().getStatusCode() <= 299){
                raf.seek(i*byteSize);
                raf.write(IOUtils.toByteArray(res.getEntity().getContent()));
                i++;
            }
        }

        //Download last one if there's one
        if(byteSize * (size/byteSize) != size){
            HttpGet reqFile = new HttpGet(new URI("http://" + this.client + ":" + this.port + "/downloadTorrentFile"));
            reqFile.setHeader("bytes", (i*byteSize)+ "-");
            i = 0;
            while (i < 1){
                res = c.execute(reqFile);
                if(res.getStatusLine().getStatusCode() >= 200 && res.getStatusLine().getStatusCode() <= 299){
                    raf.seek(byteSize * (size/byteSize));
                    raf.write(IOUtils.toByteArray(res.getEntity().getContent()));
                    i++;
                }
            }
        }
        System.out.println("Finish download torrent file");

    }




}
