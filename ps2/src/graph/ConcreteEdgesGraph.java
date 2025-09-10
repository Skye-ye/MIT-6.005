/* Copyright (c) 2015-2016 MIT 6.005 course staff, all rights reserved.
 * Redistribution of original or derived work requires permission of course staff.
 */
package graph;

import java.util.*;

/**
 * An implementation of Graph.
 * 
 * <p>PS2 instructions: you MUST use the provided rep.
 */
public class ConcreteEdgesGraph<L> implements Graph<L> {
    
    private final Set<L> vertices = new HashSet<>();
    private final List<Edge<L>> edges = new ArrayList<>();

    // Abstraction function:
    //   AF(vertices, edges) = a directed graph where:
    //     - vertices are the vertex labels in the vertices set
    //     - edges are the directed weighted edges in the edges list
    //     - each edge connects two vertices with a positive weight

    // Representation invariant:
    //   vertices != null
    //   edges != null
    //   all elements in vertices are non-null
    //   all elements in edges are non-null
    //   for all edges e: e.getSource() and e.getTarget() are in vertices
    //   no two edges have the same source and target (at most one edge between any pair)

    // Safety from rep exposure:
    //   vertices and edges are private final fields
    //   vertices() returns a defensive copy (new HashSet)
    //   sources() and targets() return new HashMap instances
    //   Edge objects are immutable
    //   clients never get direct references to the internal collections

    /**
     * Create an empty graph.
     */
    public ConcreteEdgesGraph() {
        checkRep();
    }

    /**
     * Check that the representation invariant holds.
     */
    private void checkRep() {

        // Check all vertices are non-null
        for (L vertex : vertices) {
            assert vertex != null;
        }

        // Check all edges are non-null and their vertices exist
        for (Edge<L> edge : edges) {
            assert edge != null;
            assert vertices.contains(edge.getSource());
            assert vertices.contains(edge.getTarget());
        }

        // Check no duplicate edges (same source and target)
        Set<String> seenEdges = new HashSet<>();
        for (Edge<L> edge : edges) {
            String edgeKey = edge.getSource() + "->" + edge.getTarget();
            assert !seenEdges.contains(edgeKey) : "Duplicate edge found";
            seenEdges.add(edgeKey);
        }
    }

    @Override public boolean add(L vertex) {
        if (vertex == null) {
            throw new IllegalArgumentException("Vertex cannot be null");
        }

        boolean added = vertices.add(vertex);
        checkRep();
        return added;
    }

    @Override public int set(L source, L target, int weight) {
        if (source == null || target == null) {
            throw new IllegalArgumentException("Source and target cannot be null");
        }
        if (weight < 0) {
            throw new IllegalArgumentException("Weight cannot be negative");
        }

        // Add vertices if they don't exist
        vertices.add(source);
        vertices.add(target);

        // Find existing edge with same source and target
        int previousWeight = 0;
        Iterator<Edge<L>> iterator = edges.iterator();
        while (iterator.hasNext()) {
            Edge<L> edge = iterator.next();
            if (edge.connects(source, target)) {
                previousWeight = edge.getWeight();
                iterator.remove();
                break;
            }
        }

        // Add new edge if weight > 0
        if (weight > 0) {
            edges.add(new Edge<>(source, target, weight));
        }

        checkRep();
        return previousWeight;
    }

    @Override public boolean remove(L vertex) {
        if (vertex == null) {
            return false;
        }

        if (!vertices.contains(vertex)) {
            return false;
        }

        // Remove the vertex
        vertices.remove(vertex);

        // Remove all edges involving this vertex
        edges.removeIf(edge -> edge.involves(vertex));

        checkRep();
        return true;
    }

    @Override public Set<L> vertices() {
        return new HashSet<>(vertices);
    }

    @Override public Map<L, Integer> sources(L target) {
        Map<L, Integer> result = new HashMap<>();

        if (target == null) {
            return result;
        }

        for (Edge<L> edge : edges) {
            if (edge.hasTarget(target)) {
                result.put(edge.getSource(), edge.getWeight());
            }
        }

        return result;
    }

