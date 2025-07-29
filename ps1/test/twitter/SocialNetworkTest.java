/* Copyright (c) 2007-2016 MIT 6.005 course staff, all rights reserved.
 * Redistribution of original or derived work requires permission of course staff.
 */
package twitter;

import static org.junit.Assert.*;

import java.time.Instant;
import java.util.*;

import org.junit.Test;

public class SocialNetworkTest {

    /*
     * Testing strategy for SocialNetwork.guessFollowsGraph():
     *
     * Input space partitioning:
     * - tweets list: empty, single tweet, multiple tweets
     * - Tweet content:
     *   - No @-mentions
     *   - Single @-mention
     *   - Multiple @-mentions in one tweet
     *   - Multiple @-mentions across tweets
     * - Author relationships:
     *   - Authors who don't mention anyone
     *   - Authors who mention others
     *   - Authors who are mentioned by others
     *   - Mutual mentions (A mentions B, B mentions A)
     *   - Self-mentions (author mentions themselves)
     * - Case sensitivity: usernames in different cases
     * - Username validity: proper Twitter usernames vs invalid mentions
     *
     * Output space partitioning:
     * - Empty map (no tweets or no valid mentions)
     * - Map with users who don't follow anyone (empty sets)
     * - Map with users who follow others (non-empty sets)
     * - All usernames present as keys (authors and mentioned users)
     *
     * Evidence rules to test:
     * - @-mention creates follow relationship (A mentions B → A follows B)
     * - All authors and mentioned users appear as keys in map
     * - Users with no follows have empty sets as values
     * - Case insensitive username handling
     * - Invalid mentions don't create relationships
     * - Self-mentions don't create self-follows (implementation choice)
     *
     *
     * Testing strategy for SocialNetwork.influencers():
     *
     * Input space partitioning:
     * - followsGraph map: empty, single user, multiple users
     * - Follower counts:
     *   - All users have 0 followers
     *   - Users with different follower counts
     *   - Users with same follower count (ties)
     *   - Single user with most followers
     *   - Multiple users tied for most followers
     * - Network structures:
     *   - No follow relationships (isolated users)
     *   - Linear chain (A→B→C)
     *   - Star pattern (everyone follows one person)
     *   - Complex network with various relationships
     *
     * Output space partitioning:
     * - Empty list (empty input)
     * - Single user
     * - Multiple users in descending order of followers
     * - Tied users (same follower count) - order within ties unspecified
     *
     * Key behaviors to test:
     * - Descending order by follower count
     * - All users from input graph included exactly once
     * - Consistent ordering (stable sort behavior)
     * - Handling of zero followers
     * - Tie-breaking behavior (unspecified, so should be consistent)
     */

    private static final Instant d1 = Instant.parse("2016-02-17T10:00:00Z");
    private static final Instant d2 = Instant.parse("2016-02-17T11:00:00Z");
    private static final Instant d3 = Instant.parse("2016-02-17T12:00:00Z");
    private static final Instant d4 = Instant.parse("2016-02-17T13:00:00Z");
    
    @Test(expected=AssertionError.class)
    public void testAssertionsEnabled() {
        assert false; // make sure assertions are enabled with VM argument: -ea
    }

    @Test
    public void testGuessFollowsGraphEmpty() {
        // Tests empty tweet list
        Map<String, Set<String>> followsGraph = SocialNetwork.guessFollowsGraph(List.of());

        assertTrue("expected empty graph", followsGraph.isEmpty());
    }

    @Test
    public void testGuessFollowsGraphNoMentions() {
        // Tests tweets with no @-mentions
        Tweet tweet1 = new Tweet(1, "alice", "Hello world", d1);
        Tweet tweet2 = new Tweet(2, "bob", "Good morning", d2);

        Map<String, Set<String>> followsGraph = SocialNetwork.guessFollowsGraph(Arrays.asList(tweet1, tweet2));

        assertEquals("expected two users in graph", 2, followsGraph.size());
        assertTrue("expected alice in graph", followsGraph.containsKey("alice"));
        assertTrue("expected bob in graph", followsGraph.containsKey("bob"));
        assertTrue("expected alice follows nobody", followsGraph.get("alice").isEmpty());
        assertTrue("expected bob follows nobody", followsGraph.get("bob").isEmpty());
    }

