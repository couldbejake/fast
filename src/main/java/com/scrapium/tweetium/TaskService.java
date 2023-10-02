package com.scrapium.tweetium;

import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;

public class TaskService {

    private ConcurrentLinkedDeque<TweetTask> backlogTweetQueue;
    private ConcurrentLinkedQueue<TweetTask> inProcessingTweetQueue;

    public TaskService() {
        backlogTweetQueue = new ConcurrentLinkedDeque<>();
        inProcessingTweetQueue = new ConcurrentLinkedQueue<>();
    }

    public boolean doesQueueHaveFreeSpace(){
        return (backlogTweetQueue.size() < 5000);
    }

    // maybe separate this into two lists.
    public TweetTask getNextTask(){
        if(backlogTweetQueue.size() == 0){
            System.out.println("[X] No tasks in the queue!");
            return null;
        }
        TweetTask task = backlogTweetQueue.poll();
        //task.setState(TweetTask.TweetTaskState.PROCESSING);
        inProcessingTweetQueue.add(task);
        return task;
    }

    public boolean hasNextTask(){
        return (backlogTweetQueue.size() > 0);
    }

    public void successfulTask(TweetTask task){
        inProcessingTweetQueue.remove(task);
        if(task.hasContinuation()/* && task.getState() == TweetTask.TweetTaskState.COMPLETED */){
            // continue with next request
            TweetTask continuationTask = task.getConsecutiveRequest();
            //continuationTask.setState(TweetTask.TweetTaskState.PROCESSING);
            this.backlogTweetQueue.addFirst(continuationTask);
        } else {
            // no continuation
        }
    }

    public void failTask(TweetTask task){
        inProcessingTweetQueue.remove(task);
        this.backlogTweetQueue.addFirst(task);
    }

    // fix branch prediction failures
    public void cleanup(){
        // do cleanup, if items in visible for more than 4 minutes, push to front of tweet queue
    }

    public void addNewTweetTaskEnd(TweetTask tweetTask) {
        this.backlogTweetQueue.addLast(tweetTask);
    }
}
