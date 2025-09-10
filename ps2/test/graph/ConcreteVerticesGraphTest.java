/* Copyright (c) 2015-2016 MIT 6.005 course staff, all rights reserved.
 * Redistribution of original or derived work requires permission of course staff.
 */
package graph;

import static org.junit.Assert.*;

import java.util.Map;

import org.junit.Test;

/**
 * Tests for ConcreteVerticesGraph.
 * <p>
 * This class runs the GraphInstanceTest tests against ConcreteVerticesGraph, as
 * well as tests for that particular implementation.
 * <p>
 * Tests against the Graph spec should be in GraphInstanceTest.
 */
public class ConcreteVerticesGraphTest extends GraphInstanceTest {

    // Test fixtures for common vertex labels
    private static final String VERTEX_A = "A";
    private static final String VERTEX_B = "B";
    private static final String VERTEX_C = "C";
    private static final String VERTEX_X = "X";

    // Common edge weights
    private static final int WEIGHT_5 = 5;
    private static final int WEIGHT_10 = 10;
    private static final int WEIGHT_15 = 15;

    /*
     * Provide a ConcreteVerticesGraph for tests in GraphInstanceTest.
     */
    @Override public Graph<String> emptyInstance() {
        return new ConcreteVerticesGraph<>();
    }

    /*
     * Testing ConcreteVerticesGraph...
     */

    // Testing strategy for ConcreteVerticesGraph.toString()
    // Input space partitioning:
    // - Graph state: empty, single vertex (no edges), single vertex with self-loop,
    //                multiple isolated vertices, multiple vertices with edges
    // - Edge configurations: no edges, single edge, multiple outgoing edges per vertex,
    //                       complex connectivity patterns
    //
    // Output space partitioning:
    // - Format should clearly show each vertex and its outgoing edges
    // - Should be readable for debugging purposes
    // - Should handle empty vertex lists and vertices with no outgoing edges

    @Test
    public void testToStringEmptyGraph() {
        ConcreteVerticesGraph<String> graph = new ConcreteVerticesGraph<>();
        String result = graph.toString();

        assertContainsExpectedElements(result, "ConcreteVerticesGraph", "{", "}");
    }

    @Test
    public void testToStringSingleVertexNoEdges() {
        ConcreteVerticesGraph<String> graph = new ConcreteVerticesGraph<>();
        graph.add(VERTEX_A);
        String result = graph.toString();

        assertContainsExpectedElements(result, VERTEX_A);
        assertTrue("toString should show empty edge list or vertex format",
                result.contains("[]") || result.contains(VERTEX_A + ":"));
    }

    @Test
    public void testToStringWithEdges() {
        ConcreteVerticesGraph<String> graph = createGraphWithEdges();
        String result = graph.toString();

        assertContainsExpectedElements(result, VERTEX_A, VERTEX_B, VERTEX_C);
        assertTrue("toString should show edge information",
                containsAnyOf(result, "5", "10", "3"));
    }

    @Test
    public void testToStringSelfLoop() {
        ConcreteVerticesGraph<String> graph = new ConcreteVerticesGraph<>();
        graph.set(VERTEX_X, VERTEX_X, 7);

        String result = graph.toString();
        assertContainsExpectedElements(result, VERTEX_X, "7");
    }

    @Test
    public void testToStringMultipleIsolatedVertices() {
        ConcreteVerticesGraph<String> graph = new ConcreteVerticesGraph<>();
        graph.add(VERTEX_A);
        graph.add(VERTEX_B);
        graph.add(VERTEX_C);

        String result = graph.toString();
        assertContainsExpectedElements(result, VERTEX_A, VERTEX_B, VERTEX_C);
    }

    /*
     * Testing Vertex...
     */

    // Testing strategy for Vertex
    //
    // Constructor:
    // - Label: null, empty string, normal string, string with special characters
    //
    // getLabel():
    // - Returns the correct label set in constructor
    //
    // setTarget(target, weight):
    // - Target: null, same as vertex label (self-loop), different label
    // - Weight: negative, zero, positive
    // - Existing edge: none, same weight, different weight
    // - Return value: 0 for new edge, previous weight for updated edge
    //
    // removeTarget(target):
    // - Target: null, non-existent, existing edge
    // - Return value: 0 for non-existent, previous weight for existing
    //
    // hasTarget(target):
    // - Target: null, non-existent, existing
    //
    // getTargetWeight(target):
    // - Target: null, non-existent, existing with various weights
    //
    // getTargets():
    // - Vertex state: no outgoing edges, single edge, multiple edges
    // - Mutation safety: returned map should be defensive copy
    //
    // toString():
    // - Vertex state: no edges, single edge, multiple edges, self-loop
    // - Format should be readable and show vertex label and edges clearly


