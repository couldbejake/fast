package com.scrapium;

import com.scrapium.proxium.Proxy;
import com.scrapium.threads.LoggingThread;
import com.scrapium.tweetium.TaskService;
import com.scrapium.tweetium.TweetTask;
import com.scrapium.utils.DebugLogger;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.CredentialsProvider;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.asynchttpclient.*;
import org.asynchttpclient.proxy.ProxyServer;
import org.asynchttpclient.proxy.ProxyType;

import javax.net.ssl.*;

import static org.asynchttpclient.Dsl.*;


import java.io.IOException;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Base64;

public class TweetThreadTaskProcessor {
    private AsyncHttpClient c;
    private final DefaultAsyncHttpClientConfig clientConfig;

    /*
        Notes TODO:
            - AtomicReference isn't efficient (create a new object instead)
     */

    private Scraper scraper;
    private TaskService taskService;
    private final int threadID;
    private volatile boolean  tweetThreadRunning;
    private AtomicInteger coroutineCount;

    private int requestCount;
    private Instant lastCleanup;

    private final boolean DO_CLEANUP = false;

    private SSLContext createSslContext() throws Exception {
        X509TrustManager tm = new X509TrustManager() {

            public void checkClientTrusted(X509Certificate[] xcs,
                                           String string) throws CertificateException {
            }

            public void checkServerTrusted(X509Certificate[] xcs,
                                           String string) throws CertificateException {
            }

            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }
        };

        SSLContext ctx = SSLContext.getInstance("TLS");
        ctx.init(null, new TrustManager[] { tm }, null);
        return ctx;
    }

    public TweetThreadTaskProcessor(int threadID, boolean running, Scraper scraper, TaskService taskService, AtomicInteger coroutineCount) {
        this.threadID = threadID;
        this.scraper = scraper;
        this.taskService = taskService;
        this.coroutineCount = coroutineCount;
        this.tweetThreadRunning = running;



        this.clientConfig = new DefaultAsyncHttpClientConfig.Builder()
                .setConnectTimeout(8000)
                .setRequestTimeout(8000)
                .setReadTimeout(5000)
                .setMaxConnections(5000)
                .setMaxRequestRetry(1)
                .build();

        this.c = asyncHttpClient(this.clientConfig);


        this.lastCleanup = Instant.now();

    }

    public void doClientCleanupTick(){
        if(DO_CLEANUP){
            if(this.lastCleanup.isBefore(Instant.now().minusSeconds(180))){
                System.out.println("[!] Doing client clean up.");
                this.lastCleanup = Instant.now();
                try {
                    this.c.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                this.coroutineCount.set(0);
                this.c = asyncHttpClient(this.clientConfig);
            }
        }
    }

    /*
        Run Continuously
     */
    public void processNextTask(){

        if(!DO_CLEANUP || this.lastCleanup.isBefore(Instant.now().minusSeconds(10))){
            DebugLogger.log("TweetThreadTask: Before attempting to increase request count.");

            if(this.taskService.hasNextTask()){
                Proxy proxy = this.scraper.proxyService.getNewProxy();
                TweetTask task = this.taskService.getNextTask();


                if(proxy != null){

                    // Debugging version only makes debug requests!
                    Request request1 = new RequestBuilder("GET")
                            .setUrl("http://httpforever.com")
                            .setProxyServer(new ProxyServer.Builder(proxy.getIP(), proxy.getPort()).build())
                            .build();

                    c.executeRequest(request1, new handler(c, proxy, task, this));
                } else {
                    System.out.println("No proxies are available!");
                }
            }
        }
    }


    public Scraper getScraper(){
        return this.scraper;
    }

    public LoggingThread getLogger(){
        return this.scraper.logger;
    }

    public int getCoroutineCount() { return this.coroutineCount.get(); }

    public void incrementCoroutineCount() { this.coroutineCount.incrementAndGet(); }
    public void decrementCoroutineCount() { this.coroutineCount.decrementAndGet(); }

    public TaskService getTaskService(){
        return this.taskService;
    }
}