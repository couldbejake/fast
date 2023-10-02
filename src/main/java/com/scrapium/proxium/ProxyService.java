package com.scrapium.proxium;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class ProxyService {


    private final Random rand;
    private ArrayList<Proxy> proxies;
    private ArrayList<Proxy> availableProxies;

    public ProxyService (){
        this.proxies = new ArrayList<Proxy>();
        this.availableProxies = new ArrayList<Proxy>();
        this.rand = new Random();
    }

    public void loadProxies() {
        synchronized (this.proxies){
            try (BufferedReader br = new BufferedReader(new FileReader("./checked_proxies.txt"))) {
                String _proxy_entry;

                int i = 0;

                while ((_proxy_entry = br.readLine()) != null) {

                    String proxy_entry = _proxy_entry.replaceAll("[\\r\\n]+", "");
                    String connString = proxy_entry;

                    Proxy proxy = new Proxy(
                            i++,
                            connString,
                            0,
                            0,
                            0,
                            0,
                            new Timestamp(0)
                    );

                    //System.out.println("added ");
                    //System.out.print(proxy.toString());

                    this.proxies.add(proxy);

                }

                System.out.println("Loaded (" + i + ") proxies!");
                /*
            } catch (SQLException e) {
                e.printStackTrace();
                System.out.println("Failed to get connection!");
            }*/

            } catch (IOException e) {
                System.err.format("IOException: %s%n", e);
            }
        }
    }

    /*
            String query = "SELECT id, connection_string, usage_count, success_count, failed_count, fail_streak, cooldown_until " +
                "FROM test_proxy " +
                "WHERE (cooldown_until IS NULL OR NOW() > cooldown_until) " +
                "ORDER BY CASE WHEN usage_count = 0 THEN 1 ELSE success_count / usage_count END DESC, last_used ASC " +
                "LIMIT 50";
     */

    public void updateAvailableProxies(){
        synchronized (this.availableProxies){
            this.availableProxies = new ArrayList<Proxy>();

            // benchmark the below for a better solution.
            for (int i = 0; i < this.proxies.size(); i++){
                // TODO: check isCoolDown function
                if(!this.proxies.get(i).inCoolDown()){
                    availableProxies.add(this.proxies.get(i));
                }
            }
        }

        //System.out.println("Available proxy count = " + this.availableProxies.size());

        synchronized (this.availableProxies) {
            if (this.availableProxies.size() < 50) {
                System.out.println("!! INCREDIBLY LOW AVAILABLE PROXY POOL SIZE (" + availableProxies.size() + ")");
            }
        }


        synchronized (this.availableProxies) {
            //System.out.println("[proxyman] (" + availableProxies.size() + ") available proxies.");
            Collections.sort(availableProxies, new Comparator<Proxy>() {
                public int compare(Proxy p1, Proxy p2) {
                    return Integer.compare(p2.getSuccessDelta() - p2.getFailedCount(), p1.getSuccessDelta() - p2.getFailedCount());
                }
            });
        }

        //System.out.println("[proxyman] Sorted available proxies!");
    }

    // get one of the top 50 proxies, that aren't currently banned.
    public Proxy getNewProxy() {

        boolean proxyInCoolDown = true;
        int attempts = 0;

        Proxy randomProxy = null;

        while(proxyInCoolDown && attempts <= 150){
            synchronized (this.availableProxies) {

                if (availableProxies.size() == 0) {
                    System.out.println("No available Proxies....");
                    return randomProxy;
                }
                int randInd = rand.nextInt(30);
                if (randInd > availableProxies.size()) {
                    randInd = availableProxies.size() - 1;
                }
                randomProxy = availableProxies.get(randInd);
                proxyInCoolDown = randomProxy.inCoolDown();
                attempts++;
            }
        }

        if(attempts > 100){
            System.out.println("Warning: iterated over 150 random proxies and couldn't find a viable proxy NOT in cooldown.");

            // TODO: reset all proxies
        }

        return randomProxy;
    }

    public int getAvailableProxyCount(){
        synchronized (this.availableProxies){
            return availableProxies.size();
        }
    }
}