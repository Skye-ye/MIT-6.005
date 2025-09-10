/* Copyright (c) 2015-2016 MIT 6.005 course staff, all rights reserved.
 * Redistribution of original or derived work requires permission of course staff.
 */
package graph;

import static org.junit.Assert.*;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

/**
 * Tests for instance methods of Graph.
 * 
 * <p>PS2 instructions: you MUST NOT add constructors, fields, or non-@Test
 * methods to this class, or change the spec of {@link #emptyInstance()}.
 * Your tests MUST only obtain Graph instances by calling emptyInstance().
 * Your tests MUST NOT refer to specific concrete implementations.
 */
public abstract class GraphInstanceTest {

    /*
     * Testing strategy for Graph interface:
     *
     * Partition the input space for each method:
     *
     * add(vertex):
     * - Graph state: empty, non-empty
     * - Vertex: not in graph, already in graph
     * - Vertex type: null (if allowed), valid immutable objects
     *
     * set(source, target, weight):
     * - Graph state: empty, non-empty
     * - Source vertex: not in graph, in graph
     * - Target vertex: not in graph, in graph, same as source (self-loop)
     * - Weight: 0 (remove edge), positive (add/update edge)
     * - Edge existence: no existing edge, existing edge with same weight, existing edge with different weight
     *
     * remove(vertex):
     * - Graph state: empty, single vertex, multiple vertices
     * - Vertex: not in graph, in graph with no edges, in graph with incoming edges only,
     *           in graph with outgoing edges only, in graph with both incoming and outgoing edges
     *
     * vertices():
     * - Graph state: empty, single vertex, multiple vertices
     * - Mutation: ensure returned set doesn't allow modification of graph
     *
     * sources(target):
     * - Graph state: empty, non-empty
     * - Target vertex: not in graph, in graph with no incoming edges,
     *                  in graph with single incoming edge, in graph with multiple incoming edges
     * - Edge weights: various positive values
     * - Mutation: ensure returned map doesn't allow modification of graph
     *
     * targets(source):
     * - Graph state: empty, non-empty
     * - Source vertex: not in graph, in graph with no outgoing edges,
     *                  in graph with single outgoing edge, in graph with multiple outgoing edges
     * - Edge weights: various positive values, self-loops
     * - Mutation: ensure returned map doesn't allow modification of graph
     *
     * Cross-method interactions:
     * - Operations maintain graph invariants
     * - Vertex removal properly removes associated edges
     * - Edge operations properly add vertices if needed
     *
     * Edge cases:
     * - Self-loops (source == target)
     * - Isolated vertices (no edges)
     * - Multiple edges between same vertex pair with different weights over time
     * - Large weights (within int range)
     */
    
    /**
     * Overridden by implementation-specific test classes.
     * 
     * @return a new empty graph of the particular implementation being tested
     */
    public abstract Graph<String> emptyInstance();
    
    @Test(expected=AssertionError.class)
    public void testAssertionsEnabled() {
        assert false; // make sure assertions are enabled with VM argument: -ea
    }
    
    @Test
    public void testInitialVerticesEmpty() {
        // TODO you may use, change, or remove this test
        assertEquals("expected new graph to have no vertices",
                Collections.emptySet(), emptyInstance().vertices());
    }

    // Tests for add() method

    @Test
    public void testAddVertexToEmptyGraph() {
        Graph<String> graph = emptyInstance();
        assertTrue("adding vertex to empty graph should return true",
                graph.add("vertex1"));
        assertTrue("graph should contain the added vertex",
                graph.vertices().contains("vertex1"));
        assertEquals("graph should have exactly one vertex",
                1, graph.vertices().size());
    }

    @Test
    public void testAddDuplicateVertex() {
        Graph<String> graph = emptyInstance();
        assertTrue("first add should return true", graph.add("vertex1"));
        assertFalse("adding duplicate vertex should return false",
                graph.add("vertex1"));
        assertEquals("graph should still have exactly one vertex",
                1, graph.vertices().size());
    }

    @Test
    public void testAddMultipleDistinctVertices() {
        Graph<String> graph = emptyInstance();
        assertTrue("adding first vertex should return true", graph.add("A"));
        assertTrue("adding second vertex should return true", graph.add("B"));
        assertTrue("adding third vertex should return true", graph.add("C"));

        Set<String> expected = new HashSet<>();
        expected.add("A");
        expected.add("B");
        expected.add("C");
        assertEquals("graph should contain all added vertices", expected, graph.vertices());
    }

    // Tests for set() method