    // Constructor tests
    @Test
    public void testVertexConstructorValid() {
        Vertex<String> vertex = createVertex("test");
        assertEquals("label should be set correctly", "test", vertex.getLabel());
        assertTrue("new vertex should have no targets", vertex.getTargets().isEmpty());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testVertexConstructorNullLabel() {
        new Vertex<String>(null);
    }

    @Test
    public void testVertexConstructorEmptyLabel() {
        Vertex<String> vertex = createVertex("");
        assertEquals("empty string should be valid label", "", vertex.getLabel());
    }

    @Test
    public void testVertexConstructorSpecialCharacters() {
        String specialLabel = "vertex with spaces & symbols!";
        Vertex<String> vertex = createVertex(specialLabel);
        assertEquals("special characters should be allowed", specialLabel, vertex.getLabel());
    }

    // setTarget tests
    @Test
    public void testVertexSetTargetNew() {
        Vertex<String> vertex = createVertex(VERTEX_A);
        int previousWeight = vertex.setTarget(VERTEX_B, WEIGHT_5);

        assertEquals("should return 0 for new edge", 0, previousWeight);
        assertVertexHasTarget(vertex, VERTEX_B, WEIGHT_5);
        assertEquals("should have one target", 1, vertex.getTargets().size());
    }

    @Test
    public void testVertexSetTargetUpdate() {
        Vertex<String> vertex = createVertexWithTarget(VERTEX_B, WEIGHT_10);
        int previousWeight = vertex.setTarget(VERTEX_B, WEIGHT_15);

        assertEquals("should return previous weight", WEIGHT_10, previousWeight);
        assertVertexHasTarget(vertex, VERTEX_B, WEIGHT_15);
        assertEquals("should still have one target", 1, vertex.getTargets().size());
    }

    @Test
    public void testVertexSetTargetSelfLoop() {
        Vertex<String> vertex = createVertex(VERTEX_A);
        int previousWeight = vertex.setTarget(VERTEX_A, 3);

        assertEquals("should return 0 for new self-loop", 0, previousWeight);
        assertVertexHasTarget(vertex, VERTEX_A, 3);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testVertexSetTargetNullTarget() {
        createVertex(VERTEX_A).setTarget(null, WEIGHT_5);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testVertexSetTargetZeroWeight() {
        createVertex(VERTEX_A).setTarget(VERTEX_B, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testVertexSetTargetNegativeWeight() {
        createVertex(VERTEX_A).setTarget(VERTEX_B, -1);
    }

    // removeTarget tests
    @Test
    public void testVertexRemoveTargetExisting() {
        Vertex<String> vertex = createVertexWithTarget(VERTEX_B, 7);
        int previousWeight = vertex.removeTarget(VERTEX_B);

        assertEquals("should return previous weight", 7, previousWeight);
        assertVertexDoesNotHaveTarget(vertex, VERTEX_B);
        assertEquals("should have no targets", 0, vertex.getTargets().size());
    }

    @Test
    public void testVertexRemoveTargetNonExistent() {
        Vertex<String> vertex = createVertex(VERTEX_A);
        assertEquals("should return 0 for non-existent target", 0, vertex.removeTarget(VERTEX_B));
    }

    @Test
    public void testVertexRemoveTargetNull() {
        Vertex<String> vertex = createVertexWithTarget(VERTEX_B, WEIGHT_5);
        int previousWeight = vertex.removeTarget(null);

        assertEquals("should return 0 for null target", 0, previousWeight);
        assertVertexHasTarget(vertex, VERTEX_B, WEIGHT_5);
    }

    // Query method tests
    @Test
    public void testVertexHasTarget() {
        Vertex<String> vertex = createVertex(VERTEX_A);

        assertVertexDoesNotHaveTarget(vertex, VERTEX_B);
        assertFalse("should handle null target", vertex.hasTarget(null));

        vertex.setTarget(VERTEX_B, WEIGHT_10);
        assertVertexHasTarget(vertex, VERTEX_B, WEIGHT_10);
        assertVertexDoesNotHaveTarget(vertex, VERTEX_C);
    }

    @Test
    public void testVertexGetTargetWeight() {
        Vertex<String> vertex = createVertex(VERTEX_A);

        assertEquals("non-existent target should have weight 0", 0, vertex.getTargetWeight(VERTEX_B));
        assertEquals("null target should have weight 0", 0, vertex.getTargetWeight(null));

        vertex.setTarget(VERTEX_B, WEIGHT_15);
        assertEquals("existing target should have correct weight", WEIGHT_15, vertex.getTargetWeight(VERTEX_B));

        vertex.setTarget(VERTEX_B, 25);
        assertEquals("updated target should have new weight", 25, vertex.getTargetWeight(VERTEX_B));
    }

    // getTargets tests
    @Test
    public void testVertexGetTargetsEmpty() {
        Vertex<String> vertex = createVertex(VERTEX_A);
        assertTrue("new vertex should have empty targets", vertex.getTargets().isEmpty());
    }

    @Test
    public void testVertexGetTargetsMultiple() {
        Vertex<String> vertex = createVertexWithMultipleTargets();
        Map<String, Integer> targets = vertex.getTargets();

        assertEquals("should have correct number of targets", 3, targets.size());
        assertTargetWeightsCorrect(targets, VERTEX_B, WEIGHT_5, VERTEX_C, WEIGHT_10, "D", WEIGHT_15);
    }

    @Test
    public void testVertexGetTargetsDefensiveCopy() {
        Vertex<String> vertex = createVertexWithTarget(VERTEX_B, WEIGHT_5);

        Map<String, Integer> targets1 = vertex.getTargets();
        Map<String, Integer> targets2 = vertex.getTargets();

        // Modify one of the returned maps
        targets1.put(VERTEX_C, WEIGHT_10);

        // Original vertex should be unchanged
        assertVertexDoesNotHaveTarget(vertex, VERTEX_C);
        assertEquals("second call should return clean map", 1, targets2.size());
        assertFalse("second map should not contain external addition", targets2.containsKey(VERTEX_C));
    }

    // toString tests
    @Test
    public void testVertexToStringEmpty() {
        Vertex<String> vertex = createVertex(VERTEX_A);
        String result = vertex.toString();

        assertContainsExpectedElements(result, VERTEX_A, "[]");
    }

    @Test
    public void testVertexToStringSingleTarget() {
        Vertex<String> vertex = createVertexWithTarget(VERTEX_B, WEIGHT_5);
        String result = vertex.toString();

        assertContainsExpectedElements(result, VERTEX_A, VERTEX_B, "5");
    }

    @Test
    public void testVertexToStringMultipleTargets() {
        Vertex<String> vertex = createVertex(VERTEX_A);
        vertex.setTarget(VERTEX_B, WEIGHT_5);
        vertex.setTarget(VERTEX_C, WEIGHT_10);
        String result = vertex.toString();

        assertContainsExpectedElements(result, VERTEX_A, VERTEX_B, VERTEX_C, "5", "10");
    }

    @Test
    public void testVertexToStringSelfLoop() {
        Vertex<String> vertex = createVertexWithTarget(VERTEX_A, 3);
        String result = vertex.toString();

        assertContainsExpectedElements(result, VERTEX_A, "3");
    }

    // Behavioral tests
    @Test
    public void testVertexMutability() {
        Vertex<String> vertex = createVertex(VERTEX_A);

        // Test progression through different states
        assertEquals("should start with no targets", 0, vertex.getTargets().size());

        vertex.setTarget(VERTEX_B, WEIGHT_5);
        assertEquals("should have one target after addition", 1, vertex.getTargets().size());

        vertex.setTarget(VERTEX_B, WEIGHT_10);
        assertVertexHasTarget(vertex, VERTEX_B, WEIGHT_10);
        assertEquals("should still have one target after update", 1, vertex.getTargets().size());

        vertex.setTarget(VERTEX_C, WEIGHT_15);
        assertEquals("should have two targets", 2, vertex.getTargets().size());

        vertex.removeTarget(VERTEX_B);
        assertEquals("should have one target after removal", 1, vertex.getTargets().size());
        assertVertexHasTarget(vertex, VERTEX_C, WEIGHT_15);
        assertVertexDoesNotHaveTarget(vertex, VERTEX_B);
    }

    @Test
    public void testVertexLabelImmutability() {
        String originalLabel = "original";
        Vertex<String> vertex = createVertex(originalLabel);

        // Modify internal state
        vertex.setTarget(VERTEX_B, WEIGHT_5);
        vertex.setTarget(VERTEX_C, WEIGHT_10);

        // Label should remain unchanged
        assertEquals("label should be immutable", originalLabel, vertex.getLabel());
    }

    @Test
    public void testVertexWithLargeWeight() {
        Vertex<String> vertex = createVertex(VERTEX_A);
        vertex.setTarget(VERTEX_B, Integer.MAX_VALUE);
        assertEquals("should handle large weights", Integer.MAX_VALUE, vertex.getTargetWeight(VERTEX_B));
    }

    @Test
    public void testVertexComplexOperations() {
        Vertex<String> vertex = createVertex("hub");

        // Build complex adjacency structure
        vertex.setTarget(VERTEX_A, 1);
        vertex.setTarget(VERTEX_B, 2);
        vertex.setTarget(VERTEX_C, 3);
        vertex.setTarget("hub", 4); // self-loop

        assertEquals("should have 4 targets", 4, vertex.getTargets().size());

        // Update weights
        vertex.setTarget(VERTEX_A, WEIGHT_10);
        vertex.setTarget(VERTEX_B, 20);

        // Verify state
        assertEquals("should still have 4 targets", 4, vertex.getTargets().size());
        assertVertexHasTarget(vertex, VERTEX_A, WEIGHT_10);
        assertVertexHasTarget(vertex, VERTEX_B, 20);
        assertVertexHasTarget(vertex, VERTEX_C, 3);
        assertVertexHasTarget(vertex, "hub", 4);

        // Remove targets
        vertex.removeTarget(VERTEX_B);
        vertex.removeTarget("hub");

        assertEquals("should have 2 targets after removals", 2, vertex.getTargets().size());
        assertVertexHasTarget(vertex, VERTEX_A, WEIGHT_10);
        assertVertexHasTarget(vertex, VERTEX_C, 3);
        assertVertexDoesNotHaveTarget(vertex, VERTEX_B);
        assertVertexDoesNotHaveTarget(vertex, "hub");
    }

    // Helper methods to reduce duplication and improve readability

    private ConcreteVerticesGraph<String> createGraphWithEdges() {
        ConcreteVerticesGraph<String> graph = new ConcreteVerticesGraph<>();
        graph.set(VERTEX_A, VERTEX_B, WEIGHT_5);
        graph.set(VERTEX_A, VERTEX_C, WEIGHT_10);
        graph.set(VERTEX_B, VERTEX_C, 3);
        return graph;
    }

    private Vertex<String> createVertex(String label) {
        return new Vertex<>(label);
    }

    private Vertex<String> createVertexWithTarget(String targetLabel, int weight) {
        Vertex<String> vertex = createVertex(ConcreteVerticesGraphTest.VERTEX_A);
        vertex.setTarget(targetLabel, weight);
        return vertex;
    }

    private Vertex<String> createVertexWithMultipleTargets() {
        Vertex<String> vertex = createVertex(VERTEX_A);
        vertex.setTarget(VERTEX_B, WEIGHT_5);
        vertex.setTarget(VERTEX_C, WEIGHT_10);
        vertex.setTarget("D", WEIGHT_15);
        return vertex;
    }

    private void assertVertexHasTarget(Vertex<String> vertex, String target, int expectedWeight) {
        assertTrue("should have target " + target, vertex.hasTarget(target));
        assertEquals("should have correct weight for " + target, expectedWeight, vertex.getTargetWeight(target));
    }

    private void assertVertexDoesNotHaveTarget(Vertex<String> vertex, String target) {
        assertFalse("should not have target " + target, vertex.hasTarget(target));
    }

    private void assertContainsExpectedElements(String actual, String... expectedElements) {
        for (String element : expectedElements) {
            assertTrue("should contain '" + element + "'", actual.contains(element));
        }
    }

    private boolean containsAnyOf(String text, String... elements) {
        for (String element : elements) {
            if (text.contains(element)) {
                return true;
            }
        }
        return false;
    }

    private void assertTargetWeightsCorrect(Map<String, Integer> targets, Object... targetWeightPairs) {
        for (int i = 0; i < targetWeightPairs.length; i += 2) {
            String target = (String) targetWeightPairs[i];
            Integer expectedWeight = (Integer) targetWeightPairs[i + 1];
            assertEquals("should have correct weight for " + target, expectedWeight, targets.get(target));
        }
    }
}