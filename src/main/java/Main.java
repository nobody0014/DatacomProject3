import java.io.IOException;
import java.io.InterruptedIOException;

/**
 * Created by Wit on 11/22/2016 AD.
 */

//File where everything start, but we probably wont use it till the project is done or for testing our classes
//Comment stuff out before writing your crap and make sure u dont push and pull anyhow
public class Main {
    public static void main(String[] args) throws InterruptedException{
        Service service = new Service();
        service.start();
        Thread.sleep(2000);
        service.unregister();


    }
}