    @Override public Map<L, Integer> targets(L source) {
        Map<L, Integer> result = new HashMap<>();

        if (source == null) {
            return result;
        }

        for (Edge<L> edge : edges) {
            if (edge.hasSource(source)) {
                result.put(edge.getTarget(), edge.getWeight());
            }
        }

        return result;
    }

    /**
     * String representation of this graph.
     * @return a string representation showing vertices and edges
     */
    @Override public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ConcreteEdgesGraph{\n");
        sb.append("  vertices: ").append(vertices).append("\n");
        sb.append("  edges: [\n");
        for (Edge<L> edge : edges) {
            sb.append("    ").append(edge).append("\n");
        }
        sb.append("  ]\n");
        sb.append("}");
        return sb.toString();
    }

}

/**
 * Represents a weighted directed edge in a graph.
 * Immutable.
 * This class is internal to the rep of ConcreteEdgesGraph.
 * 
 * <p>
 * PS2 instructions: the specification and implementation of this class is
 * up to you.
 */
class Edge<L> {

    private final L source;
    private final L target;
    private final int weight;

    // Abstraction function:
    //   AF(source, target, weight) = a directed edge from vertex source
    //   to vertex target with non-negative weight
    // Representation invariant:
    //   source != null
    //   target != null
    //   weight > 0
    // Safety from rep exposure:
    //   All fields are private and final
    //   source and target are Strings, which are immutable
    //   weight is primitive int, so immutable
    //   All methods return immutable values

    /**
     * Create a new directed edge.
     * @param source the source vertex label
     * @param target the target vertex label
     * @param weight the positive weight of this edge
     * @throws IllegalArgumentException if weight <= 0 or source/target is null
     */
    public Edge(L source, L target, int weight) {
        if (source == null || target == null) {
            throw new IllegalArgumentException("Source and target cannot be null");
        }
        if (weight <= 0) {
            throw new IllegalArgumentException("Edge weight must be positive");
        }

        this.source = source;
        this.target = target;
        this.weight = weight;
        checkRep();
    }

    /**
     * Check that the representation invariant holds.
     */
    private void checkRep() {
        assert source != null : "source must be non-null";
        assert target != null : "target must be non-null";
        assert weight > 0 : "weight must be positive";
    }

    /**
     * Get the source vertex of this edge.
     * @return the source vertex label
     */
    public L getSource() {
        return source;
    }

    /**
     * Get the target vertex of this edge.
     * @return the target vertex label
     */
    public L getTarget() {
        return target;
    }

    /**
     * Get the weight of this edge.
     * @return the positive weight of this edge
     */
    public int getWeight() {
        return weight;
    }

    /**
     * Check if this edge connects the specified source and target vertices.
     * @param source the source vertex to check
     * @param target the target vertex to check
     * @return true if this edge connects the specified vertices
     */
    public boolean connects(L source, L target) {
        return this.source.equals(source) && this.target.equals(target);
    }

    /**
     * Check if this edge involves the specified vertex as either source or target.
     * @param vertex the vertex to check
     * @return true if this edge has vertex as source or target
     */
    public boolean involves(L vertex) {
        return source.equals(vertex) || target.equals(vertex);
    }

    /**
     * Check if this edge has the specified vertex as its source.
     * @param vertex the vertex to check
     * @return true if this edge has vertex as its source
     */
    public boolean hasSource(L vertex) {
        return source.equals(vertex);
    }

    /**
     * Check if this edge has the specified vertex as its target.
     * @param vertex the vertex to check
     * @return true if this edge has vertex as its target
     */
    public boolean hasTarget(L vertex) {
        return target.equals(vertex);
    }

    /**
     * Two edges are equal if they have the same source, target, and weight.
     * @param obj the object to compare with
     * @return true if obj is an Edge with same source, target, and weight
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Edge<?> other)) {
            return false;
        }
        return source.equals(other.source)
                && target.equals(other.target)
                && weight == other.weight;
    }

    /**
     * Hash code consistent with equals.
     * @return hash code for this edge
     */
    @Override
    public int hashCode() {
        return source.hashCode() + target.hashCode() + weight;
    }

    /**
     * String representation of this edge.
     * @return a string representation showing source -> target (weight)
     */
    @Override
    public String toString() {
        return source + " -> " + target + " (" + weight + ")";
    }
}
