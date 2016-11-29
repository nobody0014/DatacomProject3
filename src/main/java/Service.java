
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.util.Set;
import javax.jmdns.*;
import java.util.*;
import java.*;
import java.net.*;


//Service discoverer
public class Service {
    ServiceDiscovery server;
    List<JmDNS> jmdnss;
    JmDNS jmdns;
    ServiceInfo serviceInfo;
    String serviceName;

    public Service(String serviceName){
        this.jmdnss = new ArrayList<>();
        try{
            // iterate over the network interfaces known to java
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            for (NetworkInterface interface_ : Collections.list(interfaces)) {

                // iterate over the addresses associated with the interface
                Enumeration<InetAddress> addresses = interface_.getInetAddresses();
                for (InetAddress address : Collections.list(addresses)) {
                    if (address instanceof  Inet4Address) {
                        this.jmdnss.add(JmDNS.create(address));
                    }
                }
            }
            this.serviceName = serviceName;
            this.server = new ServiceDiscovery(this.serviceName);
            this.serviceInfo = ServiceInfo.create(this.serviceName, "Hello", 80, "path=index.html");

        }catch (Exception e){System.out.println(e.getMessage());}
    }
    public void start(){
        this.server.run();
        register();
    }
    public void register(){
        try{
            for(JmDNS jmdns : jmdnss){
                jmdns.registerService(this.serviceInfo);
            }
        }catch (Exception e){System.out.println(e.getMessage());}

    }
    public void unregister(){
        for (JmDNS jmdns : jmdnss){
            jmdns.unregisterAllServices();
        }
    }

    public Set<String> getIPs(){
        return this.server.getIPs();
    }
}
