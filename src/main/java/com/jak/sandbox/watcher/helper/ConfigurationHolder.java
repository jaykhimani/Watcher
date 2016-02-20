package com.jak.sandbox.watcher.helper;

import com.jak.sandbox.watcher.model.WatcherConfig;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author Jay
 * @since 1.1
 */
public class ConfigurationHolder {

    private Map<String, WatcherConfig> watcherConfigs = new HashMap<>();
    private TimeUnit timeUnit;
    private Long timeDuration;

    public ConfigurationHolder(Long timeDuration, TimeUnit timeUnit) {
        this.timeDuration = timeDuration;
        this.timeUnit = timeUnit;
    }

    public Map<String, WatcherConfig> getWatcherConfigs() {
        return watcherConfigs;
    }

    public void setWatcherConfigs(Map<String, WatcherConfig> watcherConfigs) {
        this.watcherConfigs = watcherConfigs;
    }

    public TimeUnit getTimeUnit() {
        return timeUnit;
    }

    public void setTimeUnit(TimeUnit timeUnit) {
        this.timeUnit = timeUnit;
    }

    public Long getTimeDuration() {
        return timeDuration;
    }

    public void setTimeDuration(Long timeDuration) {
        this.timeDuration = timeDuration;
    }
}
