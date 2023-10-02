package com.scrapium.threads;

import com.scrapium.Scraper;
import com.scrapium.ThreadBase;
import com.scrapium.proxium.ProxyService;

public class ProxyThread extends ThreadBase implements Runnable {


    private final Scraper scraper;
    private final ProxyService proxyService;

    public ProxyThread(Scraper scraper, ProxyService proxyService) {
        this.scraper = scraper;
        this.proxyService = proxyService;
        this.proxyService.loadProxies();
    }

    @Override
    public void run() {
        while (this.running) {

            this.proxyService.updateAvailableProxies();

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}