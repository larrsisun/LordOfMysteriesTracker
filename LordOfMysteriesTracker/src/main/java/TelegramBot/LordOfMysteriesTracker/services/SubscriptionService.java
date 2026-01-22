package TelegramBot.LordOfMysteriesTracker.services;

import TelegramBot.LordOfMysteriesTracker.model.Subscription;
import TelegramBot.LordOfMysteriesTracker.repositories.SubscriptionRepository;
import TelegramBot.LordOfMysteriesTracker.util.DatabaseException;
import TelegramBot.LordOfMysteriesTracker.util.SubscriptionNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional
public class SubscriptionService {

    private static final Logger log = LoggerFactory.getLogger(SubscriptionService.class);
    private final SubscriptionRepository subscriptionRepository;

    @Autowired
    public SubscriptionService(SubscriptionRepository subscriptionRepository) {
        this.subscriptionRepository = subscriptionRepository;
    }

    public Subscription subscribe(Long chatID) {

        try {
            Optional<Subscription> subscription = subscriptionRepository.findByChatID(chatID);

            if (subscription.isPresent()) {
                Subscription sub = subscription.get();
                if (Boolean.TRUE.equals(sub.getActive())) {
                    log.info("Пользователь {} уже подписан.", chatID);
                    return sub;
                }
                sub.setActive(true);
                return subscriptionRepository.save(sub);
            }

            Subscription newSub = new Subscription(chatID);
            return subscriptionRepository.save(newSub);
        } catch (DataAccessException e) {
            log.error("Ошибка в базе данных при попытке подписать пользователя");
            throw new DatabaseException("Не удалось подписаться из-за проблем с базой данных");
        }
    }

    public boolean unsubscribe(Long chatID) {
        try {
            Optional<Subscription> subscription = subscriptionRepository.findByChatID(chatID);
        if (subscription.isPresent() && Boolean.TRUE.equals(subscription.get().getActive())) {
            subscriptionRepository.deactivateByChatID(chatID);
            log.info("Пользователь {} отписан!", chatID);
            return true;
        }
        log.warn("Попытка отписать неподписанного пользователя {}", chatID);
        return false;
        } catch (DataAccessException e) {
            log.error("Ошибка в базе данных при попытке отписать пользователя {}", chatID);
            throw new DatabaseException("Не удалось отписать пользователя ввиду ошибки со стороны БД");
        }
    }

    public boolean isSubscribed(Long chatID) {
        try {
            Optional<Subscription> subscription = subscriptionRepository.findByChatID(chatID);
            return subscription.isPresent() && Boolean.TRUE.equals(subscription.get().getActive());
        } catch (DataAccessException e) {
            log.error("Ошибка при попытке выяснить, подписан ли пользователь {}", chatID);
            throw new DatabaseException("Не удалось узнать статус пользователя ввиду ошибки со стороны БД");
        }

    }

    public Set<String> getFilters(Long chatID) {
        try {
            return getSubscription(chatID)
                    .map(Subscription::getFilterCodes)
                    .orElse(Set.of("all"));
        } catch (DataAccessException e) {
            log.error("Database error while getting filters for user {}", chatID, e);
            return Set.of("all"); // Безопасное значение по умолчанию
        }
    }

    public Subscription updateFilters(Long chatId, Set<String> filterCodes) {
        try {
            Subscription subscription = getSubscription(chatId)
                    .orElseThrow(() -> new SubscriptionNotFoundException("Подписка не найдена, сначала подпишитесь (/subscribe)."));
            subscription.setFiltersFromCodes(filterCodes);
            return subscriptionRepository.save(subscription);
        } catch (SubscriptionNotFoundException e) {
            throw e; // Пробрасываем дальше
        } catch (DataAccessException e) {
            log.error("Database error while updating filters for user {}", chatId, e);
            throw new DatabaseException("Не удалось обновить фильтры. Попробуйте позже.");
        }
    }


    public Optional<Subscription> getSubscription(Long chatId) {
        return subscriptionRepository.findByChatID(chatId);
    }

    public List<Subscription> getAllActiveSubscriptions() {
        return subscriptionRepository.findByIsActiveTrue();
    }

}
