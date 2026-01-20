package TelegramBot.LordOfMysteriesTracker.bot.commands.forFilters;

import TelegramBot.LordOfMysteriesTracker.bot.commands.Command;
import TelegramBot.LordOfMysteriesTracker.model.Subscription;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;


public class ClearFiltersCommand implements Command {

    @Override
    public void execute(Long chatID, String[] args, SendMessage response) {

    }

    @Override
    public String getName() {
        return "/clearFilters";
    }
}
