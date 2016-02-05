package com.jak.sandbox.sample;

public class Sample {

    public static void main(String[] args) {
        System.out.println(String.format("Sample::main called with args %s & %s", args[0], args[1]));
    }

    public void someMethod() {
        System.out.println("Sample:someMethod called");
    }

    public void someMethod(String s, String s2) {
        System.out.println("Sample:someMethod(s, s2) called " + s + " - " + s2);
    }

}
