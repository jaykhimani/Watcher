package com.jak.sandbox.watcher;

import org.junit.Rule;
import org.junit.rules.ExpectedException;

import java.lang.reflect.Field;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public abstract class BaseTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    protected void expectRuntimeException(String substring) {
        expectedException.expect(RuntimeException.class);
        expectedException.expectMessage(substring);
    }

    protected void assertAllNull(Object instance, String... fieldNames) throws NoSuchFieldException, IllegalAccessException {
        assertNotNull(instance);
        Class<?> aClass = instance.getClass();
        for (String fieldName : fieldNames) {
            Field field = aClass.getDeclaredField(fieldName);
            field.setAccessible(true);
            assertNull(String.format("%s is not null", fieldName), field.get(instance));
        }

    }

    protected void assertAllNotNull(Object instance, String... fieldNames) throws NoSuchFieldException, IllegalAccessException {
        assertNotNull(instance);
        Class<?> aClass = instance.getClass();
        for (String fieldName : fieldNames) {
            Field field = aClass.getDeclaredField(fieldName);
            field.setAccessible(true);
            assertNotNull(String.format("%s is null", fieldName), field.get(instance));
        }

    }
}