    @Test
    public void testGuessFollowsGraphSingleMention() {
        // Tests single @-mention creating follow relationship
        Tweet tweet = new Tweet(1, "alice", "Hello @bob how are you?", d1);

        Map<String, Set<String>> followsGraph = SocialNetwork.guessFollowsGraph(List.of(tweet));

        assertEquals("expected two users in graph", 2, followsGraph.size());
        assertTrue("expected alice in graph", followsGraph.containsKey("alice"));
        assertTrue("expected bob in graph", followsGraph.containsKey("bob"));

        Set<String> aliceFollows = followsGraph.get("alice");
        assertEquals("expected alice follows one person", 1, aliceFollows.size());
        assertTrue("expected alice follows bob", aliceFollows.contains("bob"));

        assertTrue("expected bob follows nobody", followsGraph.get("bob").isEmpty());
    }

    @Test
    public void testGuessFollowsGraphMultipleMentionsOneTweet() {
        // Tests multiple @-mentions in single tweet
        Tweet tweet = new Tweet(1, "alice", "Hello @bob and @charlie, meet @dave!", d1);

        Map<String, Set<String>> followsGraph = SocialNetwork.guessFollowsGraph(List.of(tweet));

        assertEquals("expected four users in graph", 4, followsGraph.size());

        Set<String> aliceFollows = followsGraph.get("alice");
        assertEquals("expected alice follows three people", 3, aliceFollows.size());
        assertTrue("expected alice follows bob", aliceFollows.contains("bob"));
        assertTrue("expected alice follows charlie", aliceFollows.contains("charlie"));
        assertTrue("expected alice follows dave", aliceFollows.contains("dave"));

        assertTrue("expected bob follows nobody", followsGraph.get("bob").isEmpty());
        assertTrue("expected charlie follows nobody", followsGraph.get("charlie").isEmpty());
        assertTrue("expected dave follows nobody", followsGraph.get("dave").isEmpty());
    }

    @Test
    public void testGuessFollowsGraphMultipleTweets() {
        // Tests mentions across multiple tweets
        Tweet tweet1 = new Tweet(1, "alice", "Hello @bob", d1);
        Tweet tweet2 = new Tweet(2, "charlie", "Hi @dave and @alice", d2);
        Tweet tweet3 = new Tweet(3, "bob", "Good morning @charlie", d3);

        Map<String, Set<String>> followsGraph = SocialNetwork.guessFollowsGraph(Arrays.asList(tweet1, tweet2, tweet3));

        assertEquals("expected four users in graph", 4, followsGraph.size());

        // Check alice's follows
        Set<String> aliceFollows = followsGraph.get("alice");
        assertEquals("expected alice follows one person", 1, aliceFollows.size());
        assertTrue("expected alice follows bob", aliceFollows.contains("bob"));

        // Check charlie's follows
        Set<String> charlieFollows = followsGraph.get("charlie");
        assertEquals("expected charlie follows two people", 2, charlieFollows.size());
        assertTrue("expected charlie follows dave", charlieFollows.contains("dave"));
        assertTrue("expected charlie follows alice", charlieFollows.contains("alice"));

        // Check bob's follows
        Set<String> bobFollows = followsGraph.get("bob");
        assertEquals("expected bob follows one person", 1, bobFollows.size());
        assertTrue("expected bob follows charlie", bobFollows.contains("charlie"));

        // Check dave's follows
        assertTrue("expected dave follows nobody", followsGraph.get("dave").isEmpty());
    }

