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
public class ConcreteVerticesGraph<L> implements Graph<L> {
    
    private final List<Vertex<L>> vertices = new ArrayList<>();

    // Abstraction function:
    //   AF(vertices) = a directed graph where:
    //     - vertex labels are the labels of Vertex objects in vertices list
    //     - edges are the outgoing edges stored in each Vertex object
    //     - each edge has a source (the vertex it's stored in), target, and positive weight

    // Representation invariant:
    //   vertices != null
    //   all elements in vertices are non-null
    //   no two vertices have the same label (vertex labels are unique)
    //   for all vertices v: v satisfies its own rep invariant
    //   for all edges in any vertex: the target vertex label exists in some vertex in vertices

    // Safety from rep exposure:
    //   vertices is private final field.
    //   vertices() returns a new HashSet (defensive copy)
    //   sources() and targets() return new HashMap instances
    //   clients never get direct references to internal Vertex objects.
    //   Vertex objects are mutable but only accessible through this class's methods

    /**
     * Create an empty graph.
     */
    public ConcreteVerticesGraph() {
        checkRep();
    }

    /**
     * Check that the representation invariant holds.
     */
    private void checkRep() {
        Set<L> allLabels = new HashSet<>();
        for (Vertex<L> vertex : vertices) {
            assert vertex != null;

            L label = vertex.getLabel();
            assert !allLabels.contains(label) : "Duplicate vertex label: " + label;
            allLabels.add(label);
        }

        for (Vertex<L> vertex : vertices) {
            for (L target : vertex.getTargets().keySet()) {
                assert allLabels.contains(target) : "Edge target " + target + " does not exist as vertex";
            }
        }
    }

    /**
     * Find vertex with given label.
     * @param label the vertex label to find
     * @return the Vertex object with that label, or null if not found
     */
    private Vertex<L> findVertex(L label) {
        for (Vertex<L> vertex : vertices) {
            if (vertex.getLabel().equals(label)) {
                return vertex;
            }
        }
        return null;
    }

    @Override public boolean add(L vertex) {
        if (vertex == null) {
            throw new IllegalArgumentException("Vertex cannot be null");
        }

        if (findVertex(vertex) != null) {
            return false; // already exists
        }

        vertices.add(new Vertex<>(vertex));
        checkRep();
        return true;
    }

    @Override public int set(L source, L target, int weight) {
        if (source == null || target == null) {
            throw new IllegalArgumentException("Source and target cannot be null");
        }
        if (weight < 0) {
            throw new IllegalArgumentException("Weight cannot be negative");
        }

        // Add vertices if they don't exist
        if (findVertex(source) == null) {
            add(source);
        }
        if (findVertex(target) == null) {
            add(target);
        }

        Vertex<L> sourceVertex = findVertex(source);
        int previousWeight;

        assert sourceVertex != null;
        if (weight == 0) {
            // Remove edge
            previousWeight = sourceVertex.removeTarget(target);
        } else {
            // Add or update edge
            previousWeight = sourceVertex.setTarget(target, weight);
        }

        checkRep();
        return previousWeight;
    }

    @Override public boolean remove(L vertex) {
        if (vertex == null) {
            return false;
        }

        Vertex<L> toRemove = findVertex(vertex);
        if (toRemove == null) {
            return false;
        }

        // Remove the vertex from the list
        vertices.remove(toRemove);

        // Remove all edges pointing to this vertex from other vertices
        for (Vertex<L> v : vertices) {
            v.removeTarget(vertex);
        }

        checkRep();
        return true;
    }

    @Override public Set<L> vertices() {
        Set<L> result = new HashSet<>();
        for (Vertex<L> vertex : vertices) {
            result.add(vertex.getLabel());
        }
        return result;
    }

    @Override public Map<L, Integer> sources(L target) {
        Map<L, Integer> result = new HashMap<>();

        if (target == null) {
            return result;
        }

        for (Vertex<L> vertex : vertices) {
            if (vertex.hasTarget(target)) {
                result.put(vertex.getLabel(), vertex.getTargetWeight(target));
            }
        }

        return result;
    }

    @Override public Map<L, Integer> targets(L source) {
        if (source == null) {
            return new HashMap<>();
        }

        Vertex<L> sourceVertex = findVertex(source);
        if (sourceVertex == null) {
            return new HashMap<>();
        }

        return new HashMap<>(sourceVertex.getTargets());
    }

