package twitter;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import static org.junit.Assert.*;

public class MySocialNetworkTest {

    private static final Instant d1 = Instant.parse("2016-02-17T10:00:00Z");
    private static final Instant d2 = Instant.parse("2016-02-17T11:00:00Z");
    private static final Instant d3 = Instant.parse("2016-02-17T12:00:00Z");
    private static final Instant d4 = Instant.parse("2016-02-17T13:00:00Z");

    @Test
    public void testHashtagEvidenceBasic() {
        // Tests basic hashtag evidence between two users
        Tweet tweet1 = new Tweet(1, "alice", "Loving #java programming!", d1);
        Tweet tweet2 = new Tweet(2, "bob", "Just learned #java today", d2);

        Map<String, Set<String>> followsGraph = SocialNetwork.guessFollowsGraph(Arrays.asList(tweet1, tweet2));

        // Both users should follow each other due to shared #java hashtag
        assertTrue("expected alice follows bob", followsGraph.get("alice").contains("bob"));
        assertTrue("expected bob follows alice", followsGraph.get("bob").contains("alice"));
    }

    @Test
    public void testHashtagEvidenceMultipleUsers() {
        // Tests hashtag evidence among multiple users
        Tweet tweet1 = new Tweet(1, "alice", "Great #mit lecture today!", d1);
        Tweet tweet2 = new Tweet(2, "bob", "Proud to be at #mit", d2);
        Tweet tweet3 = new Tweet(3, "charlie", "Love #mit classes", d3);

        Map<String, Set<String>> followsGraph = SocialNetwork.guessFollowsGraph(Arrays.asList(tweet1, tweet2, tweet3));

        // All three should follow each other due to shared #mit hashtag
        assertTrue("expected alice follows bob", followsGraph.get("alice").contains("bob"));
        assertTrue("expected alice follows charlie", followsGraph.get("alice").contains("charlie"));
        assertTrue("expected bob follows alice", followsGraph.get("bob").contains("alice"));
        assertTrue("expected bob follows charlie", followsGraph.get("bob").contains("charlie"));
        assertTrue("expected charlie follows alice", followsGraph.get("charlie").contains("alice"));
        assertTrue("expected charlie follows bob", followsGraph.get("charlie").contains("bob"));
    }

    @Test
    public void testHashtagEvidenceCaseInsensitive() {
        // Tests that hashtag matching is case-insensitive
        Tweet tweet1 = new Tweet(1, "alice", "Learning #Java today", d1);
        Tweet tweet2 = new Tweet(2, "bob", "Love #JAVA programming", d2);

        Map<String, Set<String>> followsGraph = SocialNetwork.guessFollowsGraph(Arrays.asList(tweet1, tweet2));

        // Should create mutual follows despite case differences
        assertTrue("expected alice follows bob", followsGraph.get("alice").contains("bob"));
        assertTrue("expected bob follows alice", followsGraph.get("bob").contains("alice"));
    }

    @Test
    public void testHashtagEvidenceCommonHashtagIgnored() {
        // Tests that very common hashtags (>4 users) don't create evidence
        Tweet tweet1 = new Tweet(1, "alice", "Good morning #today", d1);
        Tweet tweet2 = new Tweet(2, "bob", "Nice weather #today", d2);
        Tweet tweet3 = new Tweet(3, "charlie", "Busy day #today", d3);
        Tweet tweet4 = new Tweet(4, "dave", "Happy #today", d4);
        Tweet tweet5 = new Tweet(5, "eve", "Great #today", d1);

        Map<String, Set<String>> followsGraph = SocialNetwork.guessFollowsGraph(
                Arrays.asList(tweet1, tweet2, tweet3, tweet4, tweet5));

        // No follows should be created due to #today being too common (5 users > 4)
        assertTrue("expected alice follows nobody", followsGraph.get("alice").isEmpty());
        assertTrue("expected bob follows nobody", followsGraph.get("bob").isEmpty());
        assertTrue("expected charlie follows nobody", followsGraph.get("charlie").isEmpty());
        assertTrue("expected dave follows nobody", followsGraph.get("dave").isEmpty());
        assertTrue("expected eve follows nobody", followsGraph.get("eve").isEmpty());
    }

