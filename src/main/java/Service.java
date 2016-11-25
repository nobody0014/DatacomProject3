import java.net.InetAddress;
import java.util.Set;
import javax.jmdns.*;


//Service discoverer
public class Service {
    ServiceDiscovery server;
    JmDNS jmdns;
    ServiceInfo serviceInfo;
    String serviceName;

    public Service(String serviceName){
        try{
            this.serviceName = serviceName;
            this.server = new ServiceDiscovery(this.serviceName);
            this.serviceInfo = ServiceInfo.create(this.serviceName, "Hello", 80, "path=index.html");
            this.jmdns = JmDNS.create(InetAddress.getLocalHost());
        }catch (Exception e){System.out.println(e.getMessage());}

    }
    public void start(){
        this.server.run();
        register();
    }
    public void register(){
        try{
            this.jmdns.registerService(this.serviceInfo);
        }catch (Exception e){System.out.println(e.getMessage());}

    }
    public void unregister(){
        this.jmdns.unregisterAllServices();
    }

    public Set<String> getIPs(){
        return this.server.getIPs();
    }
}
