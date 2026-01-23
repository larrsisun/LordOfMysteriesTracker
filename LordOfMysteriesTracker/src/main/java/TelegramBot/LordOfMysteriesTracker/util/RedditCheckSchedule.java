package TelegramBot.LordOfMysteriesTracker.util;

import TelegramBot.LordOfMysteriesTracker.dto.RedditPostDTO;
import TelegramBot.LordOfMysteriesTracker.model.Subscription;
import TelegramBot.LordOfMysteriesTracker.services.NotificationService;
import TelegramBot.LordOfMysteriesTracker.services.RedditService;
import TelegramBot.LordOfMysteriesTracker.services.SubscriptionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RedditCheckSchedule {

    private static final Logger log = LoggerFactory.getLogger(RedditCheckSchedule.class);

    @Value("${reddit.target.subreddit}")
    private String SUBREDDIT;
    @Value("${notification.delay.between.posts.ms:500}") // 0.5 секунды между постами
    private long delayBetweenPosts;
    @Value("${reddit.check.interval.ms:300000}") // 5 минут по умолчанию
    private long checkIntervalMs;
    @Value("${notification.delay.between.users.ms:1000}") // 1 секунда между пользователями
    private long delayBetweenUsers;

    private final SubscriptionService subscriptionService;
    private final RedditService redditService;
    private final NotificationService notificationService;

    @Autowired
    public RedditCheckSchedule(SubscriptionService subscriptionService, RedditService redditService, NotificationService notificationService) {
        this.subscriptionService = subscriptionService;
        this.redditService = redditService;
        this.notificationService = notificationService;
    }

    @Scheduled(fixedDelay = 300000)
    public void checkForNewPosts() {

        try {
            List<Subscription> activeSubscriptions = subscriptionService.getAllActiveSubscriptions();

            if (activeSubscriptions.isEmpty()) {
                log.info("Нет активных подписчиков, проверка пропущена");
                return;
            }

            log.info("Найдено {} активных подписчиков", activeSubscriptions.size());

            // Получаем новые посты из Reddit
            List<RedditPostDTO> newPosts = redditService.getNewPosts(SUBREDDIT);

            if (newPosts.isEmpty()) {
                log.info("Новых постов не найдено.");
                return;
            }

            log.info("Найдено {} новых постов для обработки", newPosts.size());

            // Проходим по каждому подписчику
            for (Subscription subscription : activeSubscriptions) {
                try {
                    // Задержка между пользователями, чтобы не перегрузить Telegram API
                    if (delayBetweenUsers > 0) {
                        Thread.sleep(delayBetweenUsers);
                    }

                } catch (Exception e) {
                    log.error("Ошибка при обработке подписки пользователя {}: {}",
                            subscription.getChatID(), e.getMessage(), e);
                }
            }

        } catch (Exception e) {
            log.error("Ошибка при проверке постов.", e);
        }
    }

}
