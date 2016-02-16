package com.jak.sandbox.watcher.model.processor;

import com.jak.sandbox.sample.Sample;
import com.jak.sandbox.watcher.BaseTest;
import com.jak.sandbox.watcher.Main;
import com.jak.sandbox.watcher.helper.Utils;
import org.junit.Ignore;
import org.junit.Test;

import java.lang.reflect.Field;

import static com.jak.sandbox.watcher.model.processor.ProcessorType.JAVA;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class JavaEventProcessorTest extends BaseTest {

    private JavaEventProcessor victim;

    @Test
    public void getProcessorType() {
        victim = new JavaEventProcessor("com.jak.sandbox.watcher.model.processor.JavaEventProcessorTest", "getProcessorType");
        assertNotNull(victim);
        assertEquals(JAVA, victim.getProcessorType());
    }

    @Test
    public void create() throws NoSuchFieldException, IllegalAccessException {
        String className = Main.class.getCanonicalName();
        victim = new JavaEventProcessor(className, "configure");
        assertNotNull(victim);
        Field field = victim.getClass().getDeclaredField("declaredMethod");
        assertNotNull(field);
        field.setAccessible(true);
        Object value = field.get(victim);
        assertEquals("private static void com.jak.sandbox.watcher.Main.configure()", value.toString());
    }

    @Test
    public void createWithMain() throws NoSuchFieldException, IllegalAccessException {
        String className = Main.class.getCanonicalName();
        victim = new JavaEventProcessor(className, null);
        assertNotNull(victim);
        Field field = victim.getClass().getDeclaredField("declaredMethod");
        assertNotNull(field);
        field.setAccessible(true);
        Object value = field.get(victim);
        assertEquals("public static void com.jak.sandbox.watcher.Main.main(java.lang.String[])", value.toString());
    }

    @Test
    public void createWithParam() throws NoSuchFieldException, IllegalAccessException {
        String className = Main.class.getCanonicalName();
        victim = new JavaEventProcessor(className, "getWatcherName,EVENT");
        assertNotNull(victim);
        Field field = victim.getClass().getDeclaredField("declaredMethod");
        assertNotNull(field);
        field.setAccessible(true);
        Object value = field.get(victim);
        assertEquals("private static java.lang.String com.jak.sandbox.watcher.Main.getWatcherName(java.lang.String)", value.toString());

        className = Sample.class.getCanonicalName();
        victim = new JavaEventProcessor(className, "someMethod,EVENT,RESOURCE");
        assertNotNull(victim);
        field = victim.getClass().getDeclaredField("declaredMethod");
        assertNotNull(field);
        field.setAccessible(true);
        value = field.get(victim);
        assertEquals("public void com.jak.sandbox.sample.Sample.someMethod(java.lang.String,java.lang.String)", value.toString());
    }

    @Test
    public void createClassNotFund() {
        String className = "baba.ji.ka.thullu";
        expectRuntimeException(String.format("Invalid Configuration: Java class not found '%s'", className));
        new JavaEventProcessor(className, "");
    }

    @Test
    public void createCantInstantiate() {
        String className = EventProcessor.class.getCanonicalName();
        expectRuntimeException(String.format("Invalid Configuration: Cannot instantiate class '%s'", className));
        new JavaEventProcessor(className, "");
    }

    @Test
    public void createCantAccess() {
        String className = Utils.class.getCanonicalName();
        expectRuntimeException(String.format("Invalid Configuration: Cannot access class '%s'", className));
        new JavaEventProcessor(className, "");
    }

    @Test
    public void createNoSuchMethod() {
        String className = getClass().getCanonicalName();
        String methodName = "fooBar";
        expectRuntimeException(String.format("Invalid Configuration: '%s' method not found for event processor class '%s'.", methodName, className));
        new JavaEventProcessor(className, methodName);
    }

    @Test
    @Ignore("Implement me. figure out way to test this")
    public void execute() {
    }
}