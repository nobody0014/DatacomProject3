
import java.net.Inet4Address;
import java.util.*;
import java.io.IOException;
import java.lang.reflect.Array;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceListener;


    public class ServiceDiscovery implements Runnable  {
        private class  SampleListener  implements ServiceListener{
            Set<String> ipList;
            public SampleListener(Set<String> s){
                super();
                ipList = s;
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
        public ServiceDiscovery(String serviceName){
            this.serviceName = serviceName;
        }

        public void run() {
            try {
                // Create a JmDNS instance
                JmDNS jmdns = JmDNS.create(Inet4Address.getLocalHost());
                // Add a service listener
                this.sl = new SampleListener(new HashSet<>());
                jmdns.addServiceListener(this.serviceName, this.sl);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
        public Set<String> getIPs(){
            return this.sl.ipList;
        }
    }