    @Test
    public void testGuessFollowsGraphCaseInsensitive() {
        // Tests case-insensitive handling of usernames
        Tweet tweet1 = new Tweet(1, "Alice", "Hello @BOB", d1);
        Tweet tweet2 = new Tweet(2, "bob", "Hi @alice", d2);

        Map<String, Set<String>> followsGraph = SocialNetwork.guessFollowsGraph(Arrays.asList(tweet1, tweet2));

        assertEquals("expected two users in graph", 2, followsGraph.size());

        // The keys should be in some consistent case (likely lowercase)
        // Check that both users exist (regardless of case)
        boolean hasAlice = followsGraph.containsKey("alice") || followsGraph.containsKey("Alice");
        boolean hasBob = followsGraph.containsKey("bob") || followsGraph.containsKey("BOB") || followsGraph.containsKey("Bob");

        assertTrue("expected alice in graph (case insensitive)", hasAlice);
        assertTrue("expected bob in graph (case insensitive)", hasBob);

        // Check follow relationships exist (case-insensitive)
        boolean aliceFollowsBob = false;
        boolean bobFollowsAlice = false;

        for (Map.Entry<String, Set<String>> entry : followsGraph.entrySet()) {
            String user = entry.getKey().toLowerCase();
            Set<String> follows = entry.getValue();

            if (user.equals("alice")) {
                for (String followed : follows) {
                    if (followed.equalsIgnoreCase("bob")) {
                        aliceFollowsBob = true;
                        break;
                    }
                }
            } else if (user.equals("bob")) {
                for (String followed : follows) {
                    if (followed.equalsIgnoreCase("alice")) {
                        bobFollowsAlice = true;
                        break;
                    }
                }
            }
        }

        assertTrue("expected alice follows bob", aliceFollowsBob);
        assertTrue("expected bob follows alice", bobFollowsAlice);
    }

    @Test
    public void testGuessFollowsGraphDuplicateMentions() {
        // Tests duplicate mentions of same user (should not create duplicate follows)
        Tweet tweet = new Tweet(1, "alice", "@bob hello @bob again @bob", d1);

        Map<String, Set<String>> followsGraph = SocialNetwork.guessFollowsGraph(List.of(tweet));

        Set<String> aliceFollows = followsGraph.get("alice");
        assertEquals("expected alice follows bob only once despite duplicates", 1, aliceFollows.size());
        assertTrue("expected alice follows bob", aliceFollows.contains("bob"));
    }

    @Test
    public void testGuessFollowsGraphSelfMention() {
        // Tests self-mention (implementation choice: should probably not create self-follow)
        Tweet tweet = new Tweet(1, "alice", "Hello @alice and @bob", d1);

        Map<String, Set<String>> followsGraph = SocialNetwork.guessFollowsGraph(List.of(tweet));

        assertTrue("expected alice in graph", followsGraph.containsKey("alice"));
        Set<String> aliceFollows = followsGraph.get("alice");

        // Self-mention behavior is implementation choice - document the decision
        // Most reasonable: alice should NOT follow herself
        assertFalse("expected alice does not follow herself", aliceFollows.contains("alice"));
        assertTrue("expected alice follows bob", aliceFollows.contains("bob"));
    }

    @Test
    public void testGuessFollowsGraphInvalidMentions() {
        // Tests invalid @-mentions that should not create relationships
        Tweet tweet = new Tweet(1, "alice", "Email alice@mit.edu and @", d1);

        Map<String, Set<String>> followsGraph = SocialNetwork.guessFollowsGraph(List.of(tweet));

        assertEquals("expected only alice in graph", 1, followsGraph.size());
        assertTrue("expected alice in graph", followsGraph.containsKey("alice"));
        assertTrue("expected alice follows nobody", followsGraph.get("alice").isEmpty());
    }

    @Test
    public void testGuessFollowsGraphMentionedUserNotAuthor() {
        // Tests mentioned user who never authors tweets
        Tweet tweet = new Tweet(1, "alice", "Hello @bob", d1);

        Map<String, Set<String>> followsGraph = SocialNetwork.guessFollowsGraph(List.of(tweet));

        assertEquals("expected both users in graph", 2, followsGraph.size());
        assertTrue("expected alice in graph", followsGraph.containsKey("alice"));
        assertTrue("expected bob in graph", followsGraph.containsKey("bob"));
        assertTrue("expected bob follows nobody", followsGraph.get("bob").isEmpty());
    }

