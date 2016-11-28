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
    String host;
    int port;


    public TorrentClient(int port, String host, String fileName){
        this.fileName = fileName + ".torrent";
        this.host = host;
        this.port = port;
    }

    public void download() throws URISyntaxException, IOException{
        HttpClient c = HttpClients.createDefault();

        //First get the size of the file
        HttpGet req = new HttpGet(new URI("http://" + host + ":" + port + "/downloadTorrentFileSize"));
        HttpResponse  res = c.execute(req);
        long size  = Long.valueOf(res.getLastHeader("").getValue());

        //Second create the file with that size we have just got
        File f = new File(this.fileName);
        RandomAccessFile raf = new RandomAccessFile(f, "w");

        //Time for downloading
        int byteSize = 10000;
        HttpGet reqFile = new HttpGet(new URI("http://" + host + ":" + port + "/downloadTorrentFile"));
        int i = 0;
        while (i < size/byteSize){
            reqFile.setHeader("bytes", (i*byteSize)+ "-" + ((i+1)*byteSize));
            res = c.execute(reqFile);
            if(res.getStatusLine().getStatusCode() >= 200 && res.getStatusLine().getStatusCode() <= 299){
                raf.seek(i*byteSize);
                raf.write(IOUtils.toByteArray(res.getEntity().getContent()));
                i++;
            }
        }

        //Download last one if there's one
        if(byteSize * (size/byteSize) != size){
            req.setHeader("bytes", (byteSize * (size/byteSize))+ "-");
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
        //Done

    }




}
