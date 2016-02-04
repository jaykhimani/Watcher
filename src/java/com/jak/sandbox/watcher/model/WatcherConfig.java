package com.jak.sandbox.watcher.model;

import com.jak.sandbox.watcher.model.processor.EventProcessor;

import java.nio.file.Path;
import java.nio.file.WatchEvent.Kind;
import java.util.ArrayList;
import java.util.List;

import static java.nio.file.StandardWatchEventKinds.*;

public class WatcherConfig {

    private String name;
    private String whatToWatch;
    private List<Kind<Path>> events = new ArrayList<>();
    private boolean recursive;
    private String logfile;
    private EventProcessor eventProcessor;

    private WatcherConfig(String name) {
        this.name = name;
    }

    public static WatcherConfig withName(String watcherName) {
        return new WatcherConfig(watcherName);
    }

    public WatcherConfig watch(String property, String dirOrFile) {
        if (dirOrFile == null || dirOrFile.isEmpty()) {
            throw new RuntimeException(String.format("Invalid Configuration: Define to to monitor/watch for key %s", property));
        }
        whatToWatch = dirOrFile;
        return this;
    }

    public WatcherConfig withEvents(String property, String value) {
        if (value == null || value.isEmpty()) {
            throw new RuntimeException(String.format("Invalid Configuration: Events to watch not specified for key %s", property));
        }
        String[] eventNames = value.split(",");
        for (String event : eventNames) {
            switch (event) {
                case "CREATE":
                    events.add(ENTRY_CREATE);
                    break;
                case "DELETE":
                    events.add(ENTRY_DELETE);
                    break;
                case "MODIFY":
                    events.add(ENTRY_MODIFY);
                    break;
                default:
                    throw new RuntimeException(String.format("Invalid Configuration: Unknown event type %s for key %s", event, property));
            }
        }

        return this;
    }

    public WatcherConfig withRecursive(String property) {
        if (property != null && !property.isEmpty()) {
            recursive = Boolean.parseBoolean(property);
        }
        return this;
    }

    public WatcherConfig withLogFile(String property) {
        if (property != null && !property.isEmpty()) {
            logfile = property;
        }
        return this;
    }

    public void withEventProcessor(EventProcessor eventProc) {
        this.eventProcessor = eventProc;
    }

    public String getName() {
        return name;
    }

    public List<Kind<Path>> getEvents() {
        return events;
    }

    public boolean isRecursive() {
        return recursive;
    }

    public String getLogfile() {
        return logfile;
    }

    public String getWhatToWatch() {
        return whatToWatch;
    }

    public EventProcessor getEventProcessor() {
        return eventProcessor;
    }

    @Override
    public String toString() {
        return "WatcherConfig{" + "name='" + name + '\'' + ", whatToWatch='" + whatToWatch + '\'' + ", events=" + events + ", recursive=" + recursive
                + ", logfile='" + logfile + '\'' + ", eventProcessor=" + eventProcessor + '}';
    }
}
