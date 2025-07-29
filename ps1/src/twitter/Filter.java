/* Copyright (c) 2007-2016 MIT 6.005 course staff, all rights reserved.
 * Redistribution of original or derived work requires permission of course staff.
 */
package twitter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Filter consists of methods that filter a list of tweets for those matching a
 * condition.
 * <p>
 * DO NOT change the method signatures and specifications of these methods, but
 * you should implement their method bodies, and you may add new public or
 * private methods or classes if you like.
 */
public class Filter {

    /**
     * Find tweets written by a particular user.
     * 
     * @param tweets
     *            a list of tweets with distinct ids, not modified by this method.
     * @param username
     *            Twitter username, required to be a valid Twitter username as
     *            defined by Tweet.getAuthor()'s spec.
     * @return all and only the tweets in the list whose author is username,
     *         in the same order as in the input list.
     */
    public static List<Tweet> writtenBy(List<Tweet> tweets, String username) {
        List<Tweet> result = new ArrayList<Tweet>();

        String lowerUsername = username.toLowerCase();

        for (Tweet tweet : tweets) {
            if (tweet.getAuthor().toLowerCase().equals(lowerUsername)) {
                result.add(tweet);
            }
        }

        return result;
    }

    /**
     * Find tweets that were sent during a particular timespan.
     * 
     * @param tweets
     *            a list of tweets with distinct ids, not modified by this method.
     * @param timespan
     *            timespan
     * @return all and only the tweets in the list that were sent during the timespan,
     *         in the same order as in the input list.
     */
    public static List<Tweet> inTimespan(List<Tweet> tweets, Timespan timespan) {
        List<Tweet> result = new ArrayList<Tweet>();

        for (Tweet tweet : tweets) {
            Instant tweetTime = tweet.getTimestamp();

            if (!tweetTime.isBefore(timespan.getStart()) && !tweetTime.isAfter(timespan.getEnd())) {
                result.add(tweet);
            }
        }

        return result;
    }

    /**
     * Find tweets that contain certain words.
     * 
     * @param tweets
     *            a list of tweets with distinct ids, not modified by this method.
     * @param words
     *            a list of words to search for in the tweets. 
     *            A word is a nonempty sequence of nonspace characters.
     * @return all and only the tweets in the list such that the tweet text (when 
     *         represented as a sequence of nonempty words bounded by space characters 
     *         and the ends of the string) includes *at least one* of the words 
     *         found in the words list. Word comparison is not case-sensitive,
     *         so "Obama" is the same as "obama".  The returned tweets are in the
     *         same order as in the input list.
     */
    public static List<Tweet> containing(List<Tweet> tweets, List<String> words) {
        // Handle empty words list
        if (words.isEmpty()) {
            return new ArrayList<>();
        }

        List<Tweet> result = new ArrayList<>();

        // Convert words to lowercase and store in a set for quick lookup
        Set<String> searchWords = new HashSet<>();
        for (String word : words) {
            searchWords.add(word.toLowerCase());
        }

        for (Tweet tweet : tweets) {
            if (tweetContainsAnyWord(tweet.getText(), searchWords)) {
                result.add(tweet);
            }
        }

        return result;
    }

    /**
     * Helper method to check if tweet text contains any of the search words
     * @param tweetText the text of the tweet
     * @param searchWords set of lowercase search words
     * @return true if tweet contains at least one search word
     */
    private static boolean tweetContainsAnyWord(String tweetText, Set<String> searchWords) {
        // Split tweet text into words (split on whitespace)
        // A word is defined as a nonempty sequence of nonspace characters
        String[] tweetWords = tweetText.trim().split("\\s+");

        for (String tweetWord : tweetWords) {
            // Skip empty strings (shouldn't happen with split, but defensive)
            if (!tweetWord.isEmpty() && searchWords.contains(tweetWord.toLowerCase())) {
                return true;
            }
        }

        return false;
    }
}
