
import org.eclipse.jetty.util.ConcurrentHashSet;

import java.net.*;
import java.util.*;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceListener;

    public class ServiceDiscovery implements Runnable  {
        private class  SampleListener  implements ServiceListener{
            public SampleListener(){
                super();
            }
            @Override
            public void serviceAdded(ServiceEvent event)  {}
            @Override
            public void serviceRemoved(ServiceEvent event) {
                for(Inet4Address addr : event.getInfo().getInet4Addresses()){
                    try{
                        ipList.remove(addr.getHostAddress());
                        System.out.println("Removed: " + addr.getHostAddress());
                    }catch (Exception e){System.out.println(e.getMessage());}
                }
            }
            @Override
            public void serviceResolved(ServiceEvent event){

                for(Inet4Address addr : event.getInfo().getInet4Addresses()){
                    try{
                        System.out.println("Detecting IP: " + addr.getHostAddress());
                        ipList.add(addr.getHostAddress());
                        System.out.println("Added: " + addr.getHostAddress());
                    }catch (Exception e){System.out.println(e.getMessage());}
                }
            }
        }
        SampleListener sl;
        String serviceName;
        List<JmDNS> jmDNSList;
        ConcurrentHashSet<String> ipList;
        public ServiceDiscovery(String serviceName){
            this.serviceName = serviceName;
            this.jmDNSList = new ArrayList<>();
            this.ipList = new ConcurrentHashSet<>();
        }
        public void run() {
            try {
                // iterate over the network interfaces known to java
                Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
                for (NetworkInterface interface_ : Collections.list(interfaces)) {
                    // iterate over the addresses of that interface
                    Enumeration<InetAddress> addresses = interface_.getInetAddresses();
                    for (InetAddress address : Collections.list(addresses)) {
                        if (address instanceof  Inet4Address) {
                            //create an JmDNS instance of this address
                            JmDNS jmdns = JmDNS.create(address);
                            this.sl = new SampleListener();
                            //make that address of a certain interface listen to a specified serviceName
                            jmdns.addServiceListener(this.serviceName, this.sl);
                            this.jmDNSList.add(jmdns);
                        }
                    }
                }

            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
        public Set<String> getIPs(){
            return this.ipList;
        }
    }
