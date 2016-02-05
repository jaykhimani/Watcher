package com.jak.sandbox.watcher.model.processor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class JavaEventProcessor implements EventProcessor {

    private final Object instance;
    private final Method declaredMethod;
    private boolean defaultMethod;
    private String className;
    private String methodName;
    private String param1;
    private String param2;

    public JavaEventProcessor(String className, String methodName) {
        this.className = className;

        if (methodName == null || methodName.isEmpty()) {
            this.methodName = "main";
            this.defaultMethod = true;
        }
        String[] split = this.methodName.split(",");
        this.methodName = split[0];
        if (split.length > 1) {
            this.param1 = split[1];
        }
        if (split.length > 2) {
            this.param2 = split[2];
        }

        Class<?> aClass = null;
        try {
            aClass = Class.forName(className);
            instance = aClass.newInstance();
            if (this.param2 != null) {
                declaredMethod = aClass.getDeclaredMethod(this.methodName, String.class, String.class);
            } else if (this.param1 != null) {
                declaredMethod = aClass.getDeclaredMethod(this.methodName, String.class);
            } else if (this.defaultMethod) {
                declaredMethod = aClass.getDeclaredMethod(this.methodName, String[].class);
            } else {
                declaredMethod = aClass.getDeclaredMethod(this.methodName);
            }
            declaredMethod.setAccessible(true);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e.getMessage(), e);
        } catch (InstantiationException e) {
            throw new RuntimeException(e.getMessage(), e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e.getMessage(), e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(String.format("Invalid Configuration: %s method not found for event processor class %s.", this.methodName,
                    this.className), e);
        }
    }

    @Override
    public ProcessorType getProcessorType() {
        return ProcessorType.JAVA;
    }

    @Override
    public void execute(String name, String path) {
        try {
            if (this.param2 != null) {
                if (param2.equals("RESOURCE")) {
                    declaredMethod.invoke(instance, name, path);
                } else {
                    declaredMethod.invoke(instance, path, name);
                }
            } else if (this.param1 != null) {
                if (param1.equals("RESOURCE")) {
                    declaredMethod.invoke(instance, path);
                } else {
                    declaredMethod.invoke(instance, name);
                }
            } else if (this.defaultMethod) {
                // default main method is static, hence null
                declaredMethod.invoke(null, (Object) new String[] { name, path });
            } else {
                declaredMethod.invoke(instance);
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e.getMessage(), e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public String getClassName() {
        return className;
    }

    public String getMethodName() {
        return methodName;
    }

    @Override
    public String toString() {
        return "JavaEventProcessor{" + "className='" + className + '\'' + ", methodName='" + methodName + '\'' + '}';
    }
}
