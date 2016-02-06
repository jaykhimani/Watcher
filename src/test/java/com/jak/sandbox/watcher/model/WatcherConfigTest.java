package com.jak.sandbox.watcher.model;

import com.jak.sandbox.watcher.BaseTest;
import com.jak.sandbox.watcher.model.processor.EventProcessor;
import com.jak.sandbox.watcher.model.processor.ProcessorType;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

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
    public static final String LOG_DIR = "target";

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

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

        this.watcherConfig.getEvents().clear();
        watcherConfig = this.watcherConfig.withEvents("", "CREATE,DELETE");
        events = watcherConfig.getEvents();
        assertNotNull(events);
        assertEquals(2, events.size());

        assertEquals(ENTRY_CREATE, events.get(0));
        assertEquals(ENTRY_DELETE, events.get(1));

        this.watcherConfig.getEvents().clear();
        watcherConfig = this.watcherConfig.withEvents("", "CREATE,DELETE,MODIFY");
        events = watcherConfig.getEvents();
        assertNotNull(events);
        assertEquals(3, events.size());

        assertEquals(ENTRY_CREATE, events.get(0));
        assertEquals(ENTRY_DELETE, events.get(1));
        assertEquals(ENTRY_MODIFY, events.get(2));
    }

    @Test
    public void createEventFail() {
        expectedException.expect(RuntimeException.class);
        expectedException.expectMessage("Invalid Configuration: Unknown event type bla bla for key MyWatcher");
        this.watcherConfig.withEvents(MY_WATCHER, "bla bla");
    }

    @Test
    public void createWithProcessor() {
        EventProcessor eventProc = new EventProcessor() {
            @Override
            public ProcessorType getProcessorType() {
                return null;
            }

            @Override
            public void execute(String name, String resource) {

            }
        };
        watcherConfig = watcherConfig.withEventProcessor(eventProc);
        assertNotNull(watcherConfig.getEventProcessor());
        assertEquals(eventProc, watcherConfig.getEventProcessor());
    }

    @Test
    public void watch() {
        watcherConfig = watcherConfig.watch(MY_WATCHER, LOG_DIR);
        assertNotNull(watcherConfig.getWhatToWatch());
        assertEquals(LOG_DIR, watcherConfig.getWhatToWatch());
    }

    @Test
    public void watchFailEmpty() {
        expectedException.expect(RuntimeException.class);
        expectedException.expectMessage("Invalid Configuration: Define what to monitor/watch for key " + MY_WATCHER);
        watcherConfig.watch(MY_WATCHER, "");
    }

    @Test
    public void watchFailNull() {
        expectedException.expect(RuntimeException.class);
        expectedException.expectMessage("Invalid Configuration: Define what to monitor/watch for key " + MY_WATCHER);
        watcherConfig.watch(MY_WATCHER, null);
    }

    @After
    public void tearDown() {
        watcherConfig.clean();
        new File(LOG_DIR + "/" + MY_WATCHER + ".log").delete();
    }
}