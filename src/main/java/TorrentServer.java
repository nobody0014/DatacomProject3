/**
 * Created by Wit on 11/25/2016 AD.
 */
import spark.Route;

import java.io.File;
import java.io.RandomAccessFile;

import static spark.Spark.*;

public class TorrentServer implements Runnable{
    public int TORRENT_DEFAULT_PORT;
    public String TORRENT_FILE_NAME;
    public long fileSize;
    public File f;
    public RandomAccessFile raf;

    public TorrentServer(int port, String torrentFileName){
        try{
            this.TORRENT_DEFAULT_PORT = port;
            this.TORRENT_FILE_NAME = torrentFileName;
            this.f =  new File(torrentFileName);
            this.raf = new RandomAccessFile(f,"r");
            this.fileSize = this.f.length();
        }catch (Exception e){}
    }

    public void run(){
        port(TORRENT_DEFAULT_PORT);
        get("/downloadTorrent", download());
        get("/downloadTorrentFileSize", downloadSize());
    }
    private Route download(){
        return (request, response) -> {
            System.out.println("Receving torrent file request from" + request.host());
            long byteStart,byteEnd;
            String[] byteRange = request.params("bytes").split("-");
            byteStart = Long.valueOf(byteRange[0]);
            if(byteRange.length > 1){
                byteEnd = Long.valueOf(byteRange[1]);
            }else{
                byteEnd = this.fileSize;
            }
            int length = (int) (byteEnd-byteStart);
            raf.seek(byteStart);
            return raf.read(new byte[length]);
        };
    }
    private Route downloadSize(){
        return (request, response) -> {
            return fileSize;
        };
    }

}
