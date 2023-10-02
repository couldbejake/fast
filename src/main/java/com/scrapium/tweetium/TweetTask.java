package com.scrapium.tweetium;

public class TweetTask {

    private final String searchTerm;
    private final int fromEpoch;
    private final int toEpoch;
    private final String cursor;

    public TweetTask(String searchTerm, int fromEpoch, int toEpoch) {
        this.searchTerm = searchTerm;
        this.fromEpoch = fromEpoch;
        this.toEpoch = toEpoch;
        this.cursor = "";
    }

    public TweetTask(String searchTerm, int fromEpoch, int toEpoch, String cursor) {
        this.searchTerm = searchTerm;
        this.fromEpoch = fromEpoch;
        this.toEpoch = toEpoch;
        this.cursor = cursor;
    }

    public TweetTask getConsecutiveRequest() {
        return null;
    }


    public boolean hasContinuation() {
        return false;
    }

    @Override
    public String toString() {
        return "TweetTask{" +
                "searchTerm='" + searchTerm + '\'' +
                ", fromEpoch=" + fromEpoch +
                ", toEpoch=" + toEpoch +
                ", cursor='" + cursor + '\'' +
                '}';
    }
}