    @Test
    public void testHashtagEvidenceUniqueHashtagIgnored() {
        // Tests that unique hashtags (1 user) don't create evidence
        Tweet tweet = new Tweet(1, "alice", "My unique thought #alicespecial", d1);

        Map<String, Set<String>> followsGraph = SocialNetwork.guessFollowsGraph(List.of(tweet));

        // No additional follows should be created from unique hashtag
        assertTrue("expected alice follows nobody", followsGraph.get("alice").isEmpty());
    }

    @Test
    public void testHashtagEvidenceMultipleHashtags() {
        // Tests users sharing multiple hashtags
        Tweet tweet1 = new Tweet(1, "alice", "Love #java and #programming", d1);
        Tweet tweet2 = new Tweet(2, "bob", "Learning #java and #programming", d2);

        Map<String, Set<String>> followsGraph = SocialNetwork.guessFollowsGraph(Arrays.asList(tweet1, tweet2));

        // Should create mutual follows (evidence from both hashtags)
        assertTrue("expected alice follows bob", followsGraph.get("alice").contains("bob"));
        assertTrue("expected bob follows alice", followsGraph.get("bob").contains("alice"));
    }

    @Test
    public void testHashtagEvidenceWithMentions() {
        // Tests that hashtag evidence combines with mention evidence
        Tweet tweet1 = new Tweet(1, "alice", "Hey @bob, check out #java!", d1);
        Tweet tweet2 = new Tweet(2, "bob", "Thanks! I love #java too", d2);
        Tweet tweet3 = new Tweet(3, "charlie", "Also learning #java", d3);

        Map<String, Set<String>> followsGraph = SocialNetwork.guessFollowsGraph(Arrays.asList(tweet1, tweet2, tweet3));

        // alice follows bob (mention evidence)
        assertTrue("expected alice follows bob from mention", followsGraph.get("alice").contains("bob"));

        // Hashtag evidence should create additional follows
        assertTrue("expected alice follows charlie from hashtag", followsGraph.get("alice").contains("charlie"));
        assertTrue("expected bob follows alice from hashtag", followsGraph.get("bob").contains("alice"));
        assertTrue("expected bob follows charlie from hashtag", followsGraph.get("bob").contains("charlie"));
        assertTrue("expected charlie follows alice from hashtag", followsGraph.get("charlie").contains("alice"));
        assertTrue("expected charlie follows bob from hashtag", followsGraph.get("charlie").contains("bob"));
    }

    @Test
    public void testHashtagEvidencePartialOverlap() {
        // Tests mixed hashtag usage - some shared, some not
        Tweet tweet1 = new Tweet(1, "alice", "Love #java and #mit", d1);
        Tweet tweet2 = new Tweet(2, "bob", "Learning #java and #python", d2);
        Tweet tweet3 = new Tweet(3, "charlie", "At #mit studying #algorithms", d3);

        Map<String, Set<String>> followsGraph = SocialNetwork.guessFollowsGraph(Arrays.asList(tweet1, tweet2, tweet3));

        // alice-bob: share #java
        assertTrue("expected alice follows bob", followsGraph.get("alice").contains("bob"));
        assertTrue("expected bob follows alice", followsGraph.get("bob").contains("alice"));

        // alice-charlie: share #mit
        assertTrue("expected alice follows charlie", followsGraph.get("alice").contains("charlie"));
        assertTrue("expected charlie follows alice", followsGraph.get("charlie").contains("alice"));

        // bob-charlie: no shared hashtags
        assertFalse("expected bob doesn't follow charlie", followsGraph.get("bob").contains("charlie"));
        assertFalse("expected charlie doesn't follow bob", followsGraph.get("charlie").contains("bob"));
    }

