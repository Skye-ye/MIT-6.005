/* Copyright (c) 2015-2016 MIT 6.005 course staff, all rights reserved.
 * Redistribution of original or derived work requires permission of course staff.
 */
package graph;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * Tests for ConcreteEdgesGraph.
 * <p>
 * This class runs the GraphInstanceTest tests against ConcreteEdgesGraph, as
 * well as tests for that particular implementation.
 * <p>
 * Tests against the Graph spec should be in GraphInstanceTest.
 */
public class ConcreteEdgesGraphTest extends GraphInstanceTest {

    // Test fixtures for common vertex labels
    private static final String VERTEX_A = "A";
    private static final String VERTEX_B = "B";
    private static final String VERTEX_C = "C";
    private static final String VERTEX_X = "X";
    private static final String VERTEX_Y = "Y";

    // Common edge weights
    private static final int WEIGHT_5 = 5;
    private static final int WEIGHT_7 = 7;
    private static final int WEIGHT_10 = 10;
    private static final int WEIGHT_15 = 15;
    private static final int WEIGHT_42 = 42;

    // Special test strings
    private static final String VERTEX_WITH_SPACES = "vertex with spaces";
    private static final String VERTEX_WITH_DASHES = "vertex-with-dashes";

    /*
     * Provide a ConcreteEdgesGraph for tests in GraphInstanceTest.
     */
    @Override public Graph<String> emptyInstance() {
        return new ConcreteEdgesGraph<>();
    }

    /*
     * Testing ConcreteEdgesGraph...
     */

    // Testing strategy for ConcreteEdgesGraph.toString()
    // Input space partitioning:
    // - Graph state: empty, single vertex (no edges), single vertex with self-loop,
    //                multiple vertices with no edges, multiple vertices with edges
    // - Edge configurations: no edges, single edge, multiple edges, self-loops
    //
    // Output space partitioning:
    // - String format should show vertices and edges clearly
    // - Should be parseable/readable for debugging
    // - Should handle empty collections properly

    @Test
    public void testToStringEmptyGraph() {
        ConcreteEdgesGraph<String> graph = new ConcreteEdgesGraph<>();
        String result = graph.toString();

        assertContainsExpectedElements(result, "ConcreteEdgesGraph", "vertices", "edges");
    }

    @Test
    public void testToStringSingleVertex() {
        ConcreteEdgesGraph<String> graph = new ConcreteEdgesGraph<>();
        graph.add(VERTEX_A);
        String result = graph.toString();

        assertContainsExpectedElements(result, VERTEX_A, "vertices", "edges");
    }

    @Test
    public void testToStringWithEdges() {
        ConcreteEdgesGraph<String> graph = createGraphWithEdges();
        String result = graph.toString();

        assertContainsExpectedElements(result, VERTEX_A, VERTEX_B, VERTEX_C);
        assertTrue("toString should show edge structure",
                result.contains("->") || result.contains("edges"));
        assertContainsExpectedElements(result, "{", "}");
    }

    @Test
    public void testToStringWithSelfLoop() {
        ConcreteEdgesGraph<String> graph = new ConcreteEdgesGraph<>();
        graph.set(VERTEX_X, VERTEX_X, WEIGHT_7);
        String result = graph.toString();

        assertContainsExpectedElements(result, VERTEX_X, String.valueOf(WEIGHT_7));
        assertTrue("toString should show self-loop",
                result.contains("->") || result.contains("edges"));
    }

    /*
     * Testing Edge...
     */

    // Testing strategy for Edge
    //
    // Constructor:
    // - Source: null, valid string
    // - Target: null, valid string, same as source (self-loop)
    // - Weight: negative, zero, positive
    //
    // getSource(), getTarget(), getWeight():
    // - Return correct values from constructor
    //
    // connects(source, target):
    // - Source/target: null, non-matching strings, matching strings
    // - Edge type: normal edge, self-loop
    //
    // involves(vertex):
    // - Vertex: null, not involved, source only, target only, both (self-loop)
    //
    // hasSource(vertex), hasTarget(vertex):
    // - Vertex: null, matching, non-matching
    //
    // equals(Object):
    // - Object: null, different type, Edge with different source/target/weight,
    //          Edge with same source/target/weight
    //
    // hashCode():
    // - Consistency with equals
    //
    // toString():
    // - Format should be readable and show all edge information

