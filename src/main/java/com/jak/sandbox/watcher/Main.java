package com.jak.sandbox.watcher;

import com.jak.sandbox.watcher.helper.ConfigurationHolder;
import com.jak.sandbox.watcher.helper.ConfigurationParser;
import com.jak.sandbox.watcher.model.WatchWorker;
import com.jak.sandbox.watcher.model.WatcherConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Main class to boostrap and trigger Watcher to monitor directories
 * 
 * @author Jay
 * @since 1.0
 */
public class Main {

    public static void main(String[] args) {
        validateJavaVersion();
        ConfigurationHolder configHolder = ConfigurationParser.parseConfiguration();
        Long watchTime = configHolder.getTimeDuration();
        TimeUnit watchUnit = configHolder.getTimeUnit();
        Map<String, WatcherConfig> watcherConfigs = configHolder.getWatcherConfigs();

        if (watcherConfigs.isEmpty()) {
            return;
        }

        ExecutorService executor = Executors.newFixedThreadPool(watcherConfigs.size());
        List<Future<?>> futures = new ArrayList<>();
        System.out.println("====================================================================");
        System.out.println(String.format("| Starting Watcher for %d %s with following watcher(s)...", watchTime, watchUnit));
        try {
            for (WatcherConfig watcherConfig : watcherConfigs.values()) {
                print(watcherConfig);
                futures.add(executor.submit(new WatchWorker(watcherConfig)));
            }
            System.out.println("====================================================================");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                executor.awaitTermination(watchTime, watchUnit);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            executor.shutdown();
            for (Future<?> future : futures) {
                future.cancel(true);
            }
            System.out.println("Watcher stopped");
        }
    }

    private static void print(WatcherConfig config) {
        System.out.println("| -----------------------------------------------------------------");
        System.out.println(String.format("| Name   : %s", config.getName()));
        System.out.println(String.format("| Monitor: %s", config.getWhatToWatch()));
        System.out.println(String.format("| Events : %s", config.getEvents()));
    }

    private static void validateJavaVersion() {
        String version = System.getProperty("java.version");
        float ver = Float.parseFloat(version.substring(0, 3));
        if (ver < 1.7) {
            throw new RuntimeException("Please use Java 7 or higher to use Watcher.");
        }
    }
}
