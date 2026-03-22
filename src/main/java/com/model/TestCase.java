package com.model;

/**
 * One test case for a coding problem (input and expected output).
 * Used by {@link Contributor#addTestCases(Question, TestCase[])}.
 */
public class TestCase {

    private String input;
    private String expectedOutput;

    public TestCase(String input, String expectedOutput) {
        this.input = input;
        this.expectedOutput = expectedOutput;
    }

    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }

    public String getExpectedOutput() {
        return expectedOutput;
    }

    public void setExpectedOutput(String expectedOutput) {
        this.expectedOutput = expectedOutput;
    }
}
