package TelegramBot.LordOfMysteriesTracker.services;

import TelegramBot.LordOfMysteriesTracker.model.Subscription;
import TelegramBot.LordOfMysteriesTracker.repositories.SubscriptionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional
public class SubscriptionService {

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    public Subscription subscribe(Long chatID) {
        Optional<Subscription> subscription = subscriptionRepository.findByChatID(chatID);

        if (subscription.isPresent()) {
            Subscription sub = subscription.get();
            if (Boolean.TRUE.equals(sub.getActive())) {
                return sub;
            }
            sub.setActive(true);
            return subscriptionRepository.save(sub);
        }

        Subscription newSub = new Subscription(chatID);
        return subscriptionRepository.save(newSub);
    }

    public boolean unsubscribe(Long chatID) {
        Optional<Subscription> subscription = subscriptionRepository.findByChatID(chatID);

        if (subscription.isPresent() && Boolean.TRUE.equals(subscription.get().getActive())) {
            subscriptionRepository.deactivateByChatID(chatID);
            return true;
        }
        return false;
    }

    public boolean isSubscribed(Long chatID) {
        Optional<Subscription> subscription = subscriptionRepository.findByChatID(chatID);
        return subscription.isPresent() && Boolean.TRUE.equals(subscription.get().getActive());
    }

    public Set<String> getFilters(Long chatID) {
        return getSubscription(chatID).map(Subscription::getFilterCodes).orElse(Set.of("all"));
    }

    public Subscription updateFilters(Long chatId, Set<String> filterCodes) {
        Subscription subscription = getSubscription(chatId)
                .orElseThrow(() -> new RuntimeException("Пользователь не подписан"));

        subscription.setFiltersFromCodes(filterCodes);
        return subscriptionRepository.save(subscription);
    }

    public Subscription updateFilters(Long chatId, String filtersString) {
        Set<String> codes = new HashSet<>(Arrays.asList(filtersString.split(",")));
        return updateFilters(chatId, codes);
    }


    public Optional<Subscription> getSubscription(Long chatId) {
        return subscriptionRepository.findByChatID(chatId);
    }

    public List<Subscription> getAllActiveSubscriptions() {
        return subscriptionRepository.findByIsActiveTrue();
    }

}
