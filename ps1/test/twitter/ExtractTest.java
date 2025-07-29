/* Copyright (c) 2007-2016 MIT 6.005 course staff, all rights reserved.
 * Redistribution of original or derived work requires permission of course staff.
 */
package twitter;

import static org.junit.Assert.*;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.junit.Test;

public class ExtractTest {

    /*
     * Testing strategy for Extract.getTimespan():
     *
     * Input space partitioning:
     * - List size: empty, single tweet, two tweets, many tweets
     * - Tweet timestamps: all same, chronological order, reverse chronological, mixed order
     * - Time differences: identical times, small differences, large differences
     *
     * Output space partitioning:
     * - Timespan where start == end (single timestamp or all identical)
     * - Timespan where start < end (multiple different timestamps)
     *
     * Test cases chosen to cover each partition and catch common bugs:
     * - Empty list handling
     * - Single tweet (start == end)
     * - Two tweets in chronological order
     * - Two tweets in reverse order
     * - Multiple tweets with mixed order
     * - Tweets with identical timestamps
     *
     *
     * Testing strategy for Extract.getMentionedUsers():
     *
     * Input space partitioning:
     * - List size: empty list, single tweet, multiple tweets
     * - Tweet text content:
     *   - No mentions
     *   - Single mention
     *   - Multiple mentions in one tweet
     *   - Multiple mentions across tweets
     * - Mention format:
     *   - Valid mentions: @username
     *   - Invalid mentions: email addresses (user@domain.com)
     *   - Edge cases: @ at start/end of text, @ with no following username
     * - Username validity:
     *   - Valid characters: letters, digits, underscore, hyphen
     *   - Invalid characters: spaces, punctuation, special chars
     * - Case sensitivity: mixed case usernames
     * - Duplicate handling: same username mentioned multiple times
     * - Context sensitivity: mentions preceded/followed by valid username chars
     *
     * Output space partitioning:
     * - Empty set (no valid mentions)
     * - Single username
     * - Multiple unique usernames
     * - Case-insensitive deduplication
     */
    
    private static final Instant d1 = Instant.parse("2016-02-17T10:00:00Z");
    private static final Instant d2 = Instant.parse("2016-02-17T11:00:00Z");
    private static final Instant d3 = Instant.parse("2016-02-17T12:00:00Z");
    
    private static final Tweet tweet1 = new Tweet(1, "alyssa", "is it reasonable to talk about rivest so much?", d1);
    private static final Tweet tweet2 = new Tweet(2, "bbitdiddle", "rivest talk in 30 minutes #hype", d2);
    private static final Tweet tweet3 = new Tweet(3, "ccharles", "rivest is great", d3);

    @Test(expected=AssertionError.class)
    public void testAssertionsEnabled() {
        assert false; // make sure assertions are enabled with VM argument: -ea
    }

    @Test
    public void testGetTimespanEmptyList() {
        // Tests empty list partition - should throw exception per spec
        List<Tweet> tweets = List.of();

        assertThrows(IllegalArgumentException.class, () -> Extract.getTimespan(tweets));
    }

    @Test
    public void testGetTimespanSingleTweet() {
        // Tests single tweet partition - start should equal end
        Timespan timespan = Extract.getTimespan(List.of(tweet1));

        assertEquals("expected start equals tweet timestamp", d1, timespan.getStart());
        assertEquals("expected end equals tweet timestamp", d1, timespan.getEnd());
    }

    @Test
    public void testGetTimespanTwoTweetsChronological() {
        // Tests two tweets in chronological order
        // tweet1 has earlier timestamp d1, tweet2 has later timestamp d2
        Timespan timespan = Extract.getTimespan(Arrays.asList(tweet1, tweet2));

        assertEquals("expected start", d1, timespan.getStart());
        assertEquals("expected end", d2, timespan.getEnd());
    }

    @Test
    public void testGetTimespanTwoTweetsReverseOrder() {
        // Tests two tweets in reverse chronological order
        // Should still return correct start/end regardless of input order
        Timespan timespan = Extract.getTimespan(Arrays.asList(tweet2, tweet1));

        assertEquals("expected start", d1, timespan.getStart());
        assertEquals("expected end", d2, timespan.getEnd());
    }

