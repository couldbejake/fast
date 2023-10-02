package com.scrapium;

import com.scrapium.tests.Benchmark;

import java.util.ArrayList;
// makes sense to use PostgreSQL for data, and Redis for caching & analytics

/*
    Troubleshooting
    - Requests drop to 0
        - Maybe you're using too many resources - there's a perfect balance.
            - Having too many worker threads leads to blocks and switching between threads - slowing the system down.
            - Using too much memory and the program can't allocate memory
        - There are no proxies available
        - There are no tasks left
        - You have reached the maximum co-currency
            - Something may be causing a hang - ie. in the request handler, ie. not updating the coroutine count properly

 */

public class Main {

    public static void main(String[] args) {
        runService();
    }

    public static void runTest(){
        Benchmark.runTest();
    }

    public static void runService(){

        // Scraper(consumerCount, maxCoroutineCount, conSocketTimeout)
        
        // consumerCount - The number of threads running scraper tasks
        // maxCoroutineCount - The max amount of asynchronous calls that should be made for each thread
        // conSocketTimeout - The amount of time before connectionSocketTimeout will occur.

        // calls

        // scraper.logger.successRequestCount.get() - Will get the amount of total successful requests since .scrape() is called.
        // scraper.logger.failedRequestCount.get() - Will get the amount of total failed requests since .scrape() is called.

        // note: The last parameter of Scrape() is not currently used.

        // 6, 5000 - AWS 800 requests per second

        Scraper scraper = new Scraper(2, 2000, 10);

        scraper.scrape();

    }


}