    @Test
    public void testHashtagEvidenceInvalidHashtags() {
        // Tests that malformed hashtags don't create evidence
        Tweet tweet1 = new Tweet(1, "alice", "Email: alice@test.com and # space", d1);
        Tweet tweet2 = new Tweet(2, "bob", "Price: $100 #", d2);

        Map<String, Set<String>> followsGraph = SocialNetwork.guessFollowsGraph(Arrays.asList(tweet1, tweet2));

        // No hashtag evidence should be created from malformed hashtags
        assertTrue("expected alice follows nobody", followsGraph.get("alice").isEmpty());
        assertTrue("expected bob follows nobody", followsGraph.get("bob").isEmpty());
    }

    @Test
    public void testHashtagEvidenceSpecialCharacters() {
        // Tests hashtags with valid characters (letters, numbers, underscore)
        Tweet tweet1 = new Tweet(1, "alice", "Event #MIT_2024 was great!", d1);
        Tweet tweet2 = new Tweet(2, "bob", "Enjoyed #MIT_2024 conference", d2);

        Map<String, Set<String>> followsGraph = SocialNetwork.guessFollowsGraph(Arrays.asList(tweet1, tweet2));

        // Should create mutual follows from shared hashtag with underscore and numbers
        assertTrue("expected alice follows bob", followsGraph.get("alice").contains("bob"));
        assertTrue("expected bob follows alice", followsGraph.get("bob").contains("alice"));
    }

    @Test
    public void testHashtagEvidenceOptimalRange() {
        // Tests that hashtags used by exactly 2-4 users create evidence
        Tweet tweet1 = new Tweet(1, "alice", "Topic #specialtag", d1);
        Tweet tweet2 = new Tweet(2, "bob", "Discussing #specialtag", d2);
        Tweet tweet3 = new Tweet(3, "charlie", "More about #specialtag", d3);
        Tweet tweet4 = new Tweet(4, "dave", "Final thoughts on #specialtag", d4);

        Map<String, Set<String>> followsGraph = SocialNetwork.guessFollowsGraph(
                Arrays.asList(tweet1, tweet2, tweet3, tweet4));

        // All four users should follow each other (4 users = at boundary of acceptable range)
        String[] users = {"alice", "bob", "charlie", "dave"};
        for (String user1 : users) {
            for (String user2 : users) {
                if (!user1.equals(user2)) {
                    assertTrue("expected " + user1 + " follows " + user2,
                            followsGraph.get(user1).contains(user2));
                }
            }
        }
    }

    @Test
    public void testHashtagEvidencePreservesOriginalEvidence() {
        // Tests that adding hashtag evidence doesn't interfere with mention evidence
        Tweet tweet1 = new Tweet(1, "alice", "Hello @bob", d1);
        Tweet tweet2 = new Tweet(2, "charlie", "Learning #java", d2);
        Tweet tweet3 = new Tweet(3, "dave", "Also learning #java", d3);

        Map<String, Set<String>> followsGraph = SocialNetwork.guessFollowsGraph(Arrays.asList(tweet1, tweet2, tweet3));

        // Original mention evidence should be preserved
        assertTrue("expected alice follows bob from mention", followsGraph.get("alice").contains("bob"));
        assertTrue("expected bob in graph", followsGraph.containsKey("bob"));
        assertTrue("expected bob follows nobody", followsGraph.get("bob").isEmpty());

        // Hashtag evidence should work independently
        assertTrue("expected charlie follows dave from hashtag", followsGraph.get("charlie").contains("dave"));
        assertTrue("expected dave follows charlie from hashtag", followsGraph.get("dave").contains("charlie"));

        // No cross-contamination
        assertFalse("expected alice doesn't follow charlie", followsGraph.get("alice").contains("charlie"));
        assertFalse("expected charlie doesn't follow alice", followsGraph.get("charlie").contains("alice"));
    }
}