    @Test
    public void testGuessFollowsGraphMutualMentions() {
        // Tests mutual mentions (A mentions B, B mentions A)
        Tweet tweet1 = new Tweet(1, "alice", "Hello @bob", d1);
        Tweet tweet2 = new Tweet(2, "bob", "Hi @alice", d2);

        Map<String, Set<String>> followsGraph = SocialNetwork.guessFollowsGraph(Arrays.asList(tweet1, tweet2));

        assertEquals("expected two users in graph", 2, followsGraph.size());

        Set<String> aliceFollows = followsGraph.get("alice");
        Set<String> bobFollows = followsGraph.get("bob");

        assertEquals("expected alice follows one person", 1, aliceFollows.size());
        assertEquals("expected bob follows one person", 1, bobFollows.size());
        assertTrue("expected alice follows bob", aliceFollows.contains("bob"));
        assertTrue("expected bob follows alice", bobFollows.contains("alice"));
    }

    @Test
    public void testGuessFollowsGraphComplexNetwork() {
        // Tests complex network with multiple relationships
        Tweet tweet1 = new Tweet(1, "alice", "Hello @bob and @charlie", d1);
        Tweet tweet2 = new Tweet(2, "bob", "Hi @dave", d2);
        Tweet tweet3 = new Tweet(3, "charlie", "Hey @alice", d3);
        Tweet tweet4 = new Tweet(4, "dave", "Morning everyone", d4);

        Map<String, Set<String>> followsGraph = SocialNetwork.guessFollowsGraph(
                Arrays.asList(tweet1, tweet2, tweet3, tweet4));

        assertEquals("expected four users in graph", 4, followsGraph.size());

        // Alice follows bob and charlie
        Set<String> aliceFollows = followsGraph.get("alice");
        assertEquals("expected alice follows two people", 2, aliceFollows.size());
        assertTrue("expected alice follows bob", aliceFollows.contains("bob"));
        assertTrue("expected alice follows charlie", aliceFollows.contains("charlie"));

        // Bob follows dave
        Set<String> bobFollows = followsGraph.get("bob");
        assertEquals("expected bob follows one person", 1, bobFollows.size());
        assertTrue("expected bob follows dave", bobFollows.contains("dave"));

        // Charlie follows alice
        Set<String> charlieFollows = followsGraph.get("charlie");
        assertEquals("expected charlie follows one person", 1, charlieFollows.size());
        assertTrue("expected charlie follows alice", charlieFollows.contains("alice"));

        // Dave follows nobody
        assertTrue("expected dave follows nobody", followsGraph.get("dave").isEmpty());
    }

    @Test
    public void testGuessFollowsGraphOriginalListUnmodified() {
        // Tests that original tweets list is not modified
        Tweet tweet1 = new Tweet(1, "alice", "Hello @bob", d1);
        Tweet tweet2 = new Tweet(2, "bob", "Hi there", d2);
        List<Tweet> originalTweets = new ArrayList<>(Arrays.asList(tweet1, tweet2));
        List<Tweet> tweetsCopy = new ArrayList<>(originalTweets);

        SocialNetwork.guessFollowsGraph(originalTweets);

        assertEquals("expected original list unchanged", tweetsCopy, originalTweets);
    }

    @Test
    public void testGuessFollowsGraphValidUsernameCharacters() {
        // Tests mentions with valid username characters
        Tweet tweet = new Tweet(1, "alice", "Hello @user_123 and @test-user", d1);

        Map<String, Set<String>> followsGraph = SocialNetwork.guessFollowsGraph(List.of(tweet));

        assertEquals("expected three users in graph", 3, followsGraph.size());
        assertTrue("expected alice in graph", followsGraph.containsKey("alice"));
        assertTrue("expected user_123 in graph", followsGraph.containsKey("user_123"));
        assertTrue("expected test-user in graph", followsGraph.containsKey("test-user"));

        Set<String> aliceFollows = followsGraph.get("alice");
        assertEquals("expected alice follows two people", 2, aliceFollows.size());
        assertTrue("expected alice follows user_123", aliceFollows.contains("user_123"));
        assertTrue("expected alice follows test-user", aliceFollows.contains("test-user"));
    }

    @Test
    public void testGuessFollowsGraphReturnType() {
        // Tests that returned map and sets are modifiable (not using Collections.unmodifiable)
        Tweet tweet = new Tweet(1, "alice", "Hello @bob", d1);

        Map<String, Set<String>> followsGraph = SocialNetwork.guessFollowsGraph(List.of(tweet));

        // Should be able to modify returned structures (they're not specified as immutable)
        try {
            followsGraph.put("test", new HashSet<>());
            followsGraph.get("alice").add("test");
            // If we get here without exception, the structures are modifiable
            assertTrue("expected modifiable map and sets", true);
        } catch (UnsupportedOperationException e) {
            fail("expected modifiable map and sets, but got immutable structures");
        }
    }