    @Test
    public void testSetEdgeInEmptyGraph() {
        Graph<String> graph = emptyInstance();
        int previousWeight = graph.set("A", "B", 5);
        assertEquals("no previous edge should exist", 0, previousWeight);

        assertTrue("source vertex should be added", graph.vertices().contains("A"));
        assertTrue("target vertex should be added", graph.vertices().contains("B"));
        assertEquals("graph should have exactly two vertices", 2, graph.vertices().size());
    }

    @Test
    public void testSetEdgeWithExistingVertices() {
        Graph<String> graph = emptyInstance();
        graph.add("A");
        graph.add("B");

        int previousWeight = graph.set("A", "B", 10);
        assertEquals("no previous edge should exist", 0, previousWeight);
        assertEquals("graph should still have exactly two vertices", 2, graph.vertices().size());
    }

    @Test
    public void testSetEdgeUpdateWeight() {
        Graph<String> graph = emptyInstance();
        graph.set("A", "B", 5);
        int previousWeight = graph.set("A", "B", 15);
        assertEquals("previous weight should be returned", 5, previousWeight);
    }

    @Test
    public void testSetEdgeRemoveWithZeroWeight() {
        Graph<String> graph = emptyInstance();
        graph.set("A", "B", 10);
        int previousWeight = graph.set("A", "B", 0);
        assertEquals("previous weight should be returned", 10, previousWeight);

        // Vertices should remain even after edge removal
        assertTrue("source vertex should remain", graph.vertices().contains("A"));
        assertTrue("target vertex should remain", graph.vertices().contains("B"));
    }

    @Test
    public void testSetSelfLoop() {
        Graph<String> graph = emptyInstance();
        int previousWeight = graph.set("A", "A", 3);
        assertEquals("no previous edge should exist", 0, previousWeight);
        assertTrue("vertex should be added", graph.vertices().contains("A"));
        assertEquals("graph should have exactly one vertex", 1, graph.vertices().size());
    }

    @Test
    public void testSetRemoveNonexistentEdge() {
        Graph<String> graph = emptyInstance();
        graph.add("A");
        graph.add("B");
        int previousWeight = graph.set("A", "B", 0);
        assertEquals("no previous edge to remove", 0, previousWeight);
    }

    // Tests for remove() method

    @Test
    public void testRemoveFromEmptyGraph() {
        Graph<String> graph = emptyInstance();
        assertFalse("removing from empty graph should return false",
                graph.remove("nonexistent"));
        assertTrue("graph should remain empty", graph.vertices().isEmpty());
    }

    @Test
    public void testRemoveExistingVertex() {
        Graph<String> graph = emptyInstance();
        graph.add("A");
        assertTrue("removing existing vertex should return true", graph.remove("A"));
        assertTrue("graph should be empty after removal", graph.vertices().isEmpty());
    }

    @Test
    public void testRemoveNonexistentVertex() {
        Graph<String> graph = emptyInstance();
        graph.add("A");
        assertFalse("removing nonexistent vertex should return false",
                graph.remove("B"));
        assertTrue("existing vertex should remain", graph.vertices().contains("A"));
    }

    @Test
    public void testRemoveVertexWithIncomingEdges() {
        Graph<String> graph = emptyInstance();
        graph.set("A", "B", 5);
        graph.set("C", "B", 10);

        assertTrue("removing vertex with incoming edges should return true",
                graph.remove("B"));
        assertFalse("removed vertex should not exist", graph.vertices().contains("B"));
        assertTrue("source vertices should remain", graph.vertices().contains("A"));
        assertTrue("source vertices should remain", graph.vertices().contains("C"));

        // Check that incoming edges are removed
        assertTrue("sources of removed vertex should be empty",
                graph.sources("B").isEmpty());
    }

    @Test
    public void testRemoveVertexWithOutgoingEdges() {
        Graph<String> graph = emptyInstance();
        graph.set("A", "B", 5);
        graph.set("A", "C", 10);

        assertTrue("removing vertex with outgoing edges should return true",
                graph.remove("A"));
        assertFalse("removed vertex should not exist", graph.vertices().contains("A"));
        assertTrue("target vertices should remain", graph.vertices().contains("B"));
        assertTrue("target vertices should remain", graph.vertices().contains("C"));

        // Check that outgoing edges are removed
        assertTrue("targets of removed vertex should be empty",
                graph.targets("A").isEmpty());
    }

