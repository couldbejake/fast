package com.scrapium;

import sun.misc.Signal;
import sun.misc.SignalHandler;

public class CustomSignalHandler {
    public static void handleTSTPSignal(final Runnable onStop) {
        Signal.handle(new Signal("TSTP"), new SignalHandler() {
            @Override
            public void handle(Signal signal) {
                onStop.run();
            }
        });
    }
}