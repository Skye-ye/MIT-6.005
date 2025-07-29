/* Copyright (c) 2007-2016 MIT 6.005 course staff, all rights reserved.
 * Redistribution of original or derived work requires permission of course staff.
 */
package twitter;

import java.util.*;

/**
 * SocialNetwork provides methods that operate on a social network.
 * <p>
 * A social network is represented by a Map<String, Set<String>> where map[A] is
 * the set of people that person A follows on Twitter, and all people are
 * represented by their Twitter usernames. Users can't follow themselves. If A
 * doesn't follow anybody, then map[A] may be the empty set, or A may not even exist
 * as a key in the map; this is true even if A is followed by other people in the network.
 * Twitter usernames are not case-sensitive, so "ernie" is the same as "ERNie".
 * A username should appear at most once as a key in the map or in any given
 * map[A] set.
 * <p>
 * DO NOT change the method signatures and specifications of these methods, but
 * you should implement their method bodies, and you may add new public or
 * private methods or classes if you like.
 */
public class SocialNetwork {

    /**
     * Guess who might follow whom, from evidence found in tweets.
     * 
     * @param tweets
     *            a list of tweets providing the evidence, not modified by this
     *            method.
     * @return a social network (as defined above) in which Ernie follows Bert
     *         if and only if there is evidence for it in the given list of
     *         tweets.
     *         One kind of evidence that Ernie follows Bert is if Ernie
     *         @-mentions Bert in a tweet. This must be implemented. Other kinds
     *         of evidence may be used at the implementor's discretion.
     *         All the Twitter usernames in the returned social network must be
     *         either authors or @-mentions in the list of tweets.
     */
    public static Map<String, Set<String>> guessFollowsGraph(List<Tweet> tweets) {
        Map<String, Set<String>> followsGraph = new HashMap<>();

        // First, implement required @-mention evidence
        addMentionEvidence(tweets, followsGraph);

        // Second, add hashtag evidence
        addHashtagEvidence(tweets, followsGraph);

        return followsGraph;
    }

    /**
     * Adds follow relationships based on @-mentions (required evidence)
     */
    private static void addMentionEvidence(List<Tweet> tweets, Map<String, Set<String>> followsGraph) {
        for (Tweet tweet : tweets) {
            String author = tweet.getAuthor().toLowerCase();

            // Ensure author is in the graph
            followsGraph.putIfAbsent(author, new HashSet<>());

            // Get all mentioned users in this tweet
            Set<String> mentionedUsers =
                    Extract.getMentionedUsers(List.of(tweet));

            for (String mentionedUser : mentionedUsers) {
                // Ensure mentioned user is in the graph
                followsGraph.putIfAbsent(mentionedUser, new HashSet<>());

                // Create follow relationship: author follows mentioned user
                if (!author.equals(mentionedUser)) {
                    followsGraph.get(author).add(mentionedUser);
                }
            }
        }
    }

    /**
     * Adds follow relationships based on common hashtag usage
     */
    private static void addHashtagEvidence(List<Tweet> tweets, Map<String, Set<String>> followsGraph) {
        // Map hashtag -> set of users who used it
        Map<String, Set<String>> hashtagUsers = new HashMap<>();

        // Extract hashtags from all tweets
        for (Tweet tweet : tweets) {
            String author = tweet.getAuthor().toLowerCase();
            Set<String> hashtags = Extract.getHashtags(List.of(tweet));

            for (String hashtag : hashtags) {
                hashtagUsers.putIfAbsent(hashtag, new HashSet<>());
                hashtagUsers.get(hashtag).add(author);
            }
        }

        // Create follow relationships based on shared hashtags
        for (Map.Entry<String, Set<String>> entry : hashtagUsers.entrySet()) {
            Set<String> users = entry.getValue();

            // Only consider hashtags used by 2-4 users (uncommon but not unique)
            // Too common (>4) = weak evidence, too rare (1 user) = no evidence
            if (users.size() >= 2 && users.size() <= 4) {
                createMutualFollowsForHashtag(users, followsGraph);
            }
        }
    }

    /**
     * Creates mutual follow relationships between users who share a hashtag
     */
    private static void createMutualFollowsForHashtag(Set<String> users, Map<String, Set<String>> followsGraph) {
        List<String> userList = new ArrayList<>(users);

        // Create mutual following between all pairs of users who share this hashtag
        for (int i = 0; i < userList.size(); i++) {
            for (int j = i + 1; j < userList.size(); j++) {
                String user1 = userList.get(i);
                String user2 = userList.get(j);

                // Ensure both users are in the graph
                followsGraph.putIfAbsent(user1, new HashSet<>());
                followsGraph.putIfAbsent(user2, new HashSet<>());

                // Create mutual follows
                followsGraph.get(user1).add(user2);
                followsGraph.get(user2).add(user1);
            }
        }
    }


    /**
     * Find the people in a social network who have the greatest influence, in
     * the sense that they have the most followers.
     * 
     * @param followsGraph
     *            a social network (as defined above)
     * @return a list of all distinct Twitter usernames in followsGraph, in
     *         descending order of follower count.
     */
    public static List<String> influencers(Map<String, Set<String>> followsGraph) {
        // Count followers for each user
        Map<String, Integer> followerCounts = new HashMap<>();

        // Initialize all users with 0 followers
        for (String user : followsGraph.keySet()) {
            followerCounts.put(user, 0);
        }

        // Count followers by examining who each user follows
        for (Set<String> following : followsGraph.values()) {
            for (String followedUser : following) {
                // Increment follower count for each user being followed
                followerCounts.merge(followedUser, 1, Integer::sum);
            }
        }

        // Create list of users sorted by follower count (descending)
        List<String> users = new ArrayList<>(followsGraph.keySet());

        users.sort((user1, user2) -> {
            int count1 = followerCounts.get(user1);
            int count2 = followerCounts.get(user2);

            // Sort in descending order of follower count
            int result = Integer.compare(count2, count1);

            // For ties, sort alphabetically to ensure consistent ordering
            if (result == 0) {
                result = user1.compareTo(user2);
            }

            return result;
        });

        return users;
    }

}