    // Constructor tests
    @Test
    public void testEdgeConstructorValid() {
        Edge<String> edge = createEdge(VERTEX_A, VERTEX_B, WEIGHT_5);

        assertEquals("source should be set correctly", VERTEX_A, edge.getSource());
        assertEquals("target should be set correctly", VERTEX_B, edge.getTarget());
        assertEquals("weight should be set correctly", WEIGHT_5, edge.getWeight());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEdgeConstructorNullSource() {
        new Edge<>(null, VERTEX_B, WEIGHT_5);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEdgeConstructorNullTarget() {
        new Edge<>(VERTEX_A, null, WEIGHT_5);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEdgeConstructorZeroWeight() {
        new Edge<>(VERTEX_A, VERTEX_B, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEdgeConstructorNegativeWeight() {
        new Edge<>(VERTEX_A, VERTEX_B, -1);
    }

    @Test
    public void testEdgeConstructorSelfLoop() {
        Edge<String> edge = createEdge(VERTEX_A, VERTEX_A, WEIGHT_10);

        assertEquals("self-loop source should be correct", VERTEX_A, edge.getSource());
        assertEquals("self-loop target should be correct", VERTEX_A, edge.getTarget());
        assertEquals("self-loop weight should be correct", WEIGHT_10, edge.getWeight());
    }

    // Edge query method tests
    @Test
    public void testEdgeConnects() {
        Edge<String> edge = createEdge(VERTEX_X, VERTEX_Y, 3);

        assertTrue("should connect X to Y", edge.connects(VERTEX_X, VERTEX_Y));
        assertFalse("should not connect Y to X", edge.connects(VERTEX_Y, VERTEX_X));
        assertFalse("should not connect X to Z", edge.connects(VERTEX_X, "Z"));
        assertFalse("should not connect with null source", edge.connects(null, VERTEX_Y));
        assertFalse("should not connect with null target", edge.connects(VERTEX_X, null));
    }

    @Test
    public void testEdgeConnectsSelfLoop() {
        Edge<String> edge = createEdge(VERTEX_A, VERTEX_A, WEIGHT_5);

        assertTrue("self-loop should connect A to A", edge.connects(VERTEX_A, VERTEX_A));
        assertFalse("self-loop should not connect A to B", edge.connects(VERTEX_A, VERTEX_B));
    }

    @Test
    public void testEdgeInvolves() {
        Edge<String> edge = createEdge("P", "Q", 8);

        assertTrue("should involve source P", edge.involves("P"));
        assertTrue("should involve target Q", edge.involves("Q"));
        assertFalse("should not involve R", edge.involves("R"));
        assertFalse("should not involve null", edge.involves(null));
    }

    @Test
    public void testEdgeInvolvesSelfLoop() {
        Edge<String> edge = createEdge(VERTEX_A, VERTEX_A, 2);

        assertTrue("self-loop should involve A", edge.involves(VERTEX_A));
        assertFalse("self-loop should not involve B", edge.involves(VERTEX_B));
    }

    @Test
    public void testEdgeHasSource() {
        Edge<String> edge = createEdge("M", "N", WEIGHT_15);

        assertTrue("should have source M", edge.hasSource("M"));
        assertFalse("should not have source N", edge.hasSource("N"));
        assertFalse("should not have null source", edge.hasSource(null));
    }

    @Test
    public void testEdgeHasTarget() {
        Edge<String> edge = createEdge("M", "N", WEIGHT_15);

        assertTrue("should have target N", edge.hasTarget("N"));
        assertFalse("should not have target M", edge.hasTarget("M"));
        assertFalse("should not have null target", edge.hasTarget(null));
    }

    // Edge equals and hashCode tests
    @Test
    public void testEdgeEquals() {
        Edge<String> edge1 = createEdge(VERTEX_A, VERTEX_B, WEIGHT_10);
        Edge<String> edge2 = createEdge(VERTEX_A, VERTEX_B, WEIGHT_10);
        Edge<String> edge3 = createEdge(VERTEX_A, VERTEX_B, WEIGHT_5);
        Edge<String> edge4 = createEdge(VERTEX_B, VERTEX_A, WEIGHT_10);
        Edge<String> edge5 = createEdge(VERTEX_A, VERTEX_C, WEIGHT_10);

        // Test equality
        assertEquals("edges with same source, target, weight should be equal", edge1, edge2);
        assertEquals("equals should be reflexive", edge1, edge1);
        assertTrue("equals should be symmetric",
                edge1.equals(edge2) && edge2.equals(edge1));

        // Test inequality
        assertNotEquals("edges with different weights should not be equal", edge1, edge3);
        assertNotEquals("edges with swapped source/target should not be equal", edge1, edge4);
        assertNotEquals("edges with different targets should not be equal", edge1, edge5);
        assertNotEquals("edge should not equal null", null, edge1);
        assertNotEquals("edge should not equal different type", "not an edge", edge1);
    }

    @Test
    public void testEdgeHashCode() {
        Edge<String> edge1 = createEdge(VERTEX_A, VERTEX_B, WEIGHT_10);
        Edge<String> edge2 = createEdge(VERTEX_A, VERTEX_B, WEIGHT_10);

        assertEquals("equal edges should have equal hash codes",
                edge1.hashCode(), edge2.hashCode());
        assertEquals("hash code should be consistent", edge1.hashCode(), edge1.hashCode());
    }

    // Edge toString tests
    @Test
    public void testEdgeToString() {
        Edge<String> edge = createEdge("start", "end", WEIGHT_42);
        String result = edge.toString();

        assertContainsExpectedElements(result, "start", "end", String.valueOf(WEIGHT_42));
        assertTrue("toString should show direction",
                result.contains("->") || result.contains("to"));
    }

    @Test
    public void testEdgeToStringSelfLoop() {
        Edge<String> edge = createEdge("loop", "loop", 1);
        String result = edge.toString();

        assertContainsExpectedElements(result, "loop", "1");
    }

    // Edge cases and boundary tests
    @Test
    public void testEdgeWithLargeWeight() {
        Edge<String> edge = createEdge(VERTEX_A, VERTEX_B, Integer.MAX_VALUE);
        assertEquals("should handle large weights", Integer.MAX_VALUE, edge.getWeight());
    }

    @Test
    public void testEdgeWithSpecialCharacters() {
        Edge<String> edge = createEdge(VERTEX_WITH_SPACES, VERTEX_WITH_DASHES, 1);

        assertEquals("should handle spaces in vertex names",
                VERTEX_WITH_SPACES, edge.getSource());
        assertEquals("should handle dashes in vertex names",
                VERTEX_WITH_DASHES, edge.getTarget());
        assertTrue("toString should handle special characters",
                edge.toString().contains(VERTEX_WITH_SPACES));
    }

    @Test
    public void testEdgeImmutability() {
        Edge<String> edge = createEdge(VERTEX_A, VERTEX_B, WEIGHT_5);

        // Store original values
        String originalSource = edge.getSource();
        String originalTarget = edge.getTarget();
        int originalWeight = edge.getWeight();

        // Multiple calls should return consistent values
        assertEquals("source should be consistent", originalSource, edge.getSource());
        assertEquals("target should be consistent", originalTarget, edge.getTarget());
        assertEquals("weight should be consistent", originalWeight, edge.getWeight());
    }

    // Integration tests
    @Test
    public void testEdgeOperationsIntegration() {
        Edge<String> edge = createEdge("hub", "spoke", 100);

        // Test all query methods together
        assertTrue("should connect hub to spoke", edge.connects("hub", "spoke"));
        assertFalse("should not connect spoke to hub", edge.connects("spoke", "hub"));

        assertTrue("should involve hub", edge.involves("hub"));
        assertTrue("should involve spoke", edge.involves("spoke"));
        assertFalse("should not involve other vertex", edge.involves("other"));

        assertTrue("should have source hub", edge.hasSource("hub"));
        assertTrue("should have target spoke", edge.hasTarget("spoke"));
        assertFalse("should not have source spoke", edge.hasSource("spoke"));
        assertFalse("should not have target hub", edge.hasTarget("hub"));

        assertEquals("weight should be correct", 100, edge.getWeight());
    }

    // Helper methods to reduce duplication and improve readability

    private ConcreteEdgesGraph<String> createGraphWithEdges() {
        ConcreteEdgesGraph<String> graph = new ConcreteEdgesGraph<>();
        graph.set(VERTEX_A, VERTEX_B, WEIGHT_5);
        graph.set(VERTEX_B, VERTEX_C, WEIGHT_10);
        return graph;
    }

    private Edge<String> createEdge(String source, String target, int weight) {
        return new Edge<>(source, target, weight);
    }

    private void assertContainsExpectedElements(String actual, String... expectedElements) {
        for (String element : expectedElements) {
            assertTrue("should contain '" + element + "'", actual.contains(element));
        }
    }
}