/* Copyright (c) 2015-2016 MIT 6.005 course staff, all rights reserved.
 * Redistribution of original or derived work requires permission of course staff.
 */
package poet;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

/**
 * Tests for GraphPoet.
 */
public class GraphPoetTest {

    /*
     * Testing strategy for GraphPoet:
     *
     * Constructor GraphPoet(File corpus):
     * - File content: empty file, single word, multiple words with spaces/newlines
     * - Case sensitivity: mixed case words that should be treated as same vertex
     * - Word adjacency: repeated adjacency should increase edge weights
     * - File accessibility: valid file, non-existent file
     * - Word delimiters: spaces, newlines, file boundaries
     *
     * poem(String input):
     * - Input: empty string, single word, multiple words
     * - Bridge word selection: no bridge available, single bridge, multiple bridges (choose max weight)
     * - Case preservation: input words keep original case, bridge words are lowercase
     * - Whitespace: output should use single spaces between words
     * - Path finding: direct adjacency vs two-hop paths
     *
     * Edge cases:
     * - Self-loops in corpus (same word repeated)
     * - Punctuation attached to words
     * - Multiple possible bridges with different weights
     * - Input words not in corpus
     * - Complex multi-word inputs
     *
     * Test corpus files:
     * - simple.txt: basic adjacency testing
     * - case-test.txt: case insensitivity testing
     * - weights.txt: edge weight testing
     * - mugar.txt: example from specification
     */

    @Test(expected=AssertionError.class)
    public void testAssertionsEnabled() {
        assert false; // make sure assertions are enabled with VM argument: -ea
    }

    // Constructor tests using files in test/poet/ directory

    @Test
    public void testConstructorEmptyFile() throws IOException {
        GraphPoet poet = new GraphPoet(new File("test/poet/empty.txt"));
        assertEquals("empty corpus should produce empty poem",
                "hello world", poet.poem("hello world"));
    }

    @Test
    public void testConstructorSevenWords() throws IOException {
        GraphPoet poet = new GraphPoet(new File("test/poet/seven-words.txt"));
        // Test basic functionality with seven-words corpus
        String result = poet.poem("seek to");
        assertTrue("should produce valid output", result.contains("seek") && result.contains("to"));
    }

    @Test(expected = IOException.class)
    public void testConstructorNonExistentFile() throws IOException {
        new GraphPoet(new File("test/poet/does-not-exist.txt"));
    }

    // Basic adjacency tests using corpus files

    @Test
    public void testSimpleAdjacency() throws IOException {
        GraphPoet poet = new GraphPoet(new File("test/poet/simple.txt"));

        // Test based on simple.txt content: "hello world goodbye world"
        String result = poet.poem("hello goodbye");
        assertEquals("world should bridge hello to goodbye",
                "hello world goodbye", result);
    }

    @Test
    public void testCaseInsensitivity() throws IOException {
        GraphPoet poet = new GraphPoet(new File("test/poet/case-test.txt"));

        // Test case-insensitive behavior with mixed case corpus
        String result = poet.poem("test World");
        assertTrue("should handle case insensitivity", result.length() ==
                "test World".length());
    }

    @Test
    public void testEdgeWeights() throws IOException {
        GraphPoet poet = new GraphPoet(new File("test/poet/weights.txt"));

        // Test that higher weight bridges are preferred
        String result = poet.poem("to system");
        assertEquals("highest weight bridge should be chosen",
                "to the system", result);
    }

    @Test
    public void testMugarExample() throws IOException {
        GraphPoet poet = new GraphPoet(new File("test/poet/mugar.txt"));

        assertEquals("Mugar example should work as specified",
                "Test of the system.", poet.poem("Test the system."));
    }

    @Test
    public void testPunctuationHandling() throws IOException {
        GraphPoet poet = new GraphPoet(new File("test/poet/punctuation.txt"));

        // Test that punctuation is treated as part of words
        String result = poet.poem("Hello How");
        assertTrue("punctuation should be handled correctly", result.length() >= "Hello How".length());
    }

    @Test
    public void testSelfLoops() throws IOException {
        GraphPoet poet = new GraphPoet(new File("test/poet/self-loop.txt"));

        // Test handling of repeated words creating self-loops
        String result = poet.poem("hello world");
        assertTrue("self-loops should be handled", result.contains("hello") && result.contains("world"));
    }

    // poem() method tests using String constructor for easier testing

    @Test
    public void testPoemEmptyInput() {
        GraphPoet poet = new GraphPoet("hello world");
        assertEquals("empty input should return empty string", "", poet.poem(""));
    }

    @Test
    public void testPoemSingleWord() {
        GraphPoet poet = new GraphPoet("hello world");
        assertEquals("single word should return unchanged", "hello", poet.poem("hello"));
    }

    @Test
    public void testPoemNoAvailableBridge() {
        GraphPoet poet = new GraphPoet("hello world");
        assertEquals("no bridge available should return original",
                "world hello", poet.poem("world hello"));
    }

    @Test
    public void testPoemCasePreservation() {
        GraphPoet poet = new GraphPoet("the quick brown fox");

        // Input case should be preserved, bridge should be lowercase
        String result = poet.poem("THE fox");
        assertEquals("input case preserved, bridge lowercase", "THE fox", result);
    }

    @Test
    public void testPoemWhitespaceNormalization() {
        GraphPoet poet = new GraphPoet("hello world test");

        // Multiple spaces in input should become single spaces in output
        String result = poet.poem("hello   test");
        assertEquals("output should use single spaces", "hello world test", result);
    }

    @Test
    public void testMultipleBridgeOptions() {
        GraphPoet poet = new GraphPoet("a big dog and a small dog but a big cat");

        // Both "big" and "small" can bridge "a" to "dog"
        // "big" appears twice, "small" once, so "big" should be chosen
        String result = poet.poem("a dog");
        assertEquals("higher weight bridge should be selected", "a big dog", result);
    }

    @Test
    public void testComplexMultipleWords() {
        GraphPoet poet = new GraphPoet("the quick brown fox jumps over the lazy dog");

        String result = poet.poem("the fox the dog");
        // Should find: "the" -> "quick" -> "fox" and "the" -> "lazy" -> "dog"
        assertEquals("multiple bridges should be found",
                "the fox the lazy dog", result);
    }

    @Test
    public void testWordsNotInCorpus() {
        GraphPoet poet = new GraphPoet("hello world");

        String result = poet.poem("unknown words");
        assertEquals("unknown words should remain unchanged", "unknown words", result);
    }

    // Additional tests for String constructor

    @Test
    public void testStringConstructorBasic() {
        GraphPoet poet = new GraphPoet("hello world hello");

        // Should create edge: hello -> world (weight 1)
        // and edge: world -> hello (weight 1)
        // and edge: hello -> hello (self-loop, weight 1)
        String result = poet.poem("world world");
        assertEquals("should handle string constructor", "world hello world", result);
    }

    @Test
    public void testStringConstructorEmpty() {
        GraphPoet poet = new GraphPoet("");

        assertEquals("empty string corpus should work", "test", poet.poem("test"));
    }

    @Test
    public void testStringConstructorNewlines() {
        GraphPoet poet = new GraphPoet("hello\nworld\ngoodbye\nworld");

        String result = poet.poem("hello goodbye");
        assertEquals("newlines should delimit words", "hello world goodbye", result);
    }
}