    @Test
    public void testInfluencersEmpty() {
        // Tests empty social network
        Map<String, Set<String>> followsGraph = new HashMap<>();

        List<String> influencers = SocialNetwork.influencers(followsGraph);

        assertTrue("expected empty list for empty graph", influencers.isEmpty());
    }

    @Test
    public void testInfluencersSingleUser() {
        // Tests single user with no followers
        Map<String, Set<String>> followsGraph = new HashMap<>();
        followsGraph.put("alice", new HashSet<>());

        List<String> influencers = SocialNetwork.influencers(followsGraph);

        assertEquals("expected single user", 1, influencers.size());
        assertEquals("expected alice", "alice", influencers.getFirst());
    }

    @Test
    public void testInfluencersNoFollowers() {
        // Tests multiple users, none with followers
        Map<String, Set<String>> followsGraph = new HashMap<>();
        followsGraph.put("alice", new HashSet<>());
        followsGraph.put("bob", new HashSet<>());
        followsGraph.put("charlie", new HashSet<>());

        List<String> influencers = SocialNetwork.influencers(followsGraph);

        assertEquals("expected three users", 3, influencers.size());
        assertTrue("expected alice in list", influencers.contains("alice"));
        assertTrue("expected bob in list", influencers.contains("bob"));
        assertTrue("expected charlie in list", influencers.contains("charlie"));
        // Order is unspecified for ties, so just check all are present
    }

    @Test
    public void testInfluencersDistinctFollowerCounts() {
        // Tests users with different follower counts
        Map<String, Set<String>> followsGraph = new HashMap<>();
        followsGraph.put("alice", new HashSet<>(List.of("bob"))); // alice follows bob
        followsGraph.put("bob", new HashSet<>(List.of("charlie"))); // bob follows charlie
        followsGraph.put("charlie", new HashSet<>()); // charlie follows nobody
        followsGraph.put("dave", new HashSet<>(List.of("charlie"))); // dave follows charlie

        // Follower counts: charlie=2, bob=1, alice=0, dave=0
        List<String> influencers = SocialNetwork.influencers(followsGraph);

        assertEquals("expected four users", 4, influencers.size());
        assertEquals("expected charlie first (2 followers)", "charlie", influencers.get(0));
        assertEquals("expected bob second (1 follower)", "bob", influencers.get(1));
        // alice and dave both have 0 followers - order between them unspecified
        assertTrue("expected alice in last two positions",
                influencers.get(2).equals("alice") || influencers.get(3).equals("alice"));
        assertTrue("expected dave in last two positions",
                influencers.get(2).equals("dave") || influencers.get(3).equals("dave"));
    }

    @Test
    public void testInfluencersTiedFollowerCounts() {
        // Tests users with same follower counts
        Map<String, Set<String>> followsGraph = new HashMap<>();
        followsGraph.put("alice", new HashSet<>(Arrays.asList("bob", "charlie")));
        followsGraph.put("bob", new HashSet<>(Arrays.asList("dave", "charlie")));
        followsGraph.put("charlie", new HashSet<>());
        followsGraph.put("dave", new HashSet<>());

        // Follower counts: charlie=2, dave=1, bob=1, alice=0
        List<String> influencers = SocialNetwork.influencers(followsGraph);

        assertEquals("expected four users", 4, influencers.size());
        assertEquals("expected charlie first (2 followers)", "charlie", influencers.get(0));

        // bob and dave both have 1 follower - should be in positions 1 and 2
        Set<String> middleTwo = new HashSet<>(Arrays.asList(influencers.get(1), influencers.get(2)));
        assertTrue("expected bob in middle positions", middleTwo.contains("bob"));
        assertTrue("expected dave in middle positions", middleTwo.contains("dave"));

        assertEquals("expected alice last (0 followers)", "alice", influencers.get(3));
    }