    @Test
    public void testRemoveVertexWithBothIncomingAndOutgoingEdges() {
        Graph<String> graph = emptyInstance();
        graph.set("A", "B", 5);
        graph.set("B", "C", 10);
        graph.set("D", "B", 15);

        assertTrue("removing vertex with both types of edges should return true",
                graph.remove("B"));
        assertFalse("removed vertex should not exist", graph.vertices().contains("B"));

        // All other vertices should remain
        assertTrue("vertex A should remain", graph.vertices().contains("A"));
        assertTrue("vertex C should remain", graph.vertices().contains("C"));
        assertTrue("vertex D should remain", graph.vertices().contains("D"));

        // All edges involving B should be removed
        assertTrue("targets of A should be empty", graph.targets("A").isEmpty());
        assertTrue("sources of C should be empty", graph.sources("C").isEmpty());
        assertTrue("targets of D should be empty", graph.targets("D").isEmpty());
    }

    // Tests for vertices() method

    @Test
    public void testVerticesEmptyGraph() {
        Graph<String> graph = emptyInstance();
        Set<String> vertices = graph.vertices();
        assertTrue("empty graph should have no vertices", vertices.isEmpty());
    }

    @Test
    public void testVerticesSingleVertex() {
        Graph<String> graph = emptyInstance();
        graph.add("A");
        Set<String> vertices = graph.vertices();
        assertEquals("graph should have exactly one vertex", 1, vertices.size());
        assertTrue("graph should contain vertex A", vertices.contains("A"));
    }

    @Test
    public void testVerticesMultipleVertices() {
        Graph<String> graph = emptyInstance();
        graph.add("A");
        graph.add("B");
        graph.set("C", "D", 5); // This adds both C and D

        Set<String> vertices = graph.vertices();
        assertEquals("graph should have exactly four vertices", 4, vertices.size());
        assertTrue("graph should contain all added vertices",
                vertices.contains("A") && vertices.contains("B") &&
                        vertices.contains("C") && vertices.contains("D"));
    }

    // Tests for sources() method

    @Test
    public void testSourcesEmptyGraph() {
        Graph<String> graph = emptyInstance();
        Map<String, Integer> sources = graph.sources("nonexistent");
        assertTrue("sources of vertex in empty graph should be empty", sources.isEmpty());
    }

    @Test
    public void testSourcesVertexNotInGraph() {
        Graph<String> graph = emptyInstance();
        graph.add("A");
        Map<String, Integer> sources = graph.sources("B");
        assertTrue("sources of nonexistent vertex should be empty", sources.isEmpty());
    }

    @Test
    public void testSourcesVertexWithNoIncomingEdges() {
        Graph<String> graph = emptyInstance();
        graph.add("A");
        Map<String, Integer> sources = graph.sources("A");
        assertTrue("vertex with no incoming edges should have empty sources",
                sources.isEmpty());
    }

    @Test
    public void testSourcesSingleIncomingEdge() {
        Graph<String> graph = emptyInstance();
        graph.set("A", "B", 10);
        Map<String, Integer> sources = graph.sources("B");

        assertEquals("should have exactly one source", 1, sources.size());
        assertTrue("should contain source A", sources.containsKey("A"));
        assertEquals("weight should be correct", (Integer) 10, sources.get("A"));
    }

    @Test
    public void testSourcesMultipleIncomingEdges() {
        Graph<String> graph = emptyInstance();
        graph.set("A", "C", 5);
        graph.set("B", "C", 15);
        Map<String, Integer> sources = graph.sources("C");

        assertEquals("should have exactly two sources", 2, sources.size());
        assertTrue("should contain source A", sources.containsKey("A"));
        assertTrue("should contain source B", sources.containsKey("B"));
        assertEquals("weight from A should be correct", (Integer) 5, sources.get("A"));
        assertEquals("weight from B should be correct", (Integer) 15, sources.get("B"));
    }

    @Test
    public void testSourcesSelfLoop() {
        Graph<String> graph = emptyInstance();
        graph.set("A", "A", 7);
        Map<String, Integer> sources = graph.sources("A");

        assertEquals("should have exactly one source (itself)", 1, sources.size());
        assertTrue("should contain source A", sources.containsKey("A"));
        assertEquals("weight should be correct", (Integer) 7, sources.get("A"));
    }

    // Tests for targets() method

    @Test
    public void testTargetsEmptyGraph() {
        Graph<String> graph = emptyInstance();
        Map<String, Integer> targets = graph.targets("nonexistent");
        assertTrue("targets of vertex in empty graph should be empty", targets.isEmpty());
    }

