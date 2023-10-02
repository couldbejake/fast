package com.scrapium;

import com.scrapium.proxium.Proxy;
import com.scrapium.tweetium.TweetTask;
import io.netty.handler.codec.http.HttpHeaders;
import org.asynchttpclient.*;

import java.io.IOException;

public class handler implements AsyncHandler<Integer> {
    private final AsyncHttpClient client;
    private final TweetThreadTaskProcessor processor;
    private final Proxy proxy;
    private final TweetTask task;
    private Integer status;

    public handler(AsyncHttpClient client, Proxy proxy, TweetTask task, TweetThreadTaskProcessor tweetThreadTaskProcessor) {
        this.client = client;
        this.proxy = proxy;
        this.task = task;
        this.processor = tweetThreadTaskProcessor;
        this.processor.incrementCoroutineCount();
    }
    @Override
    public AsyncHandler.State onStatusReceived(HttpResponseStatus responseStatus) throws Exception {
        status = responseStatus.getStatusCode();
        if(status >= 200 && status < 300){
            this.processor.getScraper().logger.increaseSuccessRequestCount();
            proxy.onSuccess();
            System.out.print("V");
            processor.getTaskService().successfulTask(task);
        } else {
            this.processor.getScraper().logger.increaseFailedRequestCount();
            proxy.onFailure();
            System.out.print("X");
        }
        //try { c.close(); } catch (IOException e) { throw new RuntimeException(e); }
        return State.CONTINUE;
    }

    @Override
    public State onHeadersReceived(HttpHeaders headers) throws Exception {
        //try { c.close(); } catch (IOException e) { throw new RuntimeException(e); }
        return State.CONTINUE;
    }

    @Override
    public AsyncHandler.State onBodyPartReceived(HttpResponseBodyPart bodyPart) throws Exception {

        //try { c.close(); } catch (IOException e) { throw new RuntimeException(e); }
        return State.CONTINUE;

    }
    @Override
    public Integer onCompleted() throws Exception {
        this.processor.decrementCoroutineCount();
        return 200;
    }

    @Override
    public void onThrowable(Throwable t) {
        proxy.onFailure();
        //System.out.print("E");
        // Handle exceptions here
        this.processor.getScraper().logger.increaseFailedRequestCount();
        this.processor.decrementCoroutineCount();
        processor.getTaskService().failTask(task);
        //System.err.println("An error occurred: " + t.getMessage());
    }


}