    @Test
    public void testInfluencersStarNetwork() {
        // Tests star network where everyone follows one central person
        Map<String, Set<String>> followsGraph = new HashMap<>();
        followsGraph.put("alice", new HashSet<>(List.of("star")));
        followsGraph.put("bob", new HashSet<>(List.of("star")));
        followsGraph.put("charlie", new HashSet<>(List.of("star")));
        followsGraph.put("star", new HashSet<>());

        // Follower counts: star=3, others=0
        List<String> influencers = SocialNetwork.influencers(followsGraph);

        assertEquals("expected four users", 4, influencers.size());
        assertEquals("expected star first", "star", influencers.getFirst());

        // Others all have 0 followers
        Set<String> lastThree = new HashSet<>(influencers.subList(1, 4));
        assertTrue("expected alice in last three", lastThree.contains("alice"));
        assertTrue("expected bob in last three", lastThree.contains("bob"));
        assertTrue("expected charlie in last three", lastThree.contains("charlie"));
    }

    @Test
    public void testInfluencersLinearChain() {
        // Tests linear chain A→B→C→D
        Map<String, Set<String>> followsGraph = new HashMap<>();
        followsGraph.put("alice", new HashSet<>(List.of("bob")));
        followsGraph.put("bob", new HashSet<>(List.of("charlie")));
        followsGraph.put("charlie", new HashSet<>(List.of("dave")));
        followsGraph.put("dave", new HashSet<>());

        // Follower counts: bob=1, charlie=1, dave=1, alice=0
        List<String> influencers = SocialNetwork.influencers(followsGraph);

        assertEquals("expected four users", 4, influencers.size());

        // First three should have 1 follower each
        Set<String> firstThree = new HashSet<>(influencers.subList(0, 3));
        assertTrue("expected bob in first three", firstThree.contains("bob"));
        assertTrue("expected charlie in first three", firstThree.contains("charlie"));
        assertTrue("expected dave in first three", firstThree.contains("dave"));

        assertEquals("expected alice last (0 followers)", "alice", influencers.get(3));
    }

    @Test
    public void testInfluencersComplexNetwork() {
        // Tests complex network with various relationships
        Map<String, Set<String>> followsGraph = new HashMap<>();
        followsGraph.put("alice", new HashSet<>(List.of("popular")));
        followsGraph.put("bob", new HashSet<>(Arrays.asList("popular", "charlie")));
        followsGraph.put("charlie", new HashSet<>(List.of("popular")));
        followsGraph.put("dave", new HashSet<>(List.of("charlie")));
        followsGraph.put("popular", new HashSet<>(List.of("charlie")));

        // Follower counts: popular=3, charlie=3, alice=0, bob=0, dave=0
        List<String> influencers = SocialNetwork.influencers(followsGraph);

        assertEquals("expected five users", 5, influencers.size());

        // popular and charlie both have 3 followers - should be first two
        Set<String> firstTwo = new HashSet<>(Arrays.asList(influencers.get(0), influencers.get(1)));
        assertTrue("expected popular in first two", firstTwo.contains("popular"));
        assertTrue("expected charlie in first two", firstTwo.contains("charlie"));

        // alice, bob, dave all have 0 followers - should be last three
        Set<String> lastThree = new HashSet<>(influencers.subList(2, 5));
        assertTrue("expected alice in last three", lastThree.contains("alice"));
        assertTrue("expected bob in last three", lastThree.contains("bob"));
        assertTrue("expected dave in last three", lastThree.contains("dave"));
    }

    @Test
    public void testInfluencersAllUsersIncluded() {
        // Tests that all users from graph are included exactly once
        Map<String, Set<String>> followsGraph = new HashMap<>();
        followsGraph.put("alice", new HashSet<>(List.of("bob")));
        followsGraph.put("bob", new HashSet<>());
        followsGraph.put("charlie", new HashSet<>(List.of("alice")));

        List<String> influencers = SocialNetwork.influencers(followsGraph);

        assertEquals("expected three users", 3, influencers.size());

        // Check all users present exactly once
        Set<String> influencerSet = new HashSet<>(influencers);
        assertEquals("expected no duplicates", influencers.size(), influencerSet.size());
        assertTrue("expected alice present", influencerSet.contains("alice"));
        assertTrue("expected bob present", influencerSet.contains("bob"));
        assertTrue("expected charlie present", influencerSet.contains("charlie"));
    }