    @Test
    public void testTargetsVertexNotInGraph() {
        Graph<String> graph = emptyInstance();
        graph.add("A");
        Map<String, Integer> targets = graph.targets("B");
        assertTrue("targets of nonexistent vertex should be empty", targets.isEmpty());
    }

    @Test
    public void testTargetsVertexWithNoOutgoingEdges() {
        Graph<String> graph = emptyInstance();
        graph.add("A");
        Map<String, Integer> targets = graph.targets("A");
        assertTrue("vertex with no outgoing edges should have empty targets",
                targets.isEmpty());
    }

    @Test
    public void testTargetsSingleOutgoingEdge() {
        Graph<String> graph = emptyInstance();
        graph.set("A", "B", 20);
        Map<String, Integer> targets = graph.targets("A");

        assertEquals("should have exactly one target", 1, targets.size());
        assertTrue("should contain target B", targets.containsKey("B"));
        assertEquals("weight should be correct", (Integer) 20, targets.get("B"));
    }

    @Test
    public void testTargetsMultipleOutgoingEdges() {
        Graph<String> graph = emptyInstance();
        graph.set("A", "B", 10);
        graph.set("A", "C", 30);
        Map<String, Integer> targets = graph.targets("A");

        assertEquals("should have exactly two targets", 2, targets.size());
        assertTrue("should contain target B", targets.containsKey("B"));
        assertTrue("should contain target C", targets.containsKey("C"));
        assertEquals("weight to B should be correct", (Integer) 10, targets.get("B"));
        assertEquals("weight to C should be correct", (Integer) 30, targets.get("C"));
    }

    @Test
    public void testTargetsSelfLoop() {
        Graph<String> graph = emptyInstance();
        graph.set("A", "A", 12);
        Map<String, Integer> targets = graph.targets("A");

        assertEquals("should have exactly one target (itself)", 1, targets.size());
        assertTrue("should contain target A", targets.containsKey("A"));
        assertEquals("weight should be correct", (Integer) 12, targets.get("A"));
    }

    // Integration tests

    @Test
    public void testComplexGraphOperations() {
        Graph<String> graph = emptyInstance();

        // Build a complex graph
        graph.set("A", "B", 1);
        graph.set("B", "C", 2);
        graph.set("C", "A", 3);
        graph.set("A", "A", 4); // self-loop
        graph.add("D"); // isolated vertex

        // Verify the graph structure
        assertEquals("should have 4 vertices", 4, graph.vertices().size());

        Map<String, Integer> targetsA = graph.targets("A");
        assertEquals("A should have 2 targets", 2, targetsA.size());
        assertEquals("edge A->B weight", (Integer) 1, targetsA.get("B"));
        assertEquals("edge A->A weight", (Integer) 4, targetsA.get("A"));

        Map<String, Integer> sourcesA = graph.sources("A");
        assertEquals("A should have 2 sources", 2, sourcesA.size());
        assertEquals("edge C->A weight", (Integer) 3, sourcesA.get("C"));
        assertEquals("edge A->A weight", (Integer) 4, sourcesA.get("A"));

        // Test edge updates
        int oldWeight = graph.set("A", "B", 10);
        assertEquals("previous weight should be returned", 1, oldWeight);
        assertEquals("new weight should be set", (Integer) 10, graph.targets("A").get("B"));

        // Test vertex removal with edges
        assertTrue("removing C should succeed", graph.remove("C"));
        assertFalse("C should no longer exist", graph.vertices().contains("C"));
        assertFalse("edge A->A should not be affected", graph.targets("A").containsKey("C"));
        assertTrue("edge A->A should still exist", graph.targets("A").containsKey("A"));
    }

    @Test
    public void testEdgeRemovalAndReaddition() {
        Graph<String> graph = emptyInstance();

        // Add edge
        graph.set("X", "Y", 100);
        assertEquals("edge should exist with correct weight",
                (Integer) 100, graph.targets("X").get("Y"));

        // Remove edge
        int removedWeight = graph.set("X", "Y", 0);
        assertEquals("removed weight should be returned", 100, removedWeight);
        assertTrue("edge should be removed", graph.targets("X").isEmpty());
        assertTrue("vertices should still exist",
                graph.vertices().contains("X") && graph.vertices().contains("Y"));

        // Re-add edge with different weight
        int previousWeight = graph.set("X", "Y", 200);
        assertEquals("no previous edge after removal", 0, previousWeight);
        assertEquals("new edge should have correct weight",
                (Integer) 200, graph.targets("X").get("Y"));
    }
}
