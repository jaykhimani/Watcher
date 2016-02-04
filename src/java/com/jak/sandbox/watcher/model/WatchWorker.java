package com.jak.sandbox.watcher.model;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

public class WatchWorker implements Runnable {

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
        for (;;) {
            // wait for key to be signalled
            WatchKey key;
            try {
                key = watcher.take();
            } catch (InterruptedException x) {
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
                System.out.format("%s: %s\n", event.kind().name(), child);
                this.watcherConfig.getEventProcessor().execute(event.kind().name(), child.toString());

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

    private <T> WatchEvent<T> cast(WatchEvent<?> event) {
        return (WatchEvent<T>) event;
    }

}