    @Test
    public void testInfluencersDescendingOrder() {
        // Tests strict descending order when all follower counts are different
        Map<String, Set<String>> followsGraph = new HashMap<>();
        followsGraph.put("least", new HashSet<>());
        followsGraph.put("middle", new HashSet<>(List.of("least")));
        followsGraph.put("most", new HashSet<>(Arrays.asList("least", "middle")));

        // Follower counts: least=2, middle=1, most=0
        List<String> influencers = SocialNetwork.influencers(followsGraph);

        assertEquals("expected three users", 3, influencers.size());
        assertEquals("expected least first (2 followers)", "least", influencers.get(0));
        assertEquals("expected middle second (1 follower)", "middle", influencers.get(1));
        assertEquals("expected most last (0 followers)", "most", influencers.get(2));
    }

    @Test
    public void testInfluencersOriginalMapUnmodified() {
        // Tests that original map is not modified
        Map<String, Set<String>> originalGraph = new HashMap<>();
        originalGraph.put("alice", new HashSet<>(List.of("bob")));
        originalGraph.put("bob", new HashSet<>());

        Map<String, Set<String>> graphCopy = new HashMap<>();
        for (Map.Entry<String, Set<String>> entry : originalGraph.entrySet()) {
            graphCopy.put(entry.getKey(), new HashSet<>(entry.getValue()));
        }

        SocialNetwork.influencers(originalGraph);

        assertEquals("expected original map unchanged", graphCopy, originalGraph);
    }

    @Test
    public void testInfluencersConsistentOrdering() {
        // Tests that method returns consistent results for same input
        Map<String, Set<String>> followsGraph = new HashMap<>();
        followsGraph.put("alice", new HashSet<>(List.of("target")));
        followsGraph.put("bob", new HashSet<>(List.of("target")));
        followsGraph.put("target", new HashSet<>());

        List<String> result1 = SocialNetwork.influencers(followsGraph);
        List<String> result2 = SocialNetwork.influencers(followsGraph);

        assertEquals("expected consistent results", result1, result2);
    }

    @Test
    public void testInfluencersHighFollowerCount() {
        // Tests user with many followers
        Map<String, Set<String>> followsGraph = new HashMap<>();

        // Create celebrity followed by many users
        followsGraph.put("celebrity", new HashSet<>());
        for (int i = 0; i < 10; i++) {
            String follower = "user" + i;
            followsGraph.put(follower, new HashSet<>(List.of("celebrity")));
        }

        List<String> influencers = SocialNetwork.influencers(followsGraph);

        assertEquals("expected 11 users", 11, influencers.size());
        assertEquals("expected celebrity first", "celebrity", influencers.getFirst());

        // All others should have 0 followers and be after celebrity
        for (int i = 1; i < influencers.size(); i++) {
            String user = influencers.get(i);
            assertTrue("expected user in format user0-user9", user.startsWith("user"));
        }
    }

    @Test
    public void testInfluencersMutualFollows() {
        // Tests mutual following relationships
        Map<String, Set<String>> followsGraph = new HashMap<>();
        followsGraph.put("alice", new HashSet<>(List.of("bob")));
        followsGraph.put("bob", new HashSet<>(List.of("alice")));

        // Both have 1 follower each
        List<String> influencers = SocialNetwork.influencers(followsGraph);

        assertEquals("expected two users", 2, influencers.size());
        assertTrue("expected alice in list", influencers.contains("alice"));
        assertTrue("expected bob in list", influencers.contains("bob"));
        // Both have same follower count, so order between them is unspecified
    }

    /*
     * Warning: all the tests you write here must be runnable against any
     * SocialNetwork class that follows the spec. It will be run against several
     * staff implementations of SocialNetwork, which will be done by overwriting
     * (temporarily) your version of SocialNetwork with the staff's version.
     * DO NOT strengthen the spec of SocialNetwork or its methods.
     * 
     * In particular, your test cases must not call helper methods of your own
     * that you have put in SocialNetwork, because that means you're testing a
     * stronger spec than SocialNetwork says. If you need such helper methods,
     * define them in a different class. If you only need them in this test
     * class, then keep them in this test class.
     */

}
