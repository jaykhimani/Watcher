package com.jak.sandbox.watcher.model;

import com.jak.sandbox.watcher.model.processor.EventProcessor;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
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
    private EventProcessor eventProcessor;
    private PrintWriter logWriter;

    private WatcherConfig(String name) {
        this.name = name;
    }

    public static WatcherConfig withName(String watcherName) {
        return new WatcherConfig(watcherName);
    }

    public WatcherConfig watch(String property, String directory) {
        if (directory == null || directory.isEmpty()) {
            throw new RuntimeException(String.format("Invalid Configuration: Define what to monitor/watch for key %s", property));
        }
        whatToWatch = directory;
        return this;
    }

    public WatcherConfig withEvents(String property, String value) {
        if (value == null || value.isEmpty()) {
            events.add(ENTRY_CREATE);
            events.add(ENTRY_DELETE);
            events.add(ENTRY_MODIFY);
            return this;
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

    public WatcherConfig withLogFile(String watcherName, String property) {
        if (property != null && !property.isEmpty()) {
            String logFile = property + "/" + watcherName + ".log";
            File file = new File(logFile);
            try {
                file.getParentFile().mkdirs();
                file.createNewFile();
                logWriter = new PrintWriter(file);
            } catch (IOException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        }
        return this;
    }

    public WatcherConfig withEventProcessor(EventProcessor eventProc) {
        this.eventProcessor = eventProc;
        return this;
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

    public PrintWriter getLogWriter() {
        return logWriter;
    }

    public String getWhatToWatch() {
        return whatToWatch;
    }

    public EventProcessor getEventProcessor() {
        return eventProcessor;
    }

    @Override
    public String toString() {
        return "WatcherConfig{" +
                "name='" + name + '\'' +
                ", whatToWatch='" + whatToWatch + '\'' +
                ", events=" + events +
                ", recursive=" + recursive +
                ", eventProcessor=" + eventProcessor +
                ", logWriter=" + logWriter +
                '}';
    }

    public void clean() {
        if (logWriter != null) {
            logWriter.close();
        }
    }
}
