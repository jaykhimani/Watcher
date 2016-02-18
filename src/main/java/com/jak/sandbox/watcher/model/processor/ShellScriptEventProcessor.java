package com.jak.sandbox.watcher.model.processor;

import java.io.IOException;

/**
 * Event processor responsible to invoke Shell script as event post processor
 *
 * @author Jay
 * @since 1.0
 */
public class ShellScriptEventProcessor implements EventProcessor {

    private String script;

    public ShellScriptEventProcessor(String script) {
        this.script = script;
    }

    @Override
    public ProcessorType getProcessorType() {
        return ProcessorType.SHELL;
    }

    @Override
    public void execute(String name, String resource) {
        ProcessBuilder processBuilder = new ProcessBuilder(script, name, resource);
        try {
            System.out.println("Executing shell script: " + script);
            processBuilder.start();
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }

    }
}
