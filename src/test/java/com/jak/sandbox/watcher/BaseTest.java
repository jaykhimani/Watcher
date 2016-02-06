package com.jak.sandbox.watcher;

import java.lang.reflect.Field;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public abstract class BaseTest {

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
