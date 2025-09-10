/* Copyright (c) 2015-2016 MIT 6.005 course staff, all rights reserved.
 * Redistribution of original or derived work requires permission of course staff.
 */
package graph;

import static org.junit.Assert.*;

import java.util.Collections;
import java.util.Set;

import org.junit.Test;

/**
 * Tests for static methods of Graph.
 * <p>
 * To facilitate testing multiple implementations of Graph, instance methods are
 * tested in GraphInstanceTest.
 */
public class GraphStaticTest {

    // Testing strategy
    //   empty()
    //     no inputs, only output is empty graph
    //     observe with vertices()
    //
    //   Test with different immutable label types:
    //     String: basic string labels
    //     Integer: numeric labels
    //     Character: single character labels
    //     Custom immutable class: verify works with user-defined immutable types

    @Test(expected=AssertionError.class)
    public void testAssertionsEnabled() {
        assert false; // make sure assertions are enabled with VM argument: -ea
    }

    @Test
    public void testEmptyVerticesEmpty() {
        assertEquals("expected empty() graph to have no vertices",
                Collections.emptySet(), Graph.empty().vertices());
    }

    // Test with String labels (default case)
    @Test
    public void testEmptyGraphWithStringLabels() {
        Graph<String> graph = Graph.empty();
        assertTrue("empty graph should have no vertices", graph.vertices().isEmpty());

        // Add some vertices to verify the type works
        assertTrue("adding new string vertex should return true", graph.add("vertex1"));
        assertTrue("adding another string vertex should return true", graph.add("vertex2"));
        assertFalse("adding duplicate string vertex should return false", graph.add("vertex1"));

        Set<String> vertices = graph.vertices();
        assertEquals("should have exactly 2 vertices", 2, vertices.size());
        assertTrue("should contain vertex1", vertices.contains("vertex1"));
        assertTrue("should contain vertex2", vertices.contains("vertex2"));
    }

    // Test with Integer labels
    @Test
    public void testEmptyGraphWithIntegerLabels() {
        Graph<Integer> graph = Graph.empty();
        assertTrue("empty graph should have no vertices", graph.vertices().isEmpty());

        // Add some integer vertices
        assertTrue("adding new integer vertex should return true", graph.add(1));
        assertTrue("adding another integer vertex should return true", graph.add(42));
        assertTrue("adding negative integer vertex should return true", graph.add(-5));
        assertFalse("adding duplicate integer vertex should return false", graph.add(1));

        Set<Integer> vertices = graph.vertices();
        assertEquals("should have exactly 3 vertices", 3, vertices.size());
        assertTrue("should contain vertex 1", vertices.contains(1));
        assertTrue("should contain vertex 42", vertices.contains(42));
        assertTrue("should contain vertex -5", vertices.contains(-5));

        // Test edge operations with integers
        assertEquals("no previous edge should exist", 0, graph.set(1, 42, 10));
        assertEquals("should return previous weight", 10, graph.set(1, 42, 20));
    }

    // Test with Character labels
    @Test
    public void testEmptyGraphWithCharacterLabels() {
        Graph<Character> graph = Graph.empty();
        assertTrue("empty graph should have no vertices", graph.vertices().isEmpty());

        // Add some character vertices
        assertTrue("adding character 'A' should return true", graph.add('A'));
        assertTrue("adding character 'B' should return true", graph.add('B'));
        assertTrue("adding character 'z' should return true", graph.add('z'));
        assertFalse("adding duplicate character should return false", graph.add('A'));

        Set<Character> vertices = graph.vertices();
        assertEquals("should have exactly 3 vertices", 3, vertices.size());
        assertTrue("should contain vertex 'A'", vertices.contains('A'));
        assertTrue("should contain vertex 'B'", vertices.contains('B'));
        assertTrue("should contain vertex 'z'", vertices.contains('z'));

        // Test edge operations with characters
        assertEquals("no previous edge should exist", 0, graph.set('A', 'B', 5));
        assertTrue("sources should contain A", graph.sources('B').containsKey('A'));
        assertTrue("targets should contain B", graph.targets('A').containsKey('B'));
    }

    /**
         * Simple immutable class for testing custom label types.
         */
        private record TestLabel(String name, int id) {

        @Override
            public boolean equals(Object obj) {
                if (!(obj instanceof TestLabel(String name1, int id1))) {
                    return false;
                }
                return name.equals(name1) && id == id1;
            }

        @Override
            public String toString() {
                return name + ":" + id;
            }
        }

    // Test with custom immutable class labels
    @Test
    public void testEmptyGraphWithCustomLabels() {
        Graph<TestLabel> graph = Graph.empty();
        assertTrue("empty graph should have no vertices", graph.vertices().isEmpty());

        TestLabel label1 = new TestLabel("first", 1);
        TestLabel label2 = new TestLabel("second", 2);
        TestLabel label1Duplicate = new TestLabel("first", 1);

        // Add custom label vertices
        assertTrue("adding new custom vertex should return true", graph.add(label1));
        assertTrue("adding another custom vertex should return true", graph.add(label2));
        assertFalse("adding duplicate custom vertex should return false", graph.add(label1Duplicate));

        Set<TestLabel> vertices = graph.vertices();
        assertEquals("should have exactly 2 vertices", 2, vertices.size());
        assertTrue("should contain first label", vertices.contains(label1));
        assertTrue("should contain second label", vertices.contains(label2));

        // Test edge operations with custom labels
        assertEquals("no previous edge should exist", 0, graph.set(label1, label2, 15));
        assertEquals("should have correct edge weight", (Integer) 15,
                graph.targets(label1).get(label2));
        assertEquals("should have correct reverse lookup", (Integer) 15,
                graph.sources(label2).get(label1));
    }

    // Test that different label types can coexist (separate graph instances)
    @Test
    public void testMultipleGraphsWithDifferentLabelTypes() {
        Graph<String> stringGraph = Graph.empty();
        Graph<Integer> intGraph = Graph.empty();

        // Operate on both graphs independently
        stringGraph.add("hello");
        intGraph.add(100);

        stringGraph.set("A", "B", 1);
        intGraph.set(1, 2, 10);

        // Verify they remain independent and correctly typed
        assertEquals("string graph should have 3 vertices", 3, stringGraph.vertices().size());
        assertEquals("int graph should have 3 vertices", 3, intGraph.vertices().size());

        assertTrue("string graph should contain string vertices",
                stringGraph.vertices().contains("A"));
        assertTrue("int graph should contain integer vertices",
                intGraph.vertices().contains(1));

        // Verify edges work correctly for each type
        assertEquals("string graph should have correct edge weight", (Integer) 1,
                stringGraph.targets("A").get("B"));
        assertEquals("int graph should have correct edge weight", (Integer) 10,
                intGraph.targets(1).get(2));
    }
}