/* Copyright (c) 2007-2016 MIT 6.005 course staff, all rights reserved.
 * Redistribution of original or derived work requires permission of course staff.
 */
package twitter;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Extract consists of methods that extract information from a list of tweets.
 * <p>
 * DO NOT change the method signatures and specifications of these methods, but
 * you should implement their method bodies, and you may add new public or
 * private methods or classes if you like.
 */
public class Extract {

    /**
     * Get the time period spanned by tweets.
     * 
     * @param tweets
     *            list of tweets with distinct ids, not modified by this method.
     * @return a minimum-length time interval that contains the timestamp of
     *         every tweet in the list.
     */
    public static Timespan getTimespan(List<Tweet> tweets) {
        if (tweets == null || tweets.isEmpty()) {
            throw new IllegalArgumentException("tweets is empty or null");
        }

        Instant start = null;
        Instant end = null;
        for (Tweet tweet : tweets) {
            Instant timestamp = tweet.getTimestamp();
            if (start == null || timestamp.isBefore(start)) {
                start = timestamp;
            }
            if (end == null || timestamp.isAfter(end)) {
                end = timestamp;
            }
        }

        return new Timespan(start, end);
    }

    /**
     * Get usernames mentioned in a list of tweets.
     * 
     * @param tweets
     *            list of tweets with distinct ids, not modified by this method.
     * @return the set of usernames who are mentioned in the text of the tweets.
     *         A username-mention is "@" followed by a Twitter username (as
     *         defined by Tweet.getAuthor()'s spec).
     *         The username-mention cannot be immediately preceded or followed by any
     *         character valid in a Twitter username.
     *         For this reason, an email address like bitdiddle@mit.edu does NOT 
     *         contain a mention of the username mit.
     *         Twitter usernames are case-insensitive, and the returned set may
     *         include a username at most once.
     */
    public static Set<String> getMentionedUsers(List<Tweet> tweets) {
        Set<String> mentionedUsers = new HashSet<>();

        // Regular expression to match valid mentions
        // (?<![A-Za-z0-9_-]) - negative lookbehind: not preceded by valid username char
        // @ - literal @ symbol
        // ([A-Za-z0-9_-]+) - capture group: one or more valid username characters
        // (?![A-Za-z0-9_-]) - negative lookahead: not followed by valid username char
        Pattern mentionPattern = Pattern.compile("(?<![A-Za-z0-9_-])@([A-Za-z0-9_-]+)(?![A-Za-z0-9_-])");

        for (Tweet tweet : tweets) {
            String text = tweet.getText();
            Matcher matcher = mentionPattern.matcher(text);

            while (matcher.find()) {
                String mentionedUser = matcher.group(1).toLowerCase(); // Normalize to lowercase
                mentionedUsers.add(mentionedUser);
            }
        }
        return mentionedUsers;
    }

    /**
     * Extract hashtags from a list of tweets.
     *
     * @param tweets
     *            list of tweets with distinct ids, not modified by this method.
     * @return the set of hashtags that appear in the text of the tweets.
     *         A hashtag is "#" followed by a sequence of letters, digits, and underscores.
     *         Hashtags are case-insensitive, and the returned set may include a hashtag
     *         at most once.
     */
    public static Set<String> getHashtags(List<Tweet> tweets) {
        Set<String> hashtags = new HashSet<>();

        // Regular expression to match hashtags: # followed by word characters
        Pattern hashtagPattern = Pattern.compile("#([A-Za-z0-9_]+)");

        for (Tweet tweet : tweets) {
            String text = tweet.getText();
            Matcher matcher = hashtagPattern.matcher(text);

            while (matcher.find()) {
                String hashtag = matcher.group(1).toLowerCase(); // Remove # and normalize case
                hashtags.add(hashtag);
            }
        }

        return hashtags;
    }
}
