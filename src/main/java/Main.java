import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.Inet4Address;
import java.util.*;
import java.io.*;
import java.util.concurrent.*;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.nio.client.HttpAsyncClient;
import spark.Route;
import static spark.Spark.*;
import org.apache.http.*;

//File where everything start, but we probably wont use it till the project is done or for testing our classes
//Comment stuff out before writing your crap and make sure u dont push and pull anyhow
public class Main {
    static class Config {
        public static final int DEFAULT_PORT = 19999;
        public static final int TORRENT_DEFAULT_PORT = 29999;
    }

    public static void main(String[] args){
        Service s = new Service("_http._tcp.local.");
        Hub h = new Hub();
        s.start();
        //s.registered()
        port(Config.DEFAULT_PORT);

        //Route
        post("/download", download(h));
        post("/upload", upload(h,s));
    }
    private static Route download(Hub h) {
        return (request, response) -> {
            String host = request.host();
            int port = request.port();
            String fileName = request.params("FileName");
            String toReturn = null;
            System.out.println("Received download command from: " + host);
            System.out.println("Will attempt to download the torrrent file: " + fileName);
            TorrentClient tc = new TorrentClient(port,host,fileName);
            tc.download();
            System.out.println("Finish downloading the torrent file, connecting to the server");
            h.download(fileName);
            return toReturn;
        };
    }

    private static Route upload(Hub h, Service s) {
        return (request, response) -> {
            System.out.println("Received upload command");
            String fileName = request.params("fileName");
            String toReturn = null;
            Notifier n = new Notifier(fileName,s.getIPs(),Config.DEFAULT_PORT);
            TorrentServer ts = new TorrentServer(Config.TORRENT_DEFAULT_PORT,fileName + ".torrent");
            if ( fileName != null && fileName.length() > 0) {
                h.upload(fileName);
                ts.run();
                //Sleep so that the torrent server has time to set itself up
                Thread.sleep(2000);
                n.run();
            }
            else {
                toReturn = "Invalid fileName";
                halt(400, toReturn);
            }
            return toReturn;
        };
    }
}
