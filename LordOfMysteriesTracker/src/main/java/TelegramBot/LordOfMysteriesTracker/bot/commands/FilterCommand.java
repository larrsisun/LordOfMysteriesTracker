package TelegramBot.LordOfMysteriesTracker.bot.commands;

import TelegramBot.LordOfMysteriesTracker.model.FilterType;
import TelegramBot.LordOfMysteriesTracker.services.SubscriptionService;
import TelegramBot.LordOfMysteriesTracker.util.BotExceptionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class FilterCommand implements Command {

    @Autowired
    private SubscriptionService subscriptionService;

    @Override
    public void execute(Long chatID, String[] args, SendMessage response) {
        if(!subscriptionService.isSubscribed(chatID)) {
            response.setText("Cначала нужно подписаться!");
            return;
        }

        if (args.length == 0) {
            showCurrentFilters(chatID, response);
            return;
        }

        String action = args[0].toLowerCase();

        switch (action) {
            case "add":
                addFilter(chatID, Arrays.copyOfRange(args, 1, args.length), response);
                break;
            case "remove":
                removeFilters(chatID, Arrays.copyOfRange(args, 1, args.length), response);
                break;
            case "clear":
                clearFilters(chatID, response);
                break;
            case "list":
                listAvailableFilters(response);
                break;
            default:
                setFilters(chatID, args, response);
                break;

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
        message.append("/filters add fanart discussion - только арты и обсуждения\n");
        message.append("/filters all - всё (по умолчанию)");
        message.append("`/filter add [фильтры]` - добавить фильтры\n");
        message.append("`/filter remove [фильтры]` - убрать фильтры\n");
        message.append("`/filter clear` - сбросить все фильтры\n");
        message.append("`/filter art memes` - установить только эти фильтры\n");

        response.setText(message.toString());
        response.enableMarkdown(true);

    }

    private void addFilter(Long chatID, String[] filterCodes, SendMessage response) {

        if(!subscriptionService.isSubscribed(chatID)) {
            response.setText("Cначала нужно подписаться!");
            return;
        }

        Set<String> currentFilers = subscriptionService.getFilters(chatID);

        if (currentFilers.contains("all")) {
            currentFilers.clear();
        }

        Set<String> newFilters = new HashSet<>();
        Set<String> invalidFilters = new HashSet<>();

        for (String codes : filterCodes) {
            String trimmed = codes.trim().toLowerCase();
            if (FilterType.isValidType(trimmed)) {
                newFilters.add(trimmed);
            } else {
                invalidFilters.add(trimmed);
            }
        }

        if (newFilters.isEmpty()) {
            response.setText("Эх, указанные вами фильтры не подходят :(\n" +
                    "Может, ознакомитесь с доступными фильтрами с помощью команды /filter list?");
            response.setParseMode("Markdown");
            return;
        }

        currentFilers.addAll(newFilters);

        try {
            subscriptionService.updateFilters(chatID, currentFilers);
            response.setText("Фильтры обновлены!");

        } catch (Exception e) {
            BotExceptionHandler.handleException(e, chatID, response);
        }

    }

    private void removeFilters(Long chatID, String[] filterCodes, SendMessage response) {
        if (filterCodes.length == 0) {
            response.setText("Укажите фильтры для удаления в формате /filter remove [фильтр].");
            response.setParseMode("Markdown");
            return;
        }

        Set<String> currentFilters = subscriptionService.getFilters(chatID);

        if (currentFilters.contains("all")) {
            response.setText("У вас установлены все категории, для начала установите какой-то конкретный фильтр!");
            return;
        }

        Set<String> removed = new HashSet<>();

        for (String code : filterCodes) {
            String trimmed = code.trim().toLowerCase();
            if (currentFilters.remove(trimmed)) {
                removed.add(trimmed);
            }
        }

        if (currentFilters.isEmpty()) {
            currentFilters.add("all");
        }

        try {
            subscriptionService.updateFilters(chatID, currentFilters);

            StringBuilder result = new StringBuilder();

            if (!removed.isEmpty()) {
                result.append("Указанные вами фильтры удалены!");

            } else {
                result.append("Ни один фильтр не был удалён.\n");
            }

            if (currentFilters.contains("all")) {
                result.append("\n Теперь вы получаете все категории.");
            }
            response.setText(result.toString());
            response.setParseMode("Markdown");
        } catch (Exception e) {
            BotExceptionHandler.handleException(e, chatID, response);
        }
    }

    private void clearFilters(Long chatID, SendMessage response) {
        Set<String> allFilter = new HashSet<>();
        allFilter.add("all");

        try {
            subscriptionService.updateFilters(chatID, allFilter);
            response.setText("Все фильтры сброшены!\n" +
                    "Теперь вы будете получать все типы контента.");
        } catch (Exception e) {
            BotExceptionHandler.handleException(e, chatID, response);
        }
    }

    private void listAvailableFilters(SendMessage response) {
        StringBuilder message = new StringBuilder();
        message.append("*Доступные фильтры:*\n\n");

        for (FilterType filterType : FilterType.values()) {
            if (filterType != FilterType.ALL) {
                message.append("`").append(filterType.getType()).append("`")
                        .append(" — ").append(filterType.getDisplayName())
                        .append("\n");
            }
        }

        message.append("\n💡 *Примеры использования:*\n");
        message.append("`/filter add art memes` — добавить арты и мемы\n");
        message.append("`/filter remove news` — убрать новости\n");
        message.append("`/filter art discussion` — оставить только арты и обсуждения\n");
        message.append("`/filter clear` — получать всё\n");

        response.setText(message.toString());
        response.setParseMode("Markdown");
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
            response.setParseMode("Markdown");
        } catch (Exception e) {
            response.setText("Ошибка: " + e.getMessage() +
                    "\nИспользуйте `/filters` для справки.");
        }
    }


    @Override
    public String getName() {
        return "/filter";
    }


}
