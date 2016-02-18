package com.jak.sandbox.watcher.model.processor;

import java.io.IOException;

/**
 * Event processor responsible to invoke Python script as event post processor
 *
 * @author Jay
 * @since 1.0
 */
public class PythonEventProcessor implements EventProcessor {

    private final String script;

    public PythonEventProcessor(String script) {
        this.script = script;
    }

    @Override
    public ProcessorType getProcessorType() {
        return ProcessorType.PYTHON;
    }

    @Override
    public void execute(String name, String resource) {
        ProcessBuilder processBuilder = new ProcessBuilder("python", script, name, resource);
        try {
            processBuilder.start();
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
