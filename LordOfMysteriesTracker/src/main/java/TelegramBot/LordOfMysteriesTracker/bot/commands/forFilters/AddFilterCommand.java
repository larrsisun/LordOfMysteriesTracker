package TelegramBot.LordOfMysteriesTracker.bot.commands.forFilters;

import TelegramBot.LordOfMysteriesTracker.bot.commands.Command;
import TelegramBot.LordOfMysteriesTracker.model.Subscription;
import TelegramBot.LordOfMysteriesTracker.services.SubscriptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

@Component
public class AddFilterCommand implements Command {

    @Autowired
    private SubscriptionService subscriptionService;

    @Override
    public void execute(Long chatID, String[] args, SendMessage response) {

        if (!subscriptionService.isSubscribed(chatID)) {
            response.setText("Сначала нужно подписаться на рассылку!");
        }



    }

    @Override
    public String getName() {
        return "/addfilter";
    }
}
