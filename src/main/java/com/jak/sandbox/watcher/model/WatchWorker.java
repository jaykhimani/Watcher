package com.jak.sandbox.watcher.model;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.*;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

public class WatchWorker extends Thread {

    private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
    private final WatchService watcher;
    private final WatcherConfig watcherConfig;
    private final Map<WatchKey, Path> keys;

    public WatchWorker(WatcherConfig watcherConfig) throws IOException {
        this.watcherConfig = watcherConfig;
        Path path = Paths.get(watcherConfig.getWhatToWatch());
        this.watcher = FileSystems.getDefault().newWatchService();
        keys = new HashMap<>();
        if (watcherConfig.isRecursive()) {
            registerRecursive(path);
        } else {
            register(path);
        }
    }

    @Override
    public void interrupt() {
        super.interrupt();
        System.out.println("bangggggggggggggggg");
    }

    private void registerRecursive(Path path) throws IOException {
        Files.walkFileTree(path, new SimpleFileVisitor<Path>() {

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                register(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    private void register(Path path) throws IOException {
        List<Kind<Path>> events = watcherConfig.getEvents();
        Kind[] kinds = new Kind[events.size()];
        for (int i = 0; i < events.size(); i++) {
            kinds[i] = events.get(i);
        }

        WatchKey key = path.register(watcher, kinds);
        keys.put(key, path);
    }

    @Override
    public void run() {
        for (; ; ) {
            // wait for key to be signalled
            WatchKey key;
            try {
                key = watcher.take();
            } catch (InterruptedException x) {
                watcherConfig.clean();
                return;
            }

            Path dir = keys.get(key);
            if (dir == null) {
                System.err.println("WatchKey not recognized!!");
                continue;
            }

            for (WatchEvent<?> event : key.pollEvents()) {
                Kind kind = event.kind();

                // TBD - provide example of how OVERFLOW event is handled
                if (kind == OVERFLOW) {
                    continue;
                }

                // Context for directory entry event is the file name of entry
                WatchEvent<Path> ev = cast(event);
                Path name = ev.context();
                Path child = dir.resolve(name);

                // print out event
                String eventName = event.kind().name();
                eventName = eventName.replaceFirst("ENTRY_", "");
                String logMsg = SDF.format(new Date()) + "," + eventName + "," + child.toString();
                PrintWriter logWriter = watcherConfig.getLogWriter();
                if (logWriter == null) {
                    System.out.println(logMsg);
                } else {
                    logWriter.println(logMsg);
                    logWriter.flush();
                }
                this.watcherConfig.getEventProcessor().execute(eventName, child.toString());

                // if directory is created, and watching recursively, then
                // register it and its sub-directories
                if (watcherConfig.isRecursive() && (kind == ENTRY_CREATE)) {
                    try {
                        if (Files.isDirectory(child, LinkOption.NOFOLLOW_LINKS)) {
                            registerRecursive(child);
                        }
                    } catch (IOException x) {
                        // ignore to keep sample readbale
                    }
                }
            }

            // reset key and remove from set if directory no longer accessible
            boolean valid = key.reset();
            if (!valid) {
                keys.remove(key);

                // all directories are inaccessible
                if (keys.isEmpty()) {
                    break;
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private <T> WatchEvent<T> cast(WatchEvent<?> event) {
        return (WatchEvent<T>) event;
    }

}
