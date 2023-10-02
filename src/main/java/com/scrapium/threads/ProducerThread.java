package com.scrapium.threads;

import com.scrapium.Scraper;
import com.scrapium.ThreadBase;
import com.scrapium.tweetium.TaskService;
import com.scrapium.tweetium.TweetTask;

public class ProducerThread extends ThreadBase implements Runnable {


    private final Scraper scraper;
    private TaskService taskService;

    private int debug_epoch = 1575072000;
    private String debug_search = "$BTC";
    public ProducerThread(Scraper scraper, TaskService taskService) {
        this.scraper = scraper;
        this.taskService = taskService;
    }
    @Override
    public void run() {
        while (this.running) {

            if(this.taskService.doesQueueHaveFreeSpace()){
                TweetTask newTask = new TweetTask(debug_search, debug_epoch, debug_epoch + 30);
                debug_epoch += 30;
                this.taskService.addNewTweetTaskEnd(newTask);
            }
        }
    }
}