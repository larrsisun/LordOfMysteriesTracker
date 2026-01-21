package TelegramBot.LordOfMysteriesTracker.bot.commands.forFilters;

import TelegramBot.LordOfMysteriesTracker.bot.commands.Command;
import TelegramBot.LordOfMysteriesTracker.model.FilterType;
import TelegramBot.LordOfMysteriesTracker.services.SubscriptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class FilterCommand implements Command {

    @Autowired
    private SubscriptionService subscriptionService;

    @Override
    public void execute(Long chatID, String[] args, SendMessage response) {
        if(!subscriptionService.isSubscribed(chatID)) {
            response.setText("сначала нужно подписаться!");
            return;
        }

        if (args.length == 0) {
            showCurrentFilters(chatID, response);
        } else {
            setFilters(chatID, args, response);
        }

    }

    private void showCurrentFilters(Long chatID, SendMessage response) {
        Set<String> filters = subscriptionService.getFilters(chatID);

        StringBuilder message = new StringBuilder();
        message.append("Ваши текущие фильтры: \n");

        if (filters.contains("all")) {
            message.append("Вы подписаны на все категории (без фильтра)\n");
        } else {
            for (String filtering : filters) {
                FilterType filter = FilterType.fromType(filtering);
                message.append("*-* ").append(filter.getDisplayName()).append("\n");
            }
        }

        message.append("\n *Доступные фильтры:* \n");
        for (FilterType filterType : FilterType.values()) {
            message.append("*-* ").append(filterType.getType()).append(" - ").append(filterType.getDisplayName())
                    .append(";").append("\n");
        }

        message.append("\n *Примеры ввода:* \n");
        message.append("/filters fanart discussion - только арты и обсуждения\n");
        message.append("/filters all - всё (по умолчанию)");

        response.setText(message.toString());
        response.enableMarkdown(true);

    }

    private void setFilters(Long chatId, String[] args, SendMessage response) {
        Set<String> requestedFilters = Arrays.stream(args)
                .map(String::trim)
                .map(String::toLowerCase)
                .filter(FilterType::isValidType)
                .collect(Collectors.toSet());

        try {
            subscriptionService.updateFilters(chatId, requestedFilters);

            Set<String> newFilters = subscriptionService.getFilters(chatId);
            StringBuilder result = new StringBuilder();
            result.append("*Фильтры обновлены!*\n\n");

            if (newFilters.contains("all")) {
                result.append("Теперь вы будете получать все типы контента.\n");
            } else {
                result.append("Теперь вы будете получать:\n");
                for (String filterCode : newFilters) {
                    FilterType filter = FilterType.fromType(filterCode);
                    result.append("• ").append(filter.getDisplayName()).append("\n");
                }
            }

            result.append("\nиспользуйте `/filters`, чтобы проверить или изменить.");

            response.setText(result.toString());
            response.enableMarkdown(true);
        } catch (Exception e) {
            response.setText("ошибка: " + e.getMessage() +
                    "\nиспользуйте `/filters` для справки.");
        }
    }


    @Override
    public String getName() {
        return "/filter";
    }


}
