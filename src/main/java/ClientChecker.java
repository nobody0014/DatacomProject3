/**
 * Created by Wit on 11/30/2016 AD.
 */
public class ClientChecker implements Runnable {
    Hub h;

    public ClientChecker(Hub h){
        this.h = h;
    }

    public synchronized void printStatus(){
        for (ExtendedClient ec : h.clients){
            System.out.println("Printing the client state of all interface");
            System.out.println(ec.client.getState());
            System.out.println("Done printing");
        }
    }

    public void run(){
        while (true){
            printStatus();
            try{
                Thread.sleep(5000);
            }catch (Exception e){}

        }
    }
}
