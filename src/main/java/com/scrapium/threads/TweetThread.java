package com.scrapium.threads;

import com.scrapium.Scraper;
import com.scrapium.ThreadBase;
import com.scrapium.TweetThreadTaskProcessor;
import com.scrapium.tweetium.TaskService;
import com.scrapium.utils.DebugLogger;

import java.util.concurrent.atomic.AtomicInteger;

public class TweetThread  extends ThreadBase implements Runnable {



    private final Scraper scraper;
    private final int threadID;
    private TaskService taskService;
    private AtomicInteger coroutineCount;

    private final TweetThreadTaskProcessor taskProcessor;

    // possibly move maxCoroutineCount to scraper, so it doesn't need to be updated in each class - blocking.

    public TweetThread(int i, Scraper scraper, TaskService taskService) {
        this.threadID = i;
        this.scraper = scraper;
        this.taskService = taskService;
        this.coroutineCount = new AtomicInteger(0);
        this.taskProcessor = new TweetThreadTaskProcessor(this.threadID, this.running, this.scraper, taskService, this.coroutineCount);
    }

    @Override
    public void run() {
        while (this.running) {
            // move tick to different thread for code speed-up
            this.taskProcessor.doClientCleanupTick();

            if (this.coroutineCount.get() < scraper.maxCoroutineCount) {
                DebugLogger.log("TweetThread: Ran cycle");
                DebugLogger.log("TweetThread: Task Taken");
                this.taskProcessor.processNextTask();
                DebugLogger.log("Decrementing counter");

            } else {
                if(this.coroutineCount.get() >= scraper.maxCoroutineCount){
                    //System.out.println("Skipping thread execution!");
                    //System.out.println("  Reason: MAX CO-ROUTINES (" + this.coroutineCount.get() + "/" + scraper.maxCoroutineCount + ")");
                }

                try {
                    Thread.sleep(150); // Sleep when the maximum number of tasks are being executed
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }

            /*
            try {
                //System.out.println("tweetThread coroutine count = " + this.coroutineCount.get());
                //System.out.println("tweetThread taskQueue = " + this.taskQueue.size());

                if (this.taskQueue.size() > 0 && this.coroutineCount.get() < scraper.maxCoroutineCount) {
                    DebugLogger.log("TweetThread: Ran cycle");
                    DebugLogger.log("TweetThread: Task Taken");
                    this.taskProcessor.processNextTask();
                    DebugLogger.log("Decrementing counter");

                } else {

                    if(this.taskQueue.size() == 0){
                        //System.out.println("Skipping thread execution!");
                        //System.out.println("  Reason: QUEUE EMPTY");
                    }
                    if(this.coroutineCount.get() >= scraper.maxCoroutineCount){
                        //System.out.println("Skipping thread execution!");
                        //System.out.println("  Reason: MAX CO-ROUTINES (" + this.coroutineCount.get() + "/" + scraper.maxCoroutineCount + ")");
                    }

                    Thread.sleep(150); // Sleep when the maximum number of tasks are being executed
                }
            } catch (Exception e) {
                e.printStackTrace();
                DebugLogger.log("Interrupted Exception!");
            } */
        }

        // TODO: readd close request client;
        //System.out.println("closeRequestClient called");
        //this.taskProcessor.closeRequestClient();
    }
}
