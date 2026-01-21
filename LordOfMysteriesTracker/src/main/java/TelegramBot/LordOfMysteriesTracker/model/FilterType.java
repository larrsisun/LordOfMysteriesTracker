package TelegramBot.LordOfMysteriesTracker.model;

import lombok.Getter;

@Getter
public enum FilterType {
    ALL("all", "все категории"), ART("art", "фанарты"),
    NOVEL_DISCUSSION("novel_discussion", "обсуждения новеллы"), MEMES("memes", "мемы"),
    NEWS("news", "новости"), OFFICIAL("official", "официальный контент"),
    IMAGE("image", "картинки / фанарты"),
    CHAPTER_DISCUSSION("chapter_discussion", "обсуждения отдельных глав"),
    QUESTION("question", "вопросы"), VIDEO("video", "видео"), POLL("poll", "голосования"),
    RECOMMENDATION("recommendation", "рекоммендации"), CORRUPTION("corruption", "ПОРЧА...");

    private final String type;
    private final String displayName;

    FilterType(String type, String displayName) {
        this.type = type;
        this.displayName = displayName;
    }

    public static FilterType fromType(String type) {
        for (FilterType code : values()) {
            if (code.getType().equalsIgnoreCase(type)) {
                return code;
            }
        }
        throw new IllegalArgumentException("Неизвестный фильтр.");
    }

    public static boolean isValidType(String type) {
        try {
            fromType(type);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

}
