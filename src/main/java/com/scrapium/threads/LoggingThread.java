package com.scrapium.threads;

import com.scrapium.Scraper;
import com.scrapium.ThreadBase;
import com.scrapium.tweetium.TaskService;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;

public class LoggingThread extends ThreadBase implements Runnable {

    private final Instant scraperStart;
    private Scraper scraper;
    private AtomicInteger coroutineCount;
    private int lastRequestCount = 0;
    public AtomicInteger successRequestCount;
    public AtomicInteger failedRequestCount;

    private long startEpoch;
    private int lastSuccessCount;
    private int lastFailedCount;
    private long lastLogEpoch;

    private TaskService taskService;
    public LoggingThread(Scraper scraper, TaskService taskService) {
        this.scraper = scraper;
        this.taskService = taskService;
        this.successRequestCount = new AtomicInteger(0);
        this.failedRequestCount = new AtomicInteger(0);
        this.startEpoch = System.currentTimeMillis() / 1000;

        this.scraperStart = Instant.now();
    }

    public static String format(Duration d) {
        long days = d.toDays();
        d = d.minusDays(days);
        long hours = d.toHours();
        d = d.minusHours(hours);
        long minutes = d.toMinutes();
        d = d.minusMinutes(minutes);
        long seconds = d.getSeconds() ;
        return
                (days ==  0?"":days+" days,")+
                        (hours == 0?"":hours+" hours,")+
                        (minutes ==  0?"":minutes+" minutes,")+
                        (seconds == 0?"":seconds+" seconds,");
    }

    @Override
    public void run() {
        while (this.running) {

            long currentEpoch = System.currentTimeMillis() / 1000;

            int successDelta = this.successRequestCount.get() - this.lastSuccessCount;
            int failedDelta = this.failedRequestCount.get() - this.lastFailedCount;

            double successPS = successDelta / (currentEpoch - this.lastLogEpoch);
            double failedPS = failedDelta / (currentEpoch - this.lastLogEpoch);

            int secondSinceStart = (int) (currentEpoch - this.startEpoch);

            double successPSTotal = this.successRequestCount.get() / (secondSinceStart == 0 ? 1 : secondSinceStart);

            String out = "\n\n=== Tweet Scraper ===\n";
            out += ("Requests : " + (this.successRequestCount.get() + this.failedRequestCount.get())) + "\n";
            out += ("Success/s: " + (successPS)) + "\n";
            out += ("Success Total/s: " + (successPSTotal)) + "\n";
            out += ("Failed/s: " + (failedPS)) + "\n";
            out += ("Available Proxies: " + (this.scraper.proxyService.getAvailableProxyCount())) + "\n";
            out += ("Running for: " + format(Duration.between(this.scraperStart, Instant.now())));

            System.out.println(out);

            /*
            if(successPS == 0 && Duration.between(this.scraperStart, Instant.now()).toSeconds() > 30  ){
                System.out.println("Requests per second == 0... EXITING.");
                System.exit(0);
            }*/

            this.lastSuccessCount = this.successRequestCount.get();
            this.lastFailedCount = this.failedRequestCount.get();
            this.lastLogEpoch = System.currentTimeMillis() / 1000;


            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
    }

    public void increaseSuccessRequestCount(){
        successRequestCount.incrementAndGet();
    }

    public void increaseFailedRequestCount(){
        failedRequestCount.incrementAndGet();
    }


}
