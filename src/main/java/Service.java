import java.net.InetAddress;
import java.util.Set;
import javax.jmdns.*;


//Service discoverer
public class Service {
    ServiceDiscovery server;
    JmDNS jmdns;
    ServiceInfo serviceInfo;

    public Service(){
        try{
            server = new ServiceDiscovery();
            serviceInfo = ServiceInfo.create("_http._tcp.local.", "Heyooo", 80, "path=index.html");
            jmdns = JmDNS.create(InetAddress.getLocalHost());
        }catch (Exception e){System.out.println(e.getMessage());}

    }
    public void start(){
        server.run();
        register();
    }
    public void register(){
        try{
            ServiceInfo serviceInfo = ServiceInfo.create("_http._tcp.local.", "Heyooo", 80, "path=index.html");
            jmdns.registerService(serviceInfo);
        }catch (Exception e){System.out.println(e.getMessage());}

    }
    public void unregister(){
        jmdns.unregisterAllServices();
    }

    public Set<String> getIPs(){
        return server.getIPs();
    }
}
