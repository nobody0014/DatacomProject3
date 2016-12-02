
import jargs.gnu.CmdLineParser;
import spark.Request;
import spark.Route;
import java.io.File;
import java.io.RandomAccessFile;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import org.slf4j.*;

import static spark.Spark.*;


//File where everything start, but we probably wont use it till the project is done or for testing our classes
//Comment stuff out before writing your crap and make sure u dont push and pull anyhow
public class Main {

    enum Status{WAITING, WORKING}
    enum Level{UPLOADER,DOWNLOADER}

    static class Config {
        public static final int DEFAULT_PORT = 19999;
        public static final int TORRENT_DEFAULT_PORT = 29999;
    }

    //Used to check if client is busy or not
    static Status ClientStatus;

    //For checking the completion of the entire download --> for master --> use to reset the state of all client
    static ConcurrentHashMap<String,Boolean> statusCheckers = new ConcurrentHashMap<>();


    //For TorrentServer
    static Level ClientLevel;
    static long fileSize;
    static String TORRENT_FILE_NAME;
    static File f;
    static RandomAccessFile raf;

    static String ip;



    public static void main(String[] args){
        ClientStatus = Status.WAITING;
        ClientLevel = Level.DOWNLOADER;


        //Getting the ip from the command
        CmdLineParser parser = new CmdLineParser();
        CmdLineParser.Option iddr = parser.addStringOption('i', "ip");
        try {
            parser.parse(args);
        } catch (Exception oe) {
            System.err.println(oe.getMessage());
            System.exit(1);
        }

        ip = (String) parser.getOptionValue(iddr);


        Service s = new Service("_http._tcp.local.");
        Hub h = new Hub();
        StatusReporter sr = new StatusReporter();
        s.start();

        Logger log = LoggerFactory.getLogger(Main.class);
        log.info("Main");

        //s.registered()
        port(Config.DEFAULT_PORT);

        before((request, response) -> {
            log.info(requestInfoToString(request));
        });

        //Route
        post("/download", download(h,sr));
        post("/upload", upload(h,s));
        post("/reportClientStatus", reportClientStatus());
        post("/resetSystem", resetSystem(h,sr));
        get("/downloadTorrentFile", downloadTorrentFile());
        get("/downloadTorrentFileSize", downloadTorrentFileSize());
    }

    private static String requestInfoToString(Request request) {
        StringBuilder sb = new StringBuilder();
        sb.append(request.requestMethod());
        sb.append(" " + request.url());
        sb.append(" " + request.body());
        return sb.toString();
    }


    //For downloading --> anyone that is not master --> will be invoke by master itself
    private static Route download(Hub h, StatusReporter sr) {
        return (request, response) -> {

            if(ClientStatus == Status.WAITING){
                ClientStatus = Status.WORKING;
                System.out.println("Starting download process");


                String fileName = request.headers("fileName");
                String client = request.ip();
                System.out.println("Download information:");
                System.out.println("File Name: " + fileName);
                System.out.println("Client: " + client);

                TorrentClient tc = new TorrentClient(client,fileName,Config.DEFAULT_PORT);

                CountDownLatch latch = new CountDownLatch(1);
                h.setLatch(latch);
                h.setSetting(false,fileName);
                h.setHostIddr(ip);
                sr.setMasterIP(client);
                sr.setHub(h);


                System.out.println("Downloading the torrent file");
                tc.download();

                System.out.println("Starting the download of the actual file");
                h.start();

                latch.await();

                System.out.println("Starting the client status reporter");
                sr.start();

            }else{System.out.println("The client is working");}
            return "started the download";
        };
    }


    //For uploading --> anyone that is the master --> will be invoke by external command
    private static Route upload(Hub h, Service s) {
        return (request, response) -> {

            String fileName = request.headers("fileName");
            File fn = new File(fileName);


            //If the client is working, there is a file being transmitted in the system
            if(ClientStatus == Status.WAITING){
                if(!fn.exists()){
                    System.out.println("Does not detect file on this directory");
                }
                else{
                    System.out.println("File detected in current directory, beginning upload process");

                    ClientStatus = Status.WORKING;
                    System.out.println("Switched to Working mode");

                    System.out.println("Setting up");
                    CountDownLatch latch = new CountDownLatch(1);

                    //Setups --> used threads too
                    h.setSetting(true,fileName);
                    h.setHostIddr(ip);
                    h.setLatch(latch);

                    ResetNotifier rn = new ResetNotifier(statusCheckers);

                    Notifier nf = new Notifier(fileName,s.getIPs(),Config.DEFAULT_PORT);
                    Thread tnf = new Thread(nf);

                    //Start the hub in another thread
                    System.out.println("Starting hub");
                    h.start();

                    ClientLevel = Level.UPLOADER;

                    //wait for the hub to create torrent server, we will use latch cus we have to be sure
                    System.out.println("Waiting for the hub to start");
                    latch.await();


                    System.out.println("Setting up torrent file server");
                    setUpTorrentFileServer(fileName + ".torrent");


                    //run the notification thread so the system-wide download can start
                    System.out.println("Starting the notification");
                    tnf.start();

                    //Starting the reset thread used for when all downloads are done
                    System.out.println("Starting the reset checking");
                    rn.start();

                }
            }else{System.out.println("The client is working");}
            return "Started the upload";
        };
    }



    private static Route reportClientStatus() {
        return (request, response) -> {
            System.out.println("Report coming in from: " + request.ip());
            if(ClientStatus == Status.WORKING){
                statusCheckers.putIfAbsent(request.ip(),false);
                if (request.headers("status") != null){
                    if(Double.valueOf(request.headers("status")) >= 100.0){
                        statusCheckers.put(request.ip(), true);
                    }else{statusCheckers.put(request.ip(), false);}
                }
                System.out.printf("%s \r",request.ip() + "'s completion Status: " + request.headers("status"));
            }else{System.out.println("The client is not working");}
            return "Report received";
        };
    }


    private static Route resetSystem(Hub h, StatusReporter sr){
        return (request, response) -> {
            if(ClientStatus == Status.WORKING){
                System.out.println("System resetting");
                h.cleanUp();
                statusCheckers  = new ConcurrentHashMap<>();
                ClientStatus = Status.WAITING;
                ClientLevel = Level.DOWNLOADER;
                sr.stopThread();
                TORRENT_FILE_NAME = null;
                f =  null;
                raf = null;
                fileSize = 0;
            }
            return "System resetted";
        };
    }


    private static Route downloadTorrentFileSize() {
        return (request, response) -> {
            if(ClientLevel == Level.UPLOADER){
                System.out.println("Request for torrent file size coming in from " + request.ip());
                response.header("size",String.valueOf(fileSize));
                return fileSize;
            }
            return "Not possible to download";
        };
    }

    private static Route downloadTorrentFile() {
        return (request, response) -> {
            if(ClientLevel == Level.UPLOADER){
                System.out.println("Request for torrent file coming in from " + request.ip());
                long byteStart,byteEnd;
                String[] byteRange = request.headers("bytes").split("-");
                byteStart = Long.valueOf(byteRange[0]);
                if(byteRange.length > 1){
                    byteEnd = Long.valueOf(byteRange[1]);
                }else{
                    byteEnd = fileSize;
                }
                int length = (int) (byteEnd-byteStart);
                raf.seek(byteStart);
                byte[] toReturn = new byte[length];
                raf.read(toReturn);
                return toReturn;
            }
            return "Not possible to download";
        };
    }

    private static void setUpTorrentFileServer(String torrentFileName){
        try{
            TORRENT_FILE_NAME = torrentFileName;
            f =  new File(torrentFileName);
            raf = new RandomAccessFile(f,"r");
            fileSize = f.length();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

}
