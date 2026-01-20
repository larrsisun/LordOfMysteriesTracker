package TelegramBot.LordOfMysteriesTracker.util;

import TelegramBot.LordOfMysteriesTracker.dto.RedditPostDTO;
import TelegramBot.LordOfMysteriesTracker.model.Subscription;
import TelegramBot.LordOfMysteriesTracker.services.NotificationService;
import TelegramBot.LordOfMysteriesTracker.services.RedditService;
import TelegramBot.LordOfMysteriesTracker.services.SubscriptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RedditCheckSchedule {

    private static final String SUBREDDIT = "LordOfTheMysteries";

    @Autowired
    private SubscriptionService subscriptionService;

    @Autowired
    private RedditService redditService;

    @Autowired
    private NotificationService notificationService;

    @Scheduled(fixedDelay = 1800000)
    public void checkForNewPosts() {
        List<Subscription> activeSubscriptions = subscriptionService.getAllActiveSubscriptions();

        if (activeSubscriptions.isEmpty()) {
            System.out.println("Нет активных подписчиков!");
            return;
        }

        for(Subscription subscription : activeSubscriptions) {
            try {
                List<RedditPostDTO> newPosts = redditService.getNewFilteredPosts(SUBREDDIT, subscription.getFilters());

                for (RedditPostDTO posts : newPosts) {
                    notificationService.sendPostToUser(subscription.getChatID(), posts);
                    Thread.sleep(1000);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Scheduled(cron = "0 0 3 * * ?") // Каждый день в 3:00
    public void clearCache() {
        notificationService.clearCache();
        System.out.println("Кэш отправленных постов очищен");
    }

}
