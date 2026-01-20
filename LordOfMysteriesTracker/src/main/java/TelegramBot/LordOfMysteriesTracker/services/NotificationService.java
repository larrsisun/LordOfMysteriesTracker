package TelegramBot.LordOfMysteriesTracker.services;

import TelegramBot.LordOfMysteriesTracker.dto.RedditPostDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.HashSet;
import java.util.Set;

@Service
public class NotificationService {

    private final TelegramLongPollingBot bot;
    private final Set<String> sentPosts = new HashSet<>();

    @Autowired
    public NotificationService(TelegramLongPollingBot bot) {
        this.bot = bot;
    }

    public void sendPostToUser(Long chatID, RedditPostDTO post) {
        if (sentPosts.contains(post.getId())) {
            return;
        }

        try {
            String message = post.getFormattedMessage();
            if (isImageUrl(post.getUrl())) {
                sendPhotoWithCaption(chatID, post.getUrl(), message);
            } else {
                sendTextMessage(chatID, message);
            }

            sentPosts.add(post.getId());

            // Ограничиваем размер кэша
            if (sentPosts.size() > 1000) {
                sentPosts.clear();
            }

        } catch (Exception e) {
            // пробуем отправить просто текстом
            try {
                sendTextMessage(chatID, post.getFormattedMessage());
            } catch (TelegramApiException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void sendTextMessage(Long chatID, String text) throws TelegramApiException {
        SendMessage message = new SendMessage();

        message.setChatId(chatID.toString());
        message.setText(text);
        message.setParseMode("Markdown");
        message.disableWebPagePreview();

        bot.execute(message);
    }

    private void sendPhotoWithCaption(Long chatID, String photoURL, String caption) throws TelegramApiException {
        SendPhoto photo = new SendPhoto();

        photo.setChatId(chatID.toString());

        InputFile inputFile = new InputFile();
        inputFile.setMedia(photoURL);

        photo.setPhoto(inputFile);

        if (caption.length() > 1024) {
            caption = caption.substring(0, 1000) + "...";
        }
        photo.setCaption(caption);
        photo.setParseMode("Markdown");

        bot.execute(photo);

    }

    private boolean isImageUrl(String url) {
        if (url == null) {
            return false;
        }

        String lowerUrl = url.toLowerCase();
        return lowerUrl.matches(".*\\.(jpg|jpeg|png|gif|bmp|webp)$") ||
                lowerUrl.contains("imgur.com") ||
                lowerUrl.contains("i.redd.it");
    }

    public void clearCache() {
        sentPosts.clear();
    }


}
