package TelegramBot.LordOfMysteriesTracker.services;


import TelegramBot.LordOfMysteriesTracker.dto.RedditPostDTO;
import TelegramBot.LordOfMysteriesTracker.model.FilterType;
import net.dean.jraw.RedditClient;
import net.dean.jraw.models.Submission;
import net.dean.jraw.models.SubredditSort;
import net.dean.jraw.pagination.DefaultPaginator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class RedditService {

    private final RedditClient redditClient;
    private final Map<String, Instant> lastPostTime = new ConcurrentHashMap<>();
    private final RedditRateLimiterService rateLimiter;

    @Autowired
    public RedditService(RedditClient redditClient, RedditRateLimiterService rateLimiter) {
        this.redditClient = redditClient;
        this.rateLimiter = rateLimiter;
    }

    public List<RedditPostDTO> getNewFilteredPosts(String subreddit, Set<FilterType> userFilters) {

        List<RedditPostDTO> newPosts = getNewPosts(subreddit);

        if (userFilters.contains(FilterType.ALL) || userFilters.isEmpty()) {
            return newPosts;
        }

        Set<String> filters = userFilters.stream().map(FilterType::getType).collect(Collectors.toSet());

        return newPosts.stream().filter(post -> {
            String postFlair = post.getFlair();
            return filters.contains(postFlair.toLowerCase());
        }).collect(Collectors.toList());

    }

    public List<RedditPostDTO> getNewPosts(String subreddit) {
        List<RedditPostDTO> posts = getPostsFromReddit(subreddit, 10);
        Instant lastCheck = lastPostTime.getOrDefault(subreddit, Instant.now().minusSeconds(3600));

        List<RedditPostDTO> newPosts = posts.stream().filter(post -> post.getCreatedAt().isAfter(lastCheck))
                .toList();

        if (!posts.isEmpty()) {
            Instant newPostTime = posts.getFirst().getCreatedAt();
            lastPostTime.put(subreddit, newPostTime);
        }

        return newPosts;
    }



    private List<RedditPostDTO> getPostsFromReddit(String subreddit, int limit) {

        try {
            rateLimiter.waitForRateLimit();
            DefaultPaginator<Submission> paginator = redditClient.subreddit(subreddit)
                    .posts().sorting(SubredditSort.NEW).limit(limit).build();

            List<Submission> submission = paginator.next();

            return submission.stream().map(this::convertToDTO).collect(Collectors.toList());

        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }

    }

    private RedditPostDTO convertToDTO(Submission submission) {
        RedditPostDTO redditPostDTO = new RedditPostDTO();

        redditPostDTO.setId(submission.getId());
        redditPostDTO.setTitle(submission.getTitle());
        redditPostDTO.setAuthor(submission.getAuthor());
        redditPostDTO.setFlair(submission.getLinkFlairText());
        redditPostDTO.setUrl(submission.getUrl());
        redditPostDTO.setPermalink(submission.getPermalink());
        redditPostDTO.setCreatedAt(submission.getCreated().toInstant());
        redditPostDTO.setSpoiler(submission.isSpoiler());

        return redditPostDTO;
    }


}