    @Test
    public void testGetTimespanMultipleTweetsMixedOrder() {
        // Tests multiple tweets in mixed chronological order
        // tweet3 has timestamp d3 which is between d1 and d2
        Timespan timespan = Extract.getTimespan(Arrays.asList(tweet2, tweet1, tweet3));

        assertEquals("expected earliest timestamp", d1, timespan.getStart());
        assertEquals("expected latest timestamp", d3, timespan.getEnd());
    }

    @Test
    public void testGetTimespanIdenticalTimestamps() {
        // Tests tweets with identical timestamps - start should equal end
        Tweet tweetSameTime1 = new Tweet(1, "user1", "text1", d1);
        Tweet tweetSameTime2 = new Tweet(2, "user2", "text2", d1);

        Timespan timespan = Extract.getTimespan(Arrays.asList(tweetSameTime1, tweetSameTime2));

        assertEquals("expected start equals common timestamp", d1, timespan.getStart());
        assertEquals("expected end equals common timestamp", d1, timespan.getEnd());
    }

    @Test
    public void testGetMentionedUsersEmptyList() {
        // Tests empty list partition
        List<Tweet> tweets = List.of();
        Set<String> mentionedUsers = Extract.getMentionedUsers(tweets);

        assertTrue("expected empty set", mentionedUsers.isEmpty());
    }

    @Test
    public void testGetMentionedUsersNoMentions() {
        // Tests tweets with no @ mentions
        Tweet tweet = new Tweet(1, "alice", "Hello world! This is a tweet.", d1);
        Set<String> mentionedUsers = Extract.getMentionedUsers(List.of(tweet));

        assertTrue("expected empty set when no mentions", mentionedUsers.isEmpty());
    }

    @Test
    public void testGetMentionedUsersSingleMention() {
        // Tests single valid mention
        Tweet tweet = new Tweet(1, "alice", "Hello @bob how are you?", d1);
        Set<String> mentionedUsers = Extract.getMentionedUsers(List.of(tweet));

        Set<String> expected = Set.of("bob");
        assertEquals("expected single mention", expected, mentionedUsers);
    }

    @Test
    public void testGetMentionedUsersMultipleMentionsOneTweet() {
        // Tests multiple mentions in single tweet
        Tweet tweet = new Tweet(1, "alice", "Hey @bob and @charlie, let's meet @dave!", d1);
        Set<String> mentionedUsers = Extract.getMentionedUsers(List.of(tweet));

        Set<String> expected = Set.of("bob", "charlie", "dave");
        assertEquals("expected three mentions", expected, mentionedUsers);
    }

    @Test
    public void testGetMentionedUsersMultipleTweets() {
        // Tests mentions across multiple tweets
        Tweet tweet1 = new Tweet(1, "alice", "Hello @bob", d1);
        Tweet tweet2 = new Tweet(2, "charlie", "Hi @dave and @eve", d2);
        Set<String> mentionedUsers = Extract.getMentionedUsers(Arrays.asList(tweet1, tweet2));

        Set<String> expected = Set.of("bob", "dave", "eve");
        assertEquals("expected mentions from multiple tweets", expected, mentionedUsers);
    }

    @Test
    public void testGetMentionedUsersCaseInsensitive() {
        // Tests case insensitivity - same username in different cases
        Tweet tweet1 = new Tweet(1, "alice", "Hello @Bob", d1);
        Tweet tweet2 = new Tweet(2, "charlie", "Hi @BOB and @bob", d2);
        Set<String> mentionedUsers = Extract.getMentionedUsers(Arrays.asList(tweet1, tweet2));

        assertEquals("expected single unique username despite case differences", 1, mentionedUsers.size());
        // The spec allows any case to be returned, so we just check size
    }

