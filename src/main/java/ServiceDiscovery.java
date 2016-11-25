
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
                ipList = s;
            }
            @Override
            public void serviceAdded(ServiceEvent event)  {}
            @Override
            public void serviceRemoved(ServiceEvent event) {
                for(InetAddress addr : event.getInfo().getInetAddresses()){
                    try{
                        if(!addr.getHostAddress().equals(InetAddress.getLocalHost().getHostAddress())){
                            ipList.remove(addr.getHostAddress());
                        }
                        System.out.println("Removed: " + InetAddress.getLocalHost().getHostAddress());
                    }catch (Exception e){System.out.println(e.getMessage());}
                }
            }
            @Override
            public void serviceResolved(ServiceEvent event){
                for(InetAddress addr : event.getInfo().getInetAddresses()){
                    try{
                        System.out.println("Detecting foreign IP: " + InetAddress.getLocalHost().getHostAddress());
                        if(!addr.getHostAddress().equals(InetAddress.getLocalHost().getHostAddress())){
                            ipList.add(addr.getHostAddress());
                        }
                        System.out.println("Added: " + InetAddress.getLocalHost().getHostAddress());
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
                JmDNS jmdns = JmDNS.create(InetAddress.getLocalHost());
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

