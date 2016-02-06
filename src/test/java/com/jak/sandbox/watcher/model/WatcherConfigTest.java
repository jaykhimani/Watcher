package com.jak.sandbox.watcher.model;

import com.jak.sandbox.watcher.BaseTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.WatchEvent.Kind;
import java.util.ArrayList;
import java.util.List;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static org.junit.Assert.*;

public class WatcherConfigTest extends BaseTest {

    public static final String MY_WATCHER = "MyWatcher";
    private WatcherConfig watcherConfig;
    public static final String LOG_DIR = "/tmp";

    @Before
    public void setUp() {
        watcherConfig = WatcherConfig.withName(MY_WATCHER);
        assertNotNull(watcherConfig);
        assertNotNull(watcherConfig.getName());
        assertEquals(MY_WATCHER, watcherConfig.getName());
    }

    @Test
    public void create() throws NoSuchFieldException, IllegalAccessException {
        assertAllNull(watcherConfig, "whatToWatch", "eventProcessor", "logWriter");
        assertFalse(watcherConfig.isRecursive());
        assertTrue(watcherConfig.getEvents().isEmpty());
    }

    @Test
    public void createRecursive() {
        watcherConfig = watcherConfig.withRecursive("true");
        assertTrue(watcherConfig.isRecursive());

        watcherConfig = watcherConfig.withRecursive("ture");
        assertFalse(watcherConfig.isRecursive());

        watcherConfig = watcherConfig.withRecursive("false");
        assertFalse(watcherConfig.isRecursive());
    }

    @Test
    public void createLogWriter() {
        watcherConfig = watcherConfig.withLogFile(MY_WATCHER, LOG_DIR);
        assertNotNull(watcherConfig.getLogWriter());
    }

    @Test
    public void createWithEventsAll() {
        WatcherConfig watcherConfig = this.watcherConfig.withEvents("", null);
        List<Kind<Path>> events = watcherConfig.getEvents();
        assertNotNull(events);
        assertEquals(3, events.size());

        List<Kind<Path>> expected = new ArrayList<>();
        expected.add(ENTRY_CREATE);
        expected.add(ENTRY_DELETE);
        expected.add(ENTRY_MODIFY);

        assertTrue(events.containsAll(expected));
    }

    @Test
    public void createWithEvent() {
        WatcherConfig watcherConfig = this.watcherConfig.withEvents("", "CREATE");
        List<Kind<Path>> events = watcherConfig.getEvents();
        assertNotNull(events);
        assertEquals(1, events.size());

        assertEquals(ENTRY_CREATE, events.get(0));

    }

    @After
    public void cleanUp() {
        watcherConfig.clean();
        new File(LOG_DIR + "/" + MY_WATCHER + ".log").delete();
    }
}