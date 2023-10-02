package com.scrapium;

import com.scrapium.proxium.ProxyService;
import com.scrapium.threads.LoggingThread;
import com.scrapium.threads.ProducerThread;
import com.scrapium.threads.ProxyThread;
import com.scrapium.threads.TweetThread;
import com.scrapium.tweetium.TaskService;
import com.scrapium.utils.DebugLogger;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.*;

public class Scraper {


    public ProxyService proxyService;
    public long conSocketTimeout;
    private int consumerCount;
    public int maxCoroutineCount;

    private final ExecutorService threadPool;
    private TaskService taskService;

    //public AtomicInteger coroutineCount;
    public LoggingThread logger;
    public ProxyThread proxyThread;

    private ProducerThread producer;
    private ArrayList<ThreadBase> threads;

    // the number of coroutines currently running

    public Scraper(int consumerCount, int maxCoroutineCount, int conSocketTimeout) {

        this.proxyService = new ProxyService();

        this.consumerCount = consumerCount;
        this.maxCoroutineCount = maxCoroutineCount;
        this.conSocketTimeout = conSocketTimeout;

        this.threadPool = Executors.newFixedThreadPool(consumerCount + 3);
        this.taskService = new TaskService();
        this.threads = new ArrayList<ThreadBase>();


        /*
        // Handle the SIGINT signal (CTRL + C)
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down gracefully...");
            this.stop();
        }));

        String osName = System.getProperty("os.name");
        if (!osName.toLowerCase().contains("windows")) {
            // the environment is not Windows

            // Handle the SIGTSTP signal (CTRL + Z)
            CustomSignalHandler.handleTSTPSignal(() -> {
                this.stop();
                System.out.println("SIGTSTP signal received!");
                System.exit(0);
            });
        } */

    }

    public void scrape() {

        this.logger = new LoggingThread(this, taskService);
        //threads.add(this.logger);
        threadPool.submit(this.logger);

        this.proxyThread = new ProxyThread(this, this.proxyService);
        //threads.add(this.proxyThread);
        threadPool.submit(this.proxyThread);

        this.producer = new ProducerThread(this, taskService);
        //threads.add(this.producer);
        threadPool.submit(this.producer);

        for (int i = 0; i < consumerCount; i++) {
            DebugLogger.log("Scraper: Created consumer thread.");
            TweetThread tweetThread = new TweetThread(i + 1, this, taskService);
           // threads.add(tweetThread);
            threadPool.submit(tweetThread);
        }
    }

    public void stop() {
        for (Iterator<ThreadBase> iterator = threads.iterator(); iterator.hasNext(); ) {
            ThreadBase item = iterator.next();
            item.running = false;
        }

        try {
            System.out.println("Attempting to shutdown thread pool...");
            threadPool.shutdown();
            threadPool.awaitTermination(400, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.err.println("Thread pool termination interrupted.");
        } finally {
            if (!threadPool.isTerminated()) {
                System.err.println("Forcing thread pool shutdown...");
                threadPool.shutdownNow();
                try {
                    threadPool.awaitTermination(60, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
            }
            System.out.println("Thread pool shutdown complete.");
        }
    }
}
