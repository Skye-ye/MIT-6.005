/* Copyright (c) 2015-2016 MIT 6.005 course staff, all rights reserved.
 * Redistribution of original or derived work requires permission of course staff.
 */
package poet;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import graph.Graph;

/**
 * A graph-based poetry generator.
 * 
 * <p>GraphPoet is initialized with a corpus of text, which it uses to derive a
 * word affinity graph.
 * Vertices in the graph are words. Words are defined as non-empty
 * case-insensitive strings of non-space non-newline characters. They are
 * delimited in the corpus by spaces, newlines, or the ends of the file.
 * Edges in the graph count adjacency: the number of times "w1" is followed by
 * "w2" in the corpus is the weight of the edge from w1 to w2.
 * 
 * <p>For example, given this corpus:
 * <pre>    Hello, HELLO, hello, goodbye!    </pre>
 * <p>the graph would contain two edges:
 * <ul><li> ("hello,") -> ("hello,")   with weight 2
 *     <li> ("hello,") -> ("goodbye!") with weight 1 </ul>
 * <p>where the vertices represent case-insensitive {@code "hello,"} and
 * {@code "goodbye!"}.
 * 
 * <p>Given an input string, GraphPoet generates a poem by attempting to
 * insert a bridge word between every adjacent pair of words in the input.
 * The bridge word between input words "w1" and "w2" will be some "b" such that
 * w1 -> b -> w2 is a two-edge-long path with maximum-weight weight among all
 * the two-edge-long paths from w1 to w2 in the affinity graph.
 * If there are no such paths, no bridge word is inserted.
 * In the output poem, input words retain their original case, while bridge
 * words are lower case. The whitespace between every word in the poem is a
 * single space.
 * 
 * <p>For example, given this corpus:
 * <pre>    This is a test of the Mugar Omni Theater sound system.    </pre>
 * <p>on this input:
 * <pre>    Test the system.    </pre>
 * <p>the output poem would be:
 * <pre>    Test of the system.    </pre>
 * 
 * <p>PS2 instructions: this is a required ADT class, and you MUST NOT weaken
 * the required specifications. However, you MAY strengthen the specifications
 * and you MAY add additional methods.
 * You MUST use Graph in your rep, but otherwise the implementation of this
 * class is up to you.
 */
public class GraphPoet {
    
    private final Graph<String> graph = Graph.empty();

    // Abstraction function:
    //   AF(graph) = a poetry generator where graph represents word affinity
    //   - vertices are lowercase words from the corpus
    //   - edge weights represent how many times one word follows another in the corpus
    //   - the poet can generate poems by finding bridge words in two-hop paths

    // Representation invariant:
    //   graph != null
    //   all vertices in graph are lowercase, non-empty strings containing no spaces or newlines
    //   all edge weights are positive (representing at least one occurrence)

    // Safety from rep exposure:
    //   graph is private final and clients cannot access it directly
    //   poem() returns a new String, which is immutable
    //   no methods return references to the internal graph
    
    /**
     * Create a new poet with the graph from corpus (as described above).
     * 
     * @param corpus text file from which to derive the poet's affinity graph
     * @throws IOException if the corpus file cannot be found or read
     */
    public GraphPoet(File corpus) throws IOException {
        StringBuilder text = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(corpus))) {
            String line;
            while ((line = reader.readLine()) != null) {
                text.append(line).append(" ");
            }
        }
        buildGraphFromText(text.toString());
        checkRep();
    }

    /**
     * Create a new poet with the graph from corpus (as described above).
     *
     * @param corpus text from which to derive the poet's affinity graph
     */
    public GraphPoet(String corpus) {
        buildGraphFromText(corpus);
        checkRep();
    }

    /**
     * Check that the representation invariant holds.
     */
    private void checkRep() {
        // Verify all vertices are lowercase and properly formatted
        for (String vertex : graph.vertices()) {
            assert vertex != null;
            assert !vertex.isEmpty() : "vertex cannot be empty";
            assert vertex.equals(vertex.toLowerCase()) : "vertex must be lowercase";
            assert !vertex.contains(" ") : "vertex cannot contain spaces";
            assert !vertex.contains("\n") : "vertex cannot contain newlines";
            assert !vertex.contains("\r") : "vertex cannot contain carriage returns";
        }
    }

    /**
     * Build the affinity graph from corpus text.
     * @param corpusText the text to process
     */
    private void buildGraphFromText(String corpusText) {
        List<String> words = extractWords(corpusText);

        // Add all words as vertices
        for (String word : words) {
            graph.add(word.toLowerCase());
        }

        // Add edges for adjacent word pairs
        for (int i = 0; i < words.size() - 1; i++) {
            String current = words.get(i).toLowerCase();
            String next = words.get(i + 1).toLowerCase();

            // Get current edge weight and increment by 1
            int currentWeight = graph.set(current, next, 1);
            if (currentWeight > 0) {
                // Edge already existed, increment its weight
                graph.set(current, next, currentWeight + 1);
            }
        }
    }

    /**
     * Extract words from text, where words are non-empty sequences of
     * non-space, non-newline characters.
     * @param text the text to process
     * @return list of words in order
     */
    private List<String> extractWords(String text) {
        List<String> words = new ArrayList<>();

        // Split on whitespace (spaces, tabs, newlines)
        String[] tokens = text.split("\\s+");

        for (String token : tokens) {
            if (!token.isEmpty()) {
                words.add(token);
            }
        }

        return words;
    }

    /**
     * Generate a poem.
     *
     * @param input string from which to create the poem
     * @return poem (as described above)
     */
    public String poem(String input) {
        if (input.trim().isEmpty()) {
            return "";
        }

        List<String> inputWords = extractWords(input);
        if (inputWords.size() <= 1) {
            return input.trim();
        }

        List<String> poemWords = new ArrayList<>();
        poemWords.add(inputWords.getFirst());

        // For each adjacent pair, try to find a bridge word
        for (int i = 0; i < inputWords.size() - 1; i++) {
            String current = inputWords.get(i);
            String next = inputWords.get(i + 1);

            String bridge = findBestBridge(current.toLowerCase(), next.toLowerCase());
            if (bridge != null) {
                poemWords.add(bridge);
            }
            poemWords.add(next);
        }

        return String.join(" ", poemWords);
    }

    /**
     * Find the best bridge word between two words.
     * A bridge word b creates a path w1 -> b -> w2, and we want the path
     * with maximum total weight.
     *
     * @param word1 first word (lowercase)
     * @param word2 second word (lowercase)
     * @return the best bridge word (lowercase), or null if none exists
     */
    private String findBestBridge(String word1, String word2) {
        Map<String, Integer> targetsFromWord1 = graph.targets(word1);
        Map<String, Integer> sourcesToWord2 = graph.sources(word2);

        String bestBridge = null;
        int maxWeight = 0;

        // Find common vertices that can serve as bridges
        for (String candidate : targetsFromWord1.keySet()) {
            if (sourcesToWord2.containsKey(candidate)) {
                // Calculate total path weight: w1->candidate + candidate->w2
                int pathWeight = targetsFromWord1.get(candidate) + sourcesToWord2.get(candidate);

                if (pathWeight > maxWeight) {
                    maxWeight = pathWeight;
                    bestBridge = candidate;
                }
            }
        }

        return bestBridge;
    }

    /**
     * String representation of this GraphPoet.
     * @return a string representation showing the underlying graph structure
     */
    @Override
    public String toString() {
        return "GraphPoet with " + graph.vertices().size() + " words";
    }

}
