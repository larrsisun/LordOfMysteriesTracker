package TelegramBot.LordOfMysteriesTracker.services;


import TelegramBot.LordOfMysteriesTracker.dto.RedditPostDTO;
import TelegramBot.LordOfMysteriesTracker.model.FilterType;
import net.dean.jraw.RedditClient;
import net.dean.jraw.models.Submission;
import net.dean.jraw.models.SubredditSort;
import net.dean.jraw.pagination.DefaultPaginator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RedditService {

    private final Logger log = LoggerFactory.getLogger(RedditService.class);

    private final RedditClient redditClient;
    private final RedisCacheService cacheService;
    private final RedditRateLimiterService rateLimiter;

    private final int FETCH_LIMIT = 25;

    @Autowired
    public RedditService(RedditClient redditClient, RedisCacheService cacheService, RedditRateLimiterService rateLimiter) {
        this.redditClient = redditClient;
        this.cacheService = cacheService;
        this.rateLimiter = rateLimiter;
    }

    public List<RedditPostDTO> getNewFilteredPosts(String subreddit, Set<FilterType> userFilters) {
        log.debug("Получение отфильтрованных постов из сабреддита с фильтрами {}", userFilters);

        List<RedditPostDTO> newPosts = getNewPosts(subreddit);

        if (newPosts.isEmpty()) {
            log.debug("Новых постов не найдено");
            return Collections.emptyList();
        }

        if (userFilters.contains(FilterType.ALL) || userFilters.isEmpty()) {
            log.debug("Возвращаем все {} постов с фильтром ALL.", newPosts.size());
            return newPosts;
        }

        Set<String> filters = userFilters.stream().map(FilterType::getType).collect(Collectors.toSet());

        return newPosts.stream().filter(post -> {
            String postFlair = post.getFlair();
            return filters.contains(postFlair.toLowerCase());
        }).collect(Collectors.toList());

    }

    public List<RedditPostDTO> getNewPosts(String subreddit) {
        log.info("Получение новых постов из сабреддита.");
        try {
            List<RedditPostDTO> posts = getPostsFromReddit(subreddit, FETCH_LIMIT);

            if (posts.isEmpty()) {
                log.info("Не удалось получить новые посты.");
                return Collections.emptyList();
            }
            List<RedditPostDTO> newPosts = posts.stream().filter(post -> !cacheService.wasSent(post.getId()))
                    .toList();
            log.info("Найдено {} новых постов из {}", newPosts.size(), posts.size());
            return newPosts;

        } catch (Exception e) {
            log.error("Ошибка при получении новых постов.");
            return Collections.emptyList();
        }
    }

    private List<RedditPostDTO> getPostsFromReddit(String subreddit, int limit) {
        log.debug("Запрос к Reddit API: r/{}, лимит={}", subreddit, limit);
        try {
            rateLimiter.waitForRateLimit();
            DefaultPaginator<Submission> paginator = redditClient.subreddit(subreddit)
                    .posts().sorting(SubredditSort.NEW).limit(limit).build();

            List<Submission> submission = paginator.next();
            log.debug("Получено {} постов из Reddit API", submission.size());
            return submission.stream().map(this::convertToDTO).collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Ошибка при обращении к Reddit API для r/{}", subreddit, e);
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
