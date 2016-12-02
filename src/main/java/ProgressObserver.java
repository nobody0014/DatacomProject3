import com.turn.ttorrent.client.Client;

import java.util.Observable;
import java.util.Observer;

/**
 * Created by Wit on 12/2/2016 AD.
 */
public class ProgressObserver implements Observer {
    double progress;

    public ProgressObserver(double progress){
        this.progress = progress;
    }

    public double getProgress(){
        return this.progress;
    }

    @Override
    public void update(Observable o, Object arg) {
        Client client = (Client) o;
        this.progress = client.getTorrent().getCompletion();
        System.out.printf("%s \r",client.getTorrent().getName() + "'s download progress: " + this.progress + "%");
    }
}
