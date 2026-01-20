package TelegramBot.LordOfMysteriesTracker.bot.commands;


import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

@Component
public class HelpCommand implements Command {

    @Override
    public void execute(Long chatID, String[] args, SendMessage response) {
        response.setText("""
                 /subscribe - подписаться на рассылку;\n
                \n/unsubscribe - отписаться от рассылки;\n
                \n/help - помощь по командам;\n
                \n/filter - отфильтровать посты (если вы хотите видеть какой-то specific контент)
                
                """);
    }

    @Override
    public String getName() {
        return "/help";
    }
}
