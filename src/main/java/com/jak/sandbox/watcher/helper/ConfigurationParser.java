package com.jak.sandbox.watcher.helper;

import com.jak.sandbox.watcher.model.WatcherConfig;
import com.jak.sandbox.watcher.model.processor.JavaEventProcessor;
import com.jak.sandbox.watcher.model.processor.PythonEventProcessor;
import com.jak.sandbox.watcher.model.processor.ShellScriptEventProcessor;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import static java.util.concurrent.TimeUnit.DAYS;
import static java.util.concurrent.TimeUnit.HOURS;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Parses configuraiton either from properties file or from command line or from both
 * 
 * @author Jay
 * @since 1.1
 */
public final class ConfigurationParser {
    private static final String DOT = Pattern.quote(".");
    private static final String WATCHER_CONFIG = "watcher.config";
    public static final String WATCHER_WATCH_TIME_DURATION = "watcher.watch.time.duration";
    public static final String WATCHER_WATCH_TIME_UNIT = "watcher.watch.time.unit";

    public static ConfigurationHolder parseConfiguration() {
        Properties props = new Properties();
        ConfigurationHolder holder = configWatcherFromPropertiesFile(props);

        props.clear();
        loadSystemProperties(props);

        if (holder == null) {
            holder = new ConfigurationHolder();
        }
        // Look for command line configuration
        String property = props.getProperty(WATCHER_WATCH_TIME_DURATION);
        if (Utils.isNotEmpty(property)) {
            holder.setTimeDuration(Long.valueOf(property));
        }
        property = props.getProperty(WATCHER_WATCH_TIME_UNIT);
        if (Utils.isNotEmpty(property)) {
            holder.setTimeUnit(getTimeUnit(property));
        }
        props.remove(WATCHER_WATCH_TIME_DURATION);
        props.remove(WATCHER_WATCH_TIME_UNIT);
        props.remove(WATCHER_CONFIG);

        configWatchers(props, holder.getWatcherConfigs());

        return holder;
    }

    private static void loadSystemProperties(Properties props) {
        Properties sysProps = System.getProperties();
        for (Object sysProp : sysProps.keySet()) {
            if (sysProp.toString().startsWith("watcher.")) {
                props.put(sysProp, sysProps.getProperty(sysProp.toString()));
            }
        }
    }

    protected static ConfigurationHolder configWatcherFromPropertiesFile(Properties props) {
        String configFile = System.getProperty(WATCHER_CONFIG);
        if (Utils.isEmpty(configFile)) {
            return null;
        }
        try {
            props.load(new FileInputStream(configFile));
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        Long timeDuration = Long.valueOf(props.getProperty(WATCHER_WATCH_TIME_DURATION, "1"));
        TimeUnit timeUnit;
        String unit = props.getProperty(WATCHER_WATCH_TIME_UNIT, "M");
        timeUnit = getTimeUnit(unit);
        props.remove(WATCHER_WATCH_TIME_DURATION);
        props.remove(WATCHER_WATCH_TIME_UNIT);
        ConfigurationHolder holder = new ConfigurationHolder();
        holder.setTimeUnit(timeUnit);
        holder.setTimeDuration(timeDuration);
        Map<String, WatcherConfig> watcherConfigs = holder.getWatcherConfigs();
        configWatchers(props, watcherConfigs);
        holder.setWatcherConfigs(watcherConfigs);
        return holder;
    }

    private static void configWatchers(Properties props, Map<String, WatcherConfig> watcherConfigs) {
        Enumeration propNames = props.propertyNames();
        while (propNames.hasMoreElements()) {
            String propName = (String) propNames.nextElement();
            String watcherName = getWatcherName(propName);
            WatcherConfig watcherConfig = watcherConfigs.get(watcherName);
            if (watcherConfig == null) {
                configWatcher(watcherName, props, watcherConfigs);
            }
        }
    }

    private static TimeUnit getTimeUnit(String unit) {
        TimeUnit timeUnit;
        switch (unit) {
            case "M":
                timeUnit = MINUTES;
                break;
            case "D":
                timeUnit = DAYS;
                break;
            case "H":
                timeUnit = HOURS;
                break;
            case "S":
                timeUnit = SECONDS;
                break;
            default:
                throw new RuntimeException(String.format("Invalid Configuration: Unknown watch time unit %s", unit));
        }
        return timeUnit;
    }

    private static void configWatcher(String watcherName, Properties props, Map<String, WatcherConfig> watcherConfigs) {
        String watcherNameKey = "watcher." + watcherName;
        WatcherConfig watcherConfig = WatcherConfig.withName(watcherName)
                .watch(watcherNameKey + ".watch", props.getProperty(watcherNameKey + ".watch"))
                .withEvents(watcherName + ".events", props.getProperty(watcherNameKey + ".events"))
                .withRecursive(props.getProperty(watcherNameKey + ".recursive"))
                .withLogFile(watcherName, props.getProperty(watcherNameKey + ".logfile"));
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

    private ConfigurationParser() {
        // Utility class, no nned to instantiate.
    }
}
