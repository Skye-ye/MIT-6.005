/* Copyright (c) 2007-2016 MIT 6.005 course staff, all rights reserved.
 * Redistribution of original or derived work requires permission of course staff.
 */
package twitter;

import static org.junit.Assert.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class FilterTest {

    /*
     * Testing strategy for Filter.writtenBy():
     *
     * Input space partitioning:
     * - tweets list: empty, single tweet, multiple tweets
     * - username parameter: valid Twitter username (letters, digits, underscore, hyphen)
     * - Match conditions:
     *   - No tweets by the user
     *   - Single tweet by the user
     *   - Multiple tweets by the user
     *   - Mixed: some tweets by user, some by others
     * - Case sensitivity: username parameter vs tweet author in different cases
     * - Order preservation: result maintains original order
     *
     * Output space partitioning:
     * - Empty list (no matches)
     * - Single tweet (one match)
     * - Multiple tweets (multiple matches, preserving order)
     *
     * Edge cases to test:
     * - Case insensitive matching (per Tweet.getAuthor() spec)
     * - Order preservation when multiple matches
     * - No modification of original list
     *
     *
     * Testing strategy for Filter.inTimespan():
     *
     * Input space partitioning:
     * - tweets list: empty, single tweet, multiple tweets
     * - timespan: point timespan (start == end), interval (start < end)
     * - tweet timestamps relative to timespan:
     *   - Before timespan
     *   - Exactly at start boundary
     *   - Within timespan
     *   - Exactly at end boundary
     *   - After timespan
     *   - Mixed (some in, some out)
     * - Boundary conditions: inclusive endpoints per Timespan spec
     *
     * Output space partitioning:
     * - Empty list (no tweets in timespan)
     * - Single tweet (one match)
     * - Multiple tweets (multiple matches, preserving order)
     * - All tweets (all within timespan)
     *
     * Edge cases to test:
     * - Point timespan (start == end) with tweet exactly at that time
     * - Tweets exactly at start/end boundaries (should be included)
     * - Order preservation when multiple matches
     * - No modification of original list
     *
     *
     * Testing strategy for Filter.containing():
     *
     * Input space partitioning:
     * - tweets list: empty, single tweet, multiple tweets
     * - words list: empty, single word, multiple words
     * - Word matching conditions:
     *   - No matches (tweet contains none of the words)
     *   - Single word match
     *   - Multiple word matches in same tweet
     *   - Partial word matches (should not match)
     * - Case sensitivity: words in different cases
     * - Word boundaries:
     *   - Words as complete tokens (bounded by spaces/string ends)
     *   - Words as substrings within larger words (should not match)
     *   - Words with punctuation attached
     * - Special characters: punctuation, numbers, symbols
     *
     * Output space partitioning:
     * - Empty list (no tweets contain any words)
     * - Single tweet (one match)
     * - Multiple tweets (multiple matches, preserving order)
     * - All tweets (all contain at least one word)
     *
     * Edge cases to test:
     * - Empty words list (should return empty result)
     * - Case insensitive matching
     * - Word boundary requirements (complete words only)
     * - Order preservation when multiple matches
     * - Words with punctuation/special characters
     * - At least one word matching (OR logic, not AND)
     */

    private static final Instant d1 = Instant.parse("2016-02-17T10:00:00Z");
    private static final Instant d2 = Instant.parse("2016-02-17T11:00:00Z");
    private static final Instant d3 = Instant.parse("2016-02-17T12:00:00Z");
    private static final Instant d4 = Instant.parse("2016-02-17T13:00:00Z");
    private static final Instant d5 = Instant.parse("2016-02-17T14:00:00Z");

    private static final Tweet tweet1 = new Tweet(1, "alyssa", "is it reasonable to talk about rivest so much?", d1);
    private static final Tweet tweet2 = new Tweet(2, "bbitdiddle", "rivest talk in 30 minutes #hype", d2);
    
    @Test(expected=AssertionError.class)
    public void testAssertionsEnabled() {
        assert false; // make sure assertions are enabled with VM argument: -ea
    }

    @Test
    public void testWrittenByEmptyList() {
        // Tests empty tweets list
        List<Tweet> tweets = List.of();
        List<Tweet> result = Filter.writtenBy(tweets, "alice");

        assertTrue("expected empty result from empty input", result.isEmpty());
    }

    @Test
    public void testWrittenByNoMatches() {
        // Tests when no tweets are by the specified user
        Tweet tweet1 = new Tweet(1, "bob", "Hello world", d1);
        Tweet tweet2 = new Tweet(2, "charlie", "Good morning", d2);
        List<Tweet> tweets = Arrays.asList(tweet1, tweet2);

        List<Tweet> result = Filter.writtenBy(tweets, "alice");

        assertTrue("expected empty result when no matches", result.isEmpty());
    }

    @Test
    public void testWrittenBySingleMatch() {
        // Tests single tweet by the specified user
        Tweet tweet1 = new Tweet(1, "alice", "Hello world", d1);
        Tweet tweet2 = new Tweet(2, "bob", "Good morning", d2);
        List<Tweet> tweets = Arrays.asList(tweet1, tweet2);

        List<Tweet> result = Filter.writtenBy(tweets, "alice");

        assertEquals("expected single match", 1, result.size());
        assertEquals("expected correct tweet", tweet1, result.getFirst());
    }

    @Test
    public void testWrittenByMultipleMatches() {
        // Tests multiple tweets by the same user, verifying order preservation
        Tweet tweet1 = new Tweet(1, "alice", "First tweet", d1);
        Tweet tweet2 = new Tweet(2, "bob", "Bob's tweet", d2);
        Tweet tweet3 = new Tweet(3, "alice", "Second tweet", d3);
        Tweet tweet4 = new Tweet(4, "alice", "Third tweet", d4);
        List<Tweet> tweets = Arrays.asList(tweet1, tweet2, tweet3, tweet4);

        List<Tweet> result = Filter.writtenBy(tweets, "alice");

        assertEquals("expected three matches", 3, result.size());
        assertEquals("expected first tweet in order", tweet1, result.get(0));
        assertEquals("expected second tweet in order", tweet3, result.get(1));
        assertEquals("expected third tweet in order", tweet4, result.get(2));
    }

    @Test
    public void testWrittenByCaseInsensitive() {
        // Tests case-insensitive matching (Twitter usernames are case-insensitive)
        Tweet tweet1 = new Tweet(1, "Alice", "Hello world", d1);
        Tweet tweet2 = new Tweet(2, "ALICE", "Second tweet", d2);
        Tweet tweet3 = new Tweet(3, "bob", "Bob's tweet", d3);
        List<Tweet> tweets = Arrays.asList(tweet1, tweet2, tweet3);

        List<Tweet> result = Filter.writtenBy(tweets, "alice");

        assertEquals("expected two matches despite case differences", 2, result.size());
        assertEquals("expected first match", tweet1, result.get(0));
        assertEquals("expected second match", tweet2, result.get(1));
    }

    @Test
    public void testWrittenByCaseInsensitiveParameter() {
        // Tests case-insensitive matching with different case in parameter
        Tweet tweet1 = new Tweet(1, "alice", "Hello world", d1);
        Tweet tweet2 = new Tweet(2, "bob", "Bob's tweet", d2);
        List<Tweet> tweets = Arrays.asList(tweet1, tweet2);

        List<Tweet> result = Filter.writtenBy(tweets, "ALICE");

        assertEquals("expected match with different case parameter", 1, result.size());
        assertEquals("expected correct tweet", tweet1, result.getFirst());
    }

    @Test
    public void testWrittenByValidUsernameCharacters() {
        // Tests usernames with valid characters (letters, digits, underscore, hyphen)
        Tweet tweet1 = new Tweet(1, "user_123", "Tweet with underscore", d1);
        Tweet tweet2 = new Tweet(2, "test-user", "Tweet with hyphen", d2);
        Tweet tweet3 = new Tweet(3, "bob", "Regular tweet", d3);
        List<Tweet> tweets = Arrays.asList(tweet1, tweet2, tweet3);

        List<Tweet> result1 = Filter.writtenBy(tweets, "user_123");
        List<Tweet> result2 = Filter.writtenBy(tweets, "test-user");

        assertEquals("expected match for username with underscore", 1, result1.size());
        assertEquals("expected correct tweet with underscore", tweet1, result1.getFirst());

        assertEquals("expected match for username with hyphen", 1, result2.size());
        assertEquals("expected correct tweet with hyphen", tweet2, result2.getFirst());
    }

    @Test
    public void testWrittenByOriginalListUnmodified() {
        // Tests that original list is not modified
        Tweet tweet1 = new Tweet(1, "alice", "Hello world", d1);
        Tweet tweet2 = new Tweet(2, "bob", "Good morning", d2);
        List<Tweet> originalTweets = new ArrayList<>(Arrays.asList(tweet1, tweet2));
        List<Tweet> originalCopy = new ArrayList<>(originalTweets);

        Filter.writtenBy(originalTweets, "alice");

        assertEquals("expected original list unchanged", originalCopy, originalTweets);
    }

    @Test
    public void testWrittenByAllTweetsByUser() {
        // Tests when all tweets are by the specified user
        Tweet tweet1 = new Tweet(1, "alice", "First tweet", d1);
        Tweet tweet2 = new Tweet(2, "alice", "Second tweet", d2);
        Tweet tweet3 = new Tweet(3, "alice", "Third tweet", d3);
        List<Tweet> tweets = Arrays.asList(tweet1, tweet2, tweet3);

        List<Tweet> result = Filter.writtenBy(tweets, "alice");

        assertEquals("expected all tweets when all by user", tweets.size(), result.size());
        assertEquals("expected same content and order", tweets, result);
    }

    @Test
    public void testWrittenBySingleTweetList() {
        // Tests single tweet list with matching user
        Tweet tweet = new Tweet(1, "alice", "Only tweet", d1);
        List<Tweet> tweets = List.of(tweet);

        List<Tweet> result = Filter.writtenBy(tweets, "alice");

        assertEquals("expected single match", 1, result.size());
        assertEquals("expected correct tweet", tweet, result.getFirst());
    }

    @Test
    public void testWrittenBySingleTweetListNoMatch() {
        // Tests single tweet list with non-matching user
        Tweet tweet = new Tweet(1, "bob", "Only tweet", d1);
        List<Tweet> tweets = List.of(tweet);

        List<Tweet> result = Filter.writtenBy(tweets, "alice");

        assertTrue("expected empty result when single tweet doesn't match", result.isEmpty());
    }

    @Test
    public void testWrittenByMixedCaseUsernames() {
        // Tests comprehensive case-insensitive matching
        Tweet tweet1 = new Tweet(1, "Alice", "Tweet 1", d1);
        Tweet tweet2 = new Tweet(2, "ALICE", "Tweet 2", d2);
        Tweet tweet3 = new Tweet(3, "alice", "Tweet 3", d3);
        Tweet tweet4 = new Tweet(4, "AlIcE", "Tweet 4", d4);
        List<Tweet> tweets = Arrays.asList(tweet1, tweet2, tweet3, tweet4);

        List<Tweet> result = Filter.writtenBy(tweets, "aLiCe");

        assertEquals("expected all four matches despite case variations", 4, result.size());
        // Verify order is preserved
        assertEquals("expected correct order", Arrays.asList(tweet1, tweet2, tweet3, tweet4), result);
    }

    @Test
    public void testInTimespanEmptyList() {
        // Tests empty tweets list
        List<Tweet> tweets = List.of();
        Timespan timespan = new Timespan(d2, d4);

        List<Tweet> result = Filter.inTimespan(tweets, timespan);

        assertTrue("expected empty result from empty input", result.isEmpty());
    }

    @Test
    public void testInTimespanNoMatches() {
        // Tests when no tweets are within timespan
        Tweet tweet1 = new Tweet(1, "alice", "Early tweet", d1); // Before timespan
        Tweet tweet2 = new Tweet(2, "bob", "Late tweet", d5);    // After timespan
        List<Tweet> tweets = Arrays.asList(tweet1, tweet2);
        Timespan timespan = new Timespan(d2, d4); // d2 to d4

        List<Tweet> result = Filter.inTimespan(tweets, timespan);

        assertTrue("expected empty result when no tweets in timespan", result.isEmpty());
    }

    @Test
    public void testInTimespanSingleMatch() {
        // Tests single tweet within timespan
        Tweet tweet1 = new Tweet(1, "alice", "Early tweet", d1);  // Before
        Tweet tweet2 = new Tweet(2, "bob", "In timespan", d3);    // Within
        Tweet tweet3 = new Tweet(3, "charlie", "Late tweet", d5); // After
        List<Tweet> tweets = Arrays.asList(tweet1, tweet2, tweet3);
        Timespan timespan = new Timespan(d2, d4);

        List<Tweet> result = Filter.inTimespan(tweets, timespan);

        assertEquals("expected single match", 1, result.size());
        assertEquals("expected correct tweet", tweet2, result.getFirst());
    }

    @Test
    public void testInTimespanMultipleMatches() {
        // Tests multiple tweets within timespan, verifying order preservation
        Tweet tweet1 = new Tweet(1, "alice", "Early tweet", d1);    // Before
        Tweet tweet2 = new Tweet(2, "bob", "First in span", d2);    // At start
        Tweet tweet3 = new Tweet(3, "charlie", "Second in span", d3); // Within
        Tweet tweet4 = new Tweet(4, "dave", "Third in span", d4);   // At end
        Tweet tweet5 = new Tweet(5, "eve", "Late tweet", d5);       // After
        List<Tweet> tweets = Arrays.asList(tweet1, tweet2, tweet3, tweet4, tweet5);
        Timespan timespan = new Timespan(d2, d4);

        List<Tweet> result = Filter.inTimespan(tweets, timespan);

        assertEquals("expected three matches", 3, result.size());
        assertEquals("expected first tweet in order", tweet2, result.get(0));
        assertEquals("expected second tweet in order", tweet3, result.get(1));
        assertEquals("expected third tweet in order", tweet4, result.get(2));
    }

    @Test
    public void testInTimespanStartBoundaryInclusive() {
        // Tests that tweet exactly at start time is included
        Tweet tweet = new Tweet(1, "alice", "At start boundary", d2);
        List<Tweet> tweets = List.of(tweet);
        Timespan timespan = new Timespan(d2, d4);

        List<Tweet> result = Filter.inTimespan(tweets, timespan);

        assertEquals("expected tweet at start boundary to be included", 1, result.size());
        assertEquals("expected correct tweet", tweet, result.getFirst());
    }

    @Test
    public void testInTimespanEndBoundaryInclusive() {
        // Tests that tweet exactly at end time is included
        Tweet tweet = new Tweet(1, "alice", "At end boundary", d4);
        List<Tweet> tweets = List.of(tweet);
        Timespan timespan = new Timespan(d2, d4);

        List<Tweet> result = Filter.inTimespan(tweets, timespan);

        assertEquals("expected tweet at end boundary to be included", 1, result.size());
        assertEquals("expected correct tweet", tweet, result.getFirst());
    }

    @Test
    public void testInTimespanPointTimespan() {
        // Tests point timespan where start == end
        Tweet tweet1 = new Tweet(1, "alice", "Before point", d2);
        Tweet tweet2 = new Tweet(2, "bob", "At point", d3);
        Tweet tweet3 = new Tweet(3, "charlie", "After point", d4);
        List<Tweet> tweets = Arrays.asList(tweet1, tweet2, tweet3);
        Timespan pointTimespan = new Timespan(d3, d3); // Point timespan at d3

        List<Tweet> result = Filter.inTimespan(tweets, pointTimespan);

        assertEquals("expected single match for point timespan", 1, result.size());
        assertEquals("expected tweet exactly at point time", tweet2, result.getFirst());
    }

    @Test
    public void testInTimespanAllTweetsInSpan() {
        // Tests when all tweets are within timespan
        Tweet tweet1 = new Tweet(1, "alice", "First tweet", d2);
        Tweet tweet2 = new Tweet(2, "bob", "Second tweet", d3);
        Tweet tweet3 = new Tweet(3, "charlie", "Third tweet", d4);
        List<Tweet> tweets = Arrays.asList(tweet1, tweet2, tweet3);
        Timespan timespan = new Timespan(d1, d5); // Encompasses all tweets

        List<Tweet> result = Filter.inTimespan(tweets, timespan);

        assertEquals("expected all tweets when all in timespan", tweets.size(), result.size());
        assertEquals("expected same content and order", tweets, result);
    }

    @Test
    public void testInTimespanOriginalListUnmodified() {
        // Tests that original list is not modified
        Tweet tweet1 = new Tweet(1, "alice", "Tweet 1", d2);
        Tweet tweet2 = new Tweet(2, "bob", "Tweet 2", d3);
        List<Tweet> originalTweets = new ArrayList<>(Arrays.asList(tweet1, tweet2));
        List<Tweet> originalCopy = new ArrayList<>(originalTweets);
        Timespan timespan = new Timespan(d2, d4);

        Filter.inTimespan(originalTweets, timespan);

        assertEquals("expected original list unchanged", originalCopy, originalTweets);
    }

    @Test
    public void testInTimespanJustBeforeStart() {
        // Tests tweet just before start boundary (should not be included)
        Instant justBeforeD2 = d2.minusSeconds(1);
        Tweet tweet = new Tweet(1, "alice", "Just before start", justBeforeD2);
        List<Tweet> tweets = List.of(tweet);
        Timespan timespan = new Timespan(d2, d4);

        List<Tweet> result = Filter.inTimespan(tweets, timespan);

        assertTrue("expected no match for tweet just before start", result.isEmpty());
    }

    @Test
    public void testInTimespanJustAfterEnd() {
        // Tests tweet just after end boundary (should not be included)
        Instant justAfterD4 = d4.plusSeconds(1);
        Tweet tweet = new Tweet(1, "alice", "Just after end", justAfterD4);
        List<Tweet> tweets = List.of(tweet);
        Timespan timespan = new Timespan(d2, d4);

        List<Tweet> result = Filter.inTimespan(tweets, timespan);

        assertTrue("expected no match for tweet just after end", result.isEmpty());
    }

    @Test
    public void testInTimespanSingleTweetInSpan() {
        // Tests single tweet list with tweet in timespan
        Tweet tweet = new Tweet(1, "alice", "Only tweet", d3);
        List<Tweet> tweets = List.of(tweet);
        Timespan timespan = new Timespan(d2, d4);

        List<Tweet> result = Filter.inTimespan(tweets, timespan);

        assertEquals("expected single match", 1, result.size());
        assertEquals("expected correct tweet", tweet, result.getFirst());
    }

    @Test
    public void testInTimespanSingleTweetOutsideSpan() {
        // Tests single tweet list with tweet outside timespan
        Tweet tweet = new Tweet(1, "alice", "Only tweet", d1);
        List<Tweet> tweets = List.of(tweet);
        Timespan timespan = new Timespan(d2, d4);

        List<Tweet> result = Filter.inTimespan(tweets, timespan);

        assertTrue("expected no match when single tweet outside timespan", result.isEmpty());
    }

    @Test
    public void testInTimespanMixedTimestamps() {
        // Tests comprehensive mix of timestamps relative to timespan
        Tweet before = new Tweet(1, "alice", "Before", d1);
        Tweet atStart = new Tweet(2, "bob", "At start", d2);
        Tweet within1 = new Tweet(3, "charlie", "Within 1", d3);
        Tweet within2 = new Tweet(4, "dave", "Within 2", d3.plusSeconds(30));
        Tweet atEnd = new Tweet(5, "eve", "At end", d4);
        Tweet after = new Tweet(6, "frank", "After", d5);

        List<Tweet> tweets = Arrays.asList(before, atStart, within1, within2, atEnd, after);
        Timespan timespan = new Timespan(d2, d4);

        List<Tweet> result = Filter.inTimespan(tweets, timespan);

        assertEquals("expected four matches", 4, result.size());
        assertEquals("expected correct order",
                Arrays.asList(atStart, within1, within2, atEnd), result);
    }

    @Test
    public void testInTimespanVeryShortTimespan() {
        // Tests very short timespan (1 second)
        Instant start = d3;
        Instant end = d3.plusSeconds(1);
        Tweet exactStart = new Tweet(1, "alice", "At start", start);
        Tweet exactEnd = new Tweet(2, "bob", "At end", end);
        Tweet between = new Tweet(3, "charlie", "Between", start.plusMillis(500));
        Tweet outside = new Tweet(4, "dave", "Outside", end.plusSeconds(1));

        List<Tweet> tweets = Arrays.asList(exactStart, exactEnd, between, outside);
        Timespan shortTimespan = new Timespan(start, end);

        List<Tweet> result = Filter.inTimespan(tweets, shortTimespan);

        assertEquals("expected three matches in short timespan", 3, result.size());
        assertEquals("expected correct tweets",
                Arrays.asList(exactStart, exactEnd, between), result);
    }

    @Test
    public void testContainingEmptyTweetsList() {
        // Tests empty tweets list
        List<Tweet> tweets = List.of();
        List<String> words = List.of("hello");

        List<Tweet> result = Filter.containing(tweets, words);

        assertTrue("expected empty result from empty tweets", result.isEmpty());
    }

    @Test
    public void testContainingEmptyWordsList() {
        // Tests empty words list - should return empty result
        Tweet tweet = new Tweet(1, "alice", "Hello world", d1);
        List<Tweet> tweets = List.of(tweet);
        List<String> words = List.of();

        List<Tweet> result = Filter.containing(tweets, words);

        assertTrue("expected empty result from empty words list", result.isEmpty());
    }

    @Test
    public void testContainingNoMatches() {
        // Tests when no tweets contain any of the words
        Tweet tweet1 = new Tweet(1, "alice", "Hello world", d1);
        Tweet tweet2 = new Tweet(2, "bob", "Good morning", d2);
        List<Tweet> tweets = Arrays.asList(tweet1, tweet2);
        List<String> words = Arrays.asList("python", "java");

        List<Tweet> result = Filter.containing(tweets, words);

        assertTrue("expected empty result when no matches", result.isEmpty());
    }

    @Test
    public void testContainingSingleWordSingleMatch() {
        // Tests single word matching single tweet
        Tweet tweet1 = new Tweet(1, "alice", "Hello world", d1);
        Tweet tweet2 = new Tweet(2, "bob", "Good morning", d2);
        List<Tweet> tweets = Arrays.asList(tweet1, tweet2);
        List<String> words = List.of("world");

        List<Tweet> result = Filter.containing(tweets, words);

        assertEquals("expected single match", 1, result.size());
        assertEquals("expected correct tweet", tweet1, result.getFirst());
    }

    @Test
    public void testContainingMultipleWordsSingleMatch() {
        // Tests multiple words where only one matches
        Tweet tweet1 = new Tweet(1, "alice", "Hello world", d1);
        Tweet tweet2 = new Tweet(2, "bob", "Good morning", d2);
        List<Tweet> tweets = Arrays.asList(tweet1, tweet2);
        List<String> words = Arrays.asList("world", "python", "java");

        List<Tweet> result = Filter.containing(tweets, words);

        assertEquals("expected single match", 1, result.size());
        assertEquals("expected correct tweet", tweet1, result.getFirst());
    }

    @Test
    public void testContainingMultipleMatches() {
        // Tests multiple tweets matching, verifying order preservation
        Tweet tweet1 = new Tweet(1, "alice", "I love java", d1);
        Tweet tweet2 = new Tweet(2, "bob", "Good morning", d2);
        Tweet tweet3 = new Tweet(3, "charlie", "Python is great", d3);
        Tweet tweet4 = new Tweet(4, "dave", "Learning java today", d4);
        List<Tweet> tweets = Arrays.asList(tweet1, tweet2, tweet3, tweet4);
        List<String> words = Arrays.asList("java", "python");

        List<Tweet> result = Filter.containing(tweets, words);

        assertEquals("expected three matches", 3, result.size());
        assertEquals("expected first match", tweet1, result.get(0));
        assertEquals("expected second match", tweet3, result.get(1));
        assertEquals("expected third match", tweet4, result.get(2));
    }

    @Test
    public void testContainingCaseInsensitive() {
        // Tests case-insensitive word matching
        Tweet tweet1 = new Tweet(1, "alice", "Hello WORLD", d1);
        Tweet tweet2 = new Tweet(2, "bob", "good Morning", d2);
        Tweet tweet3 = new Tweet(3, "charlie", "PYTHON rules", d3);
        List<Tweet> tweets = Arrays.asList(tweet1, tweet2, tweet3);
        List<String> words = Arrays.asList("world", "PYTHON");

        List<Tweet> result = Filter.containing(tweets, words);

        assertEquals("expected two matches despite case differences", 2, result.size());
        assertEquals("expected first match", tweet1, result.get(0));
        assertEquals("expected second match", tweet3, result.get(1));
    }

    @Test
    public void testContainingWordBoundaries() {
        // Tests that only complete words match (not substrings)
        Tweet tweet1 = new Tweet(1, "alice", "I love javascript", d1);
        Tweet tweet2 = new Tweet(2, "bob", "Java is great", d2);
        Tweet tweet3 = new Tweet(3, "charlie", "javanese culture", d3);
        List<Tweet> tweets = Arrays.asList(tweet1, tweet2, tweet3);
        List<String> words = List.of("java");

        List<Tweet> result = Filter.containing(tweets, words);

        assertEquals("expected only exact word matches", 1, result.size());
        assertEquals("expected correct tweet", tweet2, result.getFirst());
    }

    @Test
    public void testContainingMultipleWordsInSameTweet() {
        // Tests tweet containing multiple search words (should still count as one match)
        Tweet tweet1 = new Tweet(1, "alice", "I love both java and python", d1);
        Tweet tweet2 = new Tweet(2, "bob", "Good morning", d2);
        List<Tweet> tweets = Arrays.asList(tweet1, tweet2);
        List<String> words = Arrays.asList("java", "python");

        List<Tweet> result = Filter.containing(tweets, words);

        assertEquals("expected single tweet despite multiple word matches", 1, result.size());
        assertEquals("expected correct tweet", tweet1, result.getFirst());
    }

    @Test
    public void testContainingWordsAtStringBoundaries() {
        // Tests words at start and end of tweet text
        Tweet tweet1 = new Tweet(1, "alice", "java is awesome", d1);
        Tweet tweet2 = new Tweet(2, "bob", "I love python", d2);
        Tweet tweet3 = new Tweet(3, "charlie", "coding", d3);
        List<Tweet> tweets = Arrays.asList(tweet1, tweet2, tweet3);
        List<String> words = Arrays.asList("java", "python", "coding");

        List<Tweet> result = Filter.containing(tweets, words);

        assertEquals("expected all tweets to match", 3, result.size());
        assertEquals("expected correct order", tweets, result);
    }

    @Test
    public void testContainingOriginalListUnmodified() {
        // Tests that original lists are not modified
        Tweet tweet1 = new Tweet(1, "alice", "Hello world", d1);
        Tweet tweet2 = new Tweet(2, "bob", "Good morning", d2);
        List<Tweet> originalTweets = new ArrayList<>(Arrays.asList(tweet1, tweet2));
        List<String> originalWords = new ArrayList<>(List.of("world"));
        List<Tweet> tweetsCopy = new ArrayList<>(originalTweets);
        List<String> wordsCopy = new ArrayList<>(originalWords);

        Filter.containing(originalTweets, originalWords);

        assertEquals("expected tweets list unchanged", tweetsCopy, originalTweets);
        assertEquals("expected words list unchanged", wordsCopy, originalWords);
    }

    @Test
    public void testContainingSingleCharacterWords() {
        // Tests single character words
        Tweet tweet1 = new Tweet(1, "alice", "I am a programmer", d1);
        Tweet tweet2 = new Tweet(2, "bob", "Good morning", d2);
        List<Tweet> tweets = Arrays.asList(tweet1, tweet2);
        List<String> words = Arrays.asList("I", "a");

        List<Tweet> result = Filter.containing(tweets, words);

        assertEquals("expected match for single character words", 1, result.size());
        assertEquals("expected correct tweet", tweet1, result.getFirst());
    }

    @Test
    public void testContainingWordsWithNumbers() {
        // Tests words containing numbers
        Tweet tweet1 = new Tweet(1, "alice", "Java8 is great", d1);
        Tweet tweet2 = new Tweet(2, "bob", "Python3 rocks", d2);
        Tweet tweet3 = new Tweet(3, "charlie", "Learning programming", d3);
        List<Tweet> tweets = Arrays.asList(tweet1, tweet2, tweet3);
        List<String> words = Arrays.asList("Java8", "Python3");

        List<Tweet> result = Filter.containing(tweets, words);

        assertEquals("expected matches for words with numbers", 2, result.size());
        assertEquals("expected first match", tweet1, result.get(0));
        assertEquals("expected second match", tweet2, result.get(1));
    }

    @Test
    public void testContainingSpecialCharacters() {
        // Tests words with special characters (non-space)
        Tweet tweet1 = new Tweet(1, "alice", "Check out @username", d1);
        Tweet tweet2 = new Tweet(2, "bob", "Cost is $100", d2);
        Tweet tweet3 = new Tweet(3, "charlie", "Email: test@example.com", d3);
        List<Tweet> tweets = Arrays.asList(tweet1, tweet2, tweet3);
        List<String> words = Arrays.asList("@username", "$100");

        List<Tweet> result = Filter.containing(tweets, words);

        assertEquals("expected matches for words with special characters", 2, result.size());
        assertEquals("expected first match", tweet1, result.get(0));
        assertEquals("expected second match", tweet2, result.get(1));
    }

    @Test
    public void testContainingAllTweetsMatch() {
        // Tests when all tweets contain at least one word
        Tweet tweet1 = new Tweet(1, "alice", "I love java", d1);
        Tweet tweet2 = new Tweet(2, "bob", "Python is great", d2);
        Tweet tweet3 = new Tweet(3, "charlie", "Learning java programming", d3);
        List<Tweet> tweets = Arrays.asList(tweet1, tweet2, tweet3);
        List<String> words = Arrays.asList("java", "python");

        List<Tweet> result = Filter.containing(tweets, words);

        assertEquals("expected all tweets when all match", tweets.size(), result.size());
        assertEquals("expected same content and order", tweets, result);
    }

    @Test
    public void testContainingWhitespaceHandling() {
        // Tests handling of multiple spaces and edge whitespace cases
        Tweet tweet1 = new Tweet(1, "alice", "  hello   world  ", d1);
        Tweet tweet2 = new Tweet(2, "bob", "java\tpython", d2); // tab character
        List<Tweet> tweets = Arrays.asList(tweet1, tweet2);
        List<String> words = Arrays.asList("hello", "java");

        List<Tweet> result = Filter.containing(tweets, words);

        assertEquals("expected matches despite whitespace variations", 2, result.size());
        assertEquals("expected both tweets to match", tweets, result);
    }

    @Test
    public void testContainingExactWordMatch() {
        // Tests that search is for exact words, not partial matches
        Tweet tweet1 = new Tweet(1, "alice", "The cat is happy", d1);
        Tweet tweet2 = new Tweet(2, "bob", "I love cats", d2);
        Tweet tweet3 = new Tweet(3, "charlie", "Catch the ball", d3);
        List<Tweet> tweets = Arrays.asList(tweet1, tweet2, tweet3);
        List<String> words = List.of("cat");

        List<Tweet> result = Filter.containing(tweets, words);

        assertEquals("expected only exact word matches", 1, result.size());
        assertEquals("expected correct tweet", tweet1, result.getFirst());
    }

    /*
     * Warning: all the tests you write here must be runnable against any Filter
     * class that follows the spec. It will be run against several staff
     * implementations of Filter, which will be done by overwriting
     * (temporarily) your version of Filter with the staff's version.
     * DO NOT strengthen the spec of Filter or its methods.
     * 
     * In particular, your test cases must not call helper methods of your own
     * that you have put in Filter, because that means you're testing a stronger
     * spec than Filter says. If you need such helper methods, define them in a
     * different class. If you only need them in this test class, then keep them
     * in this test class.
     */

}
