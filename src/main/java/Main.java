
import spark.Route;
import static spark.Spark.*;


//File where everything start, but we probably wont use it till the project is done or for testing our classes
//Comment stuff out before writing your crap and make sure u dont push and pull anyhow
public class Main {

    enum Status{WAITING, WORKING}

    static class Config {
        public static final int DEFAULT_PORT = 19999;
        public static final int TORRENT_DEFAULT_PORT = 29999;
    }
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


    //For downloading --> anyone that is not master --> will be invoke by master itself
    private static Route download() {
        return (request, response) -> {


            String toReturn = null;

            //Same thing as upload
            if(ClientStatus == Status.WAITING){
                ClientStatus = Status.WORKING;

                String fileName = request.params("fileName");
                String host = request.host();
                int port = request.port();


                TorrentClient tc = new TorrentClient(port,host,fileName);
                Hub hub = new Hub();

                //Start the download, this is done serially, 1) get the torrent file, 2) start the download
                tc.download();
                hub.download(fileName);


                ClientStatus = Status.WAITING;
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

                String fileName = request.params("fileName");

                Notifier nf = new Notifier(fileName,s.getIPs(),Config.DEFAULT_PORT);
                TorrentServer ts = new TorrentServer(Config.TORRENT_DEFAULT_PORT, fileName+".torrent");
                Hub hub = new Hub();

                //Upload the file and set everything up and then run the torrent server for people to get torrent file
                hub.upload(fileName);
                ts.run();

                //Sleep for 2 seconds so that the torrent server can get up and running
                Thread.sleep(2000);

                //run the notifications so the system can start
                nf.run();


                ClientStatus = Status.WAITING;
            }
            return toReturn;
        };
    }

}
