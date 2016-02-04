package com.jak.sandbox.watcher.model.processor;

public interface EventProcessor {

    ProcessorType getProcessorType();

    void execute(String name, String resource);
}