    @Test
    public void testGetMentionedUsersValidUsernameCharacters() {
        // Tests usernames with all valid characters (letters, digits, underscore, hyphen)
        Tweet tweet = new Tweet(1, "alice", "Hello @user_123 and @test-user", d1);
        Set<String> mentionedUsers = Extract.getMentionedUsers(List.of(tweet));

        Set<String> expected = Set.of("user_123", "test-user");
        assertEquals("expected usernames with valid characters", expected, mentionedUsers);
    }

    @Test
    public void testGetMentionedUsersEmailNotMention() {
        // Tests that email addresses don't count as mentions
        Tweet tweet = new Tweet(1, "alice", "Email me at alice@mit.edu", d1);
        Set<String> mentionedUsers = Extract.getMentionedUsers(List.of(tweet));

        assertTrue("expected no mentions from email address", mentionedUsers.isEmpty());
    }

    @Test
    public void testGetMentionedUsersAtStartAndEnd() {
        // Tests mentions at start and end of tweet
        Tweet tweet = new Tweet(1, "alice", "@bob hello world @charlie", d1);
        Set<String> mentionedUsers = Extract.getMentionedUsers(List.of(tweet));

        Set<String> expected = Set.of("bob", "charlie");
        assertEquals("expected mentions at start and end", expected, mentionedUsers);
    }

    @Test
    public void testGetMentionedUsersAtWithoutUsername() {
        // Tests @ symbol without valid username following
        Tweet tweet = new Tweet(1, "alice", "Hello @ world and @", d1);
        Set<String> mentionedUsers = Extract.getMentionedUsers(List.of(tweet));

        assertTrue("expected no mentions when @ not followed by valid username", mentionedUsers.isEmpty());
    }

    @Test
    public void testGetMentionedUsersInvalidContext() {
        // Tests mentions preceded/followed by valid username characters (should not count)
        Tweet tweet = new Tweet(1, "alice", "user@bob and @charlieX", d1);
        Set<String> mentionedUsers = Extract.getMentionedUsers(List.of(tweet));

        Set<String> expected = Set.of("charliex");
        assertEquals("expected mentions with invalid context", expected, mentionedUsers);
    }

    @Test
    public void testGetMentionedUsersValidContext() {
        // Tests mentions with valid separators (spaces, punctuation)
        Tweet tweet = new Tweet(1, "alice", "Hello @bob! How are you, @charlie?", d1);
        Set<String> mentionedUsers = Extract.getMentionedUsers(List.of(tweet));

        Set<String> expected = Set.of("bob", "charlie");
        assertEquals("expected mentions with punctuation separators", expected, mentionedUsers);
    }

    @Test
    public void testGetMentionedUsersDuplicatesSameCase() {
        // Tests duplicate mentions in same case
        Tweet tweet = new Tweet(1, "alice", "@bob hello @bob again @bob", d1);
        Set<String> mentionedUsers = Extract.getMentionedUsers(List.of(tweet));

        Set<String> expected = Set.of("bob");
        assertEquals("expected single mention despite duplicates", expected, mentionedUsers);
    }

    @Test
    public void testGetMentionedUsersSpecialCharacterSeparators() {
        // Tests mentions separated by various punctuation
        Tweet tweet = new Tweet(1, "alice", "@alice,@bob;@charlie.@dave(@eve)@frank", d1);
        Set<String> mentionedUsers = Extract.getMentionedUsers(List.of(tweet));

        Set<String> expected = Set.of("alice", "bob", "charlie", "dave", "eve", "frank");
        assertEquals("expected mentions separated by punctuation", expected, mentionedUsers);
    }

    /*
     * Warning: all the tests you write here must be runnable against any
     * Extract class that follows the spec. It will be run against several staff
     * implementations of Extract, which will be done by overwriting
     * (temporarily) your version of Extract with the staff's version.
     * DO NOT strengthen the spec of Extract or its methods.
     * 
     * In particular, your test cases must not call helper methods of your own
     * that you have put in Extract, because that means you're testing a
     * stronger spec than Extract says. If you need such helper methods, define
     * them in a different class. If you only need them in this test class, then
     * keep them in this test class.
     */

}
