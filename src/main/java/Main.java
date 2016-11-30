
import spark.Route;
import java.io.File;
import java.io.RandomAccessFile;
import java.util.concurrent.CountDownLatch;

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

    static CountDownLatch uploaderLatch;
    static CountDownLatch downloaderLatch;

    //For TorrentServer
    static Level ClientLevel;
    static long fileSize;
    static String TORRENT_FILE_NAME;
    static File f;
    static RandomAccessFile raf;


    public static void main(String[] args){
        ClientStatus = Status.WAITING;
        ClientLevel = Level.DOWNLOADER;

        Service s = new Service("_http._tcp.local.");
        s.start();


        //s.registered()
        port(Config.DEFAULT_PORT);

        //Route
        post("/download", download());
        post("/upload", upload(s));
        get("/downloadTorrentFile", downloadTorrentFile());
        get("/downloadTorrentFileSize", downloadTorrentFileSize());

    }


    //For downloading --> anyone that is not master --> will be invoke by master itself
    private static Route download() {
        return (request, response) -> {

            String toReturn = null;
            if (uploaderLatch != null){
                return null;
            }
            else if(ClientStatus == Status.WAITING){
                ClientStatus = Status.WORKING;
                System.out.println("Starting download process");


                String fileName = request.headers("fileName");
                String client = request.ip();
                System.out.println("Download information:");
                System.out.println("File Name: " + fileName);
                System.out.println("Client: " + client);

                TorrentClient tc = new TorrentClient(client,fileName,Config.DEFAULT_PORT);

                downloaderLatch = new CountDownLatch(1);
                Hub hub = new Hub(false, fileName, downloaderLatch);

                Thread thub = new Thread(hub);

                System.out.println("Downloading the torrent file");
                tc.download();

                System.out.println("Beginning the download of the actual file");
                thub.start();


//                downloaderLatch.await();
//                ClientStatus = Status.WAITING;
            }
            return toReturn;
        };
    }


    //For uploading --> anyone that is the master --> will be invoke by external command
    private static Route upload(Service s) {
        return (request, response) -> {

            String toReturn = null;

            //If the client is working, there is a file being transmitted in the system
            if(ClientStatus == Status.WAITING){
                ClientStatus = Status.WORKING;
                System.out.println("Switched to Working mode");

                String fileName = request.headers("fileName");
                File fn = new File(fileName);


                if(!fn.exists()){
                    System.out.println("Does not detect file on this directory");
                }
                else{
                    System.out.println("File detected in current directory, beginning upload process");

                    System.out.println("Setting up");
                    //Setups --> used threads too
                    Notifier nf = new Notifier(fileName,s.getIPs(),Config.DEFAULT_PORT);

                    uploaderLatch = new CountDownLatch(1);
                    Hub hub = new Hub(true,fileName, uploaderLatch);

                    ClientChecker cc = new ClientChecker(hub);

                    Thread th = new Thread(hub);
                    Thread tnf = new Thread(nf);
                    Thread ttc = new Thread(cc);

                    //Start the hub in another thread
                    System.out.println("Starting hub");
                    th.start();

                    ClientLevel = Level.UPLOADER;

                    //Sleep for 2 second so that the hub can get everythin in place
                    System.out.println("Waiting for the hub to start");
                    Thread.sleep(2000);


                    System.out.println("Setting up torrent file server");
                    setUpTorrentFileServer(fileName + ".torrent");


                    //run the notification thread so the system-wide download can start
                    System.out.println("Starting the notification");
                    tnf.start();


                    System.out.print("Starting the checker");
                    ttc.start();

//                    System.out.println("Waitin for all seeding to be over");
//                    uploaderLatch.await();
                }


//                ClientStatus = Status.WAITING;
//                ClientLevel = Level.DOWNLOADER;
//                System.out.println("Switched to Waiting mode");
            }
            return toReturn;
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
            return null;
        };
    }

    private static Route downloadTorrentFileSize() {
        return (request, response) -> {
            if(ClientLevel == Level.UPLOADER){
                System.out.println("Request for torrent file size coming in from " + request.ip());
                response.header("size",String.valueOf(fileSize));
                return fileSize;
            }
            return null;
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
