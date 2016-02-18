package com.jak.sandbox.watcher.model.processor;

/**
 * Interface defining event post processor. Implementation can be of different types like java, python etc
 *
 * @author Jay
 * @since 1.0
 */
public interface EventProcessor {

    ProcessorType getProcessorType();

    void execute(String name, String resource);
}