    /**
     * String representation of this graph.
     * @return a string representation showing vertices and their outgoing edges
     */
    @Override public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ConcreteVerticesGraph{\n");
        for (Vertex<L> vertex : vertices) {
            sb.append("  ").append(vertex).append("\n");
        }
        sb.append("}");
        return sb.toString();
    }

}

/**
 * Represents a vertex in a directed graph with outgoing edges.
 * Mutable.
 * This class is internal to the rep of ConcreteVerticesGraph.
 * 
 * <p>PS2 instructions: the specification and implementation of this class is
 * up to you.
 */
class Vertex<L> {

    private final L label;
    private final Map<L, Integer> targets;

    // Abstraction function:
    //   AF(label, targets) = a vertex labeled 'label' with outgoing edges
    //   to each vertex in targets.keySet(), where each edge has weight targets.get(target)

    // Representation invariant:
    //   label != null
    //   targets != null
    //   all keys in targets are non-null
    //   all values in targets are positive (> 0)
    //   no key in targets equals label (no self-loops stored redundantly)

    // Safety from rep exposure:
    //   label is private final and of type L which must be immutable (per Graph spec)
    //   targets is private final Map, but the map itself is mutable
    //   getTargets() returns defensive copy (new HashMap)
    //   clients cannot directly access or modify the internal targets map

    /**
     * Create a new vertex with given label and no outgoing edges.
     * @param label the label for this vertex, must be non-null
     */
    public Vertex(L label) {
        if (label == null) {
            throw new IllegalArgumentException("Vertex label cannot be null");
        }
        this.label = label;
        this.targets = new HashMap<>();
        checkRep();
    }

    /**
     * Check that the representation invariant holds.
     */
    private void checkRep() {
        assert label != null;
        assert targets != null;

        for (Map.Entry<L, Integer> entry : targets.entrySet()) {
            assert entry.getKey() != null : "Target label cannot be null";
            assert entry.getValue() != null : "Target weight cannot be null";
            assert entry.getValue() > 0 : "Target weight must be positive";
        }
    }

    /**
     * Get the label of this vertex.
     * @return the vertex label
     */
    public L getLabel() {
        return label;
    }

    /**
     * Get all targets of outgoing edges from this vertex.
     * @return a map from target vertex labels to edge weights (defensive copy)
     */
    public Map<L, Integer> getTargets() {
        return new HashMap<>(targets);
    }

    /**
     * Set the weight of the edge from this vertex to target.
     * If an edge already exists, updates its weight.
     * @param target the target vertex label
     * @param weight the positive weight for the edge
     * @return the previous weight of the edge, or 0 if no edge existed
     */
    public int setTarget(L target, int weight) {
        if (target == null) {
            throw new IllegalArgumentException("Target cannot be null");
        }
        if (weight <= 0) {
            throw new IllegalArgumentException("Weight must be positive");
        }

        Integer previousWeight = targets.put(target, weight);
        checkRep();
        return previousWeight == null ? 0 : previousWeight;
    }

    /**
     * Remove the edge from this vertex to target.
     * @param target the target vertex label
     * @return the previous weight of the edge, or 0 if no edge existed
     */
    public int removeTarget(L target) {
        if (target == null) {
            return 0;
        }

        Integer previousWeight = targets.remove(target);
        checkRep();
        return previousWeight == null ? 0 : previousWeight;
    }

    /**
     * Check if this vertex has an outgoing edge to target.
     * @param target the target vertex label to check
     * @return true if there is an edge from this vertex to target
     */
    public boolean hasTarget(L target) {
        return target != null && targets.containsKey(target);
    }

    /**
     * Get the weight of the edge from this vertex to target.
     * @param target the target vertex label
     * @return the weight of the edge, or 0 if no edge exists
     */
    public int getTargetWeight(L target) {
        if (target == null) {
            return 0;
        }
        Integer weight = targets.get(target);
        return weight == null ? 0 : weight;
    }

    /**
     * String representation of this vertex.
     * @return a string showing the vertex label and its outgoing edges
     */
    @Override public String toString() {
        if (targets.isEmpty()) {
            return label + ": []";
        }

        StringBuilder sb = new StringBuilder();
        sb.append(label).append(": [");
        boolean first = true;
        for (Map.Entry<L, Integer> entry : targets.entrySet()) {
            if (!first) {
                sb.append(", ");
            }
            sb.append(entry.getKey()).append("(").append(entry.getValue()).append(")");
            first = false;
        }
        sb.append("]");
        return sb.toString();
    }
}
