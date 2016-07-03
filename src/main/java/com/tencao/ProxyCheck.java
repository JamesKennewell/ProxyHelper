package com.tencao;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

@SideOnly(Side.CLIENT)
public class ProxyCheck extends Thread{

    private static boolean hasChecked;

    /**
     * Runs a ping scan on all proxies
     */
    @Override
    public void run(){
        PHLogger.logInfo("Proxy check started");
        for (ProxyList s : ProxyList.values())
            try {
                Inet4Address inet4 = (Inet4Address) InetAddress.getByName(s.getIP());
                long start = System.currentTimeMillis();
                if(inet4.isReachable(1000)){
                    long end = System.currentTimeMillis();
                    long total = end-start;
                    System.out.println(total);
                    PHLogger.logInfo("ProxyHelper - " + s.name() + " ping - " + (int)total + "ms");
                    s.storePing((int)total);
                }
                else PHLogger.logWarn("ProxyHelper - " + s.name() + " Proxy timed out");

            } catch (UnknownHostException e) {
                PHLogger.logWarn("ProxyHelper - Unable to resolve " + s.name() + " Proxy");
            } catch (IOException e) {
                e.printStackTrace();
            }

        hasChecked = true;

        if (getFastest() != null)
            PHLogger.logInfo(getFastest().name() + " is the fastest proxy with " + getFastest().getPing() + "ms");
    }


    static boolean hasChecked(){
        return hasChecked;
    }


    /**
     * Returns the fastest Proxy
     * Can be null
     */
    static ProxyList getFastest(){
        return ProxyList.getFastest();
    }

    enum ProxyList {
        UK("ecz.org.uk", 25565),
        US("us.ecz.org.uk", 25565),
        EU("eu.ecz.org.uk", 25577);

        private final String ip;
        private int port;
        private int ping;

        ProxyList(String ip, int port) {this.ip = ip; this.port = port;}

        /**
         * Returns the IP associated with a Proxy
         */
        public String getIP(){
            return this.ip;
        }

        /**
         * Stores the ping for each proxy after a scan
         */
        public void storePing (int ms) {
            this.ping = ms;
        }

        /**
         * Returns the ping associated with a Proxy
         */
        public int getPing (){
            return this.ping;
        }

        /**
         * Returns the port associated with a Proxy
         */
        public int getPort (){
            return this.port;
        }

        /**
        * Returns the fastest proxy available
        * Will return null if no proxy is pingable
         */
        public static ProxyList getFastest() {
            int max = 5000;
            ProxyList fastest = null;
            for (ProxyList p: values()) if (p.ping != 0 && p.ping < max) {max = p.ping; fastest = p;}
            return fastest;
        }
    }
}
