package com.jak.sandbox.watcher.model.processor;

import com.jak.sandbox.sample.Sample;
import com.jak.sandbox.watcher.BaseTest;
import org.junit.Test;
import org.mockito.Mock;

import static com.jak.sandbox.watcher.model.processor.ProcessorType.JAVA;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class JavaEventProcessorTest extends BaseTest {

    @Mock
    private Sample sample;

    private JavaEventProcessor victim;

    @Test
    public void getProcessorType() {
        victim = new JavaEventProcessor("com.jak.sandbox.watcher.model.processor.JavaEventProcessorTest", "getProcessorType");
        assertNotNull(victim);
        assertEquals(JAVA, victim.getProcessorType());
    }

    @Test
    public void create() {
        victim = new JavaEventProcessor("com.jak.sandbox.watcher.model.processor.JavaEventProcessorTest", "getProcessorType");
        assertNotNull(victim);
    }

    @Test
    public void execute() {

    }
}