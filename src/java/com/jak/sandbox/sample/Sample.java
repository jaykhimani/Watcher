package com.jak.sandbox.sample;

public class Sample {

    public static void main(String[] args) {
        System.out.println("Sample::main called");
    }

    public void someMethod() {
        System.out.println("Sample:someMethod called");
    }

    public void someMethod(String s, String s2) {
        System.out.println("Sample:someMethod(s, s2) called " + s + " - " + s2);
    }

}
