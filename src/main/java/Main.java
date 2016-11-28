import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.Inet4Address;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.io.*;
import java.util.concurrent.*;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.nio.client.HttpAsyncClient;
import spark.Route;
import static spark.Spark.*;
import org.apache.http.*;

import javax.print.URIException;

//File where everything start, but we probably wont use it till the project is done or for testing our classes
//Comment stuff out before writing your crap and make sure u dont push and pull anyhow
public class Main {
    static class Config {
        public static final int DEFAULT_PORT = 19999;
        public static final int TORRENT_DEFAULT_PORT = 29999;
    }
    static enum Status{WAITING, WORKING}
    static Status ClientStatus;

    public static void main(String[] args){
        ClientStatus = Status.WAITING;
        Service s = new Service("_http._tcp.local.");
        s.start();

        //s.registered()
        port(Config.DEFAULT_PORT);

        //Route
        post("/download", download());
        post("/upload", upload(s));
    }
    private static Route download() {
        return (request, response) -> {
            String toReturn = null;
            if(ClientStatus == Status.WAITING){
                ClientStatus = Status.WORKING;

                String fileName = request.params("fileName");
                String host = request.host();
                int port = request.port();

                TorrentClient tc = new TorrentClient(port,host,fileName);
                Hub hub = new Hub();

                tc.download();
                hub.download(fileName);


                ClientStatus = Status.WAITING;
            }
            return toReturn;
        };
    }

    private static Route upload(Service s) {
        return (request, response) -> {
            String toReturn = null;
            if(ClientStatus == Status.WAITING){
                ClientStatus = Status.WORKING;

                String fileName = request.params("fileName");

                Notifier nf = new Notifier(fileName,s.getIPs(),Config.DEFAULT_PORT);
                TorrentServer ts = new TorrentServer(Config.TORRENT_DEFAULT_PORT, fileName+".torrent");
                Hub hub = new Hub();


                hub.upload(fileName);
                ts.run();


                Thread.sleep(2000);


                nf.run();


                ClientStatus = Status.WAITING;
            }
            return toReturn;
        };
    }

}
