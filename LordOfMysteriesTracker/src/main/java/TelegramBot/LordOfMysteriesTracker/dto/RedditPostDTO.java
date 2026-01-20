package TelegramBot.LordOfMysteriesTracker.dto;

import java.time.Instant;
import java.util.Date;

public class RedditPostDTO {
    private String id;
    private String title;
    private String author;
    private String url;
    private String permalink;
    private String flair;
    private Instant createdAt;
    private boolean isSpoiler;

    public String getFormattedMessage() {

        StringBuilder stringBuilder = new StringBuilder();

        if (flair != null && !flair.isEmpty()) {
            stringBuilder.append("Тематика: ").append(flair).append("\n");
        }

        stringBuilder.append("*").append(escapeMarkdown(title)).append("*\n");
        stringBuilder.append("\n[Ссылка на пост](https://reddit.com").append(permalink).append(")");

        if (url != null && !url.equals(permalink) && !url.contains("reddit.com")) {
            stringBuilder.append(" | [Ссылка на контент](").append(url).append(")");
        }

        return stringBuilder.toString();

    }

    private String escapeMarkdown(String text) {
        // Экранируем специальные символы MarkdownV2 для Telegram
        return text.replace("_", "\\_")
                .replace("*", "\\*")
                .replace("[", "\\[")
                .replace("]", "\\]")
                .replace("(", "\\(")
                .replace(")", "\\)")
                .replace("~", "\\~")
                .replace("`", "\\`")
                .replace(">", "\\>")
                .replace("#", "\\#")
                .replace("+", "\\+")
                .replace("-", "\\-")
                .replace("=", "\\=")
                .replace("|", "\\|")
                .replace("{", "\\{")
                .replace("}", "\\}")
                .replace(".", "\\.")
                .replace("!", "\\!");
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public String getFlair() {
        return flair;
    }

    public void setFlair(String flair) {
        this.flair = flair;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isSpoiler() {
        return isSpoiler;
    }

    public void setSpoiler(boolean spoiler) {
        isSpoiler = spoiler;
    }

    public String getPermalink() {
        return permalink;
    }

    public void setPermalink(String permalink) {
        this.permalink = permalink;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
