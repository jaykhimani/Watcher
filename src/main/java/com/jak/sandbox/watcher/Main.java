package com.jak.sandbox.watcher;

import com.jak.sandbox.watcher.helper.Utils;
import com.jak.sandbox.watcher.model.WatchWorker;
import com.jak.sandbox.watcher.model.WatcherConfig;
import com.jak.sandbox.watcher.model.processor.JavaEventProcessor;
import com.jak.sandbox.watcher.model.processor.PythonEventProcessor;
import com.jak.sandbox.watcher.model.processor.ShellScriptEventProcessor;

import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public class Main {

    private static final String DOT = Pattern.quote(".");
    private static final Map<String, WatcherConfig> watcherConfigs = new HashMap<>();
    private static Long WATCH_TIME;
    private static TimeUnit WATCH_TIME_UNIT;

    public static void main(String[] args) {
        configure();

        if (watcherConfigs.isEmpty()) {
            return;
        }

        ExecutorService executor = Executors.newFixedThreadPool(watcherConfigs.size());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
        List<Future<?>> futures = new ArrayList<>();
        try {
            for (WatcherConfig watcherConfig : watcherConfigs.values()) {
                System.out.println(watcherConfig);
                futures.add(executor.submit(new WatchWorker(watcherConfig)));
            }
            System.out.println(sdf.format(new Date()));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                executor.awaitTermination(WATCH_TIME, WATCH_TIME_UNIT);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("Shutting down executor");
            executor.shutdown();
            for (Future<?> future : futures) {
                future.cancel(true);
            }
        }
        System.out.println(sdf.format(new Date()));
        System.out.println("Done shutting down executor");
    }

    private static void configure() {
        String configFile = System.getProperty("watcher.config");
        if (Utils.isEmpty(configFile)) {
            throw new RuntimeException("Missing property watcher.config. Please provide the watcher config file using as -Dwatcher.config system property");
        }
        Properties props = new Properties();
        try {
            props.load(new FileInputStream(configFile));
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        String timeDuration = "watcher.watch.time.duration";
        String timeUnit = "watcher.watch.time.unit";
        WATCH_TIME = Long.valueOf(props.getProperty(timeDuration, "1"));
        String unit = props.getProperty(timeUnit, "M");
        switch (unit) {
            case "M":
                WATCH_TIME_UNIT = TimeUnit.MINUTES;
                break;
            case "D":
                WATCH_TIME_UNIT = TimeUnit.DAYS;
                break;
            case "H":
                WATCH_TIME_UNIT = TimeUnit.HOURS;
                break;
            case "S":
                WATCH_TIME_UNIT = TimeUnit.SECONDS;
                break;
            default:
                throw new RuntimeException(String.format("Invalid Configuration: Unknown watch time unit %s", unit));
        }
        props.remove(timeDuration);
        props.remove(timeUnit);
        Enumeration propNames = props.propertyNames();
        while (propNames.hasMoreElements()) {
            String propName = (String) propNames.nextElement();
            String watcherName = getWatcherName(propName);
            WatcherConfig watcherConfig = watcherConfigs.get(watcherName);
            if (watcherConfig == null) {
                configWatcher(watcherName, props);
            }
        }
    }

    private static void configWatcher(String watcherName, Properties props) {
        String watcherNameKey = "watcher." + watcherName;
        WatcherConfig watcherConfig = WatcherConfig.withName(watcherName)
                .watch(watcherNameKey + ".watch", props.getProperty(watcherNameKey + ".watch"))
                .withEvents(watcherName + ".events", props.getProperty(watcherNameKey + ".events"))
                .withRecursive(props.getProperty(watcherNameKey + ".recursive")).withLogFile(watcherName, props.getProperty(watcherNameKey + ".logfile"));
        String type = props.getProperty(watcherNameKey + ".eventprocessor.type");
        if (type != null) {
            switch (type) {
                case "java":
                    String className = props.getProperty(watcherNameKey + ".eventprocessor.class");
                    if (className == null || className.isEmpty()) {
                        throw new RuntimeException(String.format(
                                "Invalid Configuration: EventProcessor: Fully qualified java class name is missing for watcher %s", watcherName));
                    }
                    watcherConfig.withEventProcessor(new JavaEventProcessor(className, props.getProperty(watcherNameKey + ".eventprocessor.method")));
                    break;
                case "python":
                    String script = props.getProperty(watcherNameKey + ".eventprocessor.script");
                    if (script == null || script.isEmpty()) {
                        throw new RuntimeException(String.format("Invalid Configuration: EventProcessor: Python script is missing for watcher %s",
                                watcherName));
                    }
                    watcherConfig.withEventProcessor(new PythonEventProcessor(script));
                    break;
                case "shell":
                    String shellScript = props.getProperty(watcherNameKey + ".eventprocessor.script");
                    if (shellScript == null || shellScript.isEmpty()) {
                        throw new RuntimeException(String.format("Invalid Configuration: EventProcessor: Shell script is missing for watcher %s",
                                watcherName));
                    }
                    watcherConfig.withEventProcessor(new ShellScriptEventProcessor(shellScript));
                    break;
                default:
                    throw new RuntimeException(String.format("Invalid Configuration: Unknown event processor type '%s' found for watcher %s", type,
                            watcherName));
            }
        }
        watcherConfigs.put(watcherName, watcherConfig);
    }

    private static String getWatcherName(String propName) {
        return propName.split(DOT)[1];
    }
}
