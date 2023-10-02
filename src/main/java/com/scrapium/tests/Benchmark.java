package com.scrapium.tests;

import com.scrapium.Scraper;
import com.scrapium.utils.TimeUtils;

import java.util.HashMap;
import java.util.Map;

public class Benchmark {
    public static void runTest() {
        Map<String, Integer> configResults = new HashMap<>();
        String bestConfigKey = "";
        int highestSuccessfulRequests = 0;

        double timePerTest = 5 * 60 * 1000; // 30 seconds

        int totalTestCount = (((6-1)/2) * ((15000-100)/250) * ((28 - 4)/10));
        int totalTestTime = (int) (totalTestCount * timePerTest);

        int testIter = 0;

        System.out.println("\n== Test started ==\n");
        System.out.println("- Total Tests = " + (totalTestCount));
        System.out.println("- Test will be completed " + TimeUtils.timeToString((totalTestTime/1000)));

        for (int maxCoroutineCount = 100; maxCoroutineCount <= 15000; maxCoroutineCount += 250) { // 100 -> 2000
            for (int consumerCount = 1; consumerCount <= 6; consumerCount += 2) { // 1 -> 8
                for (int conSocketTimeout = 6; conSocketTimeout <= 28; conSocketTimeout += 10) { // 4 -> 28

                    Scraper scraper = new Scraper(consumerCount, maxCoroutineCount, conSocketTimeout);
                    scraper.scrape();

                    String configKey = String.format("c_%d_m_%d_t_%d", consumerCount, maxCoroutineCount, conSocketTimeout);

                    System.out.println("\n[" + testIter + "/" + totalTestCount + "] Starting test: "+ configKey + "\n");

                    int timeRemaining = (int) (totalTestTime - testIter * timePerTest);
                    System.out.println("( Test will be completed " + TimeUtils.timeToString(timeRemaining/1000) + " )\n");

                    try {
                        Thread.sleep((long) timePerTest);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        throw new RuntimeException(e);
                    }

                    int successfulRequests = scraper.logger.successRequestCount.get();
                    int failedRequests = scraper.logger.failedRequestCount.get();


                    configResults.put(configKey, successfulRequests);


                    scraper.stop();

                    System.out.printf("\n"+ "Test (" + testIter + "/" + totalTestCount + ") Finished Configuration: %s | Successful Requests: %d | Failed Requests: %d%n\n",
                            configKey, successfulRequests, failedRequests);


                    testIter++;

                    if (successfulRequests > highestSuccessfulRequests) {
                        highestSuccessfulRequests = successfulRequests;
                        bestConfigKey = configKey;
                    }

                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        throw new RuntimeException(e);
                    }
                }
            }
        }

        System.out.println("\n== All Configuration Results ==");
        System.out.println("\n== C=threads m=coroutines t=timeout");

        for (Map.Entry<String, Integer> entry : configResults.entrySet()) {
            System.out.printf("Configuration: %s | Successful Requests: %d%n", entry.getKey(), entry.getValue());
        }

        System.out.printf("\nBest Configuration: %s | Highest Successful Requests: %d%n", bestConfigKey, highestSuccessfulRequests);
    }
}
