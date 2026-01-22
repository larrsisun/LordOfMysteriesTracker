package TelegramBot.LordOfMysteriesTracker.configs;

import net.dean.jraw.RedditClient;
import net.dean.jraw.http.NetworkAdapter;
import net.dean.jraw.http.OkHttpNetworkAdapter;
import net.dean.jraw.http.UserAgent;
import net.dean.jraw.oauth.Credentials;
import net.dean.jraw.oauth.OAuthHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.UUID;

@Configuration
public class RedditConfig {

    private static final Logger log = LoggerFactory.getLogger(RedditConfig.class);

    @Value("${reddit.client.id}")
    private String clientId;

    @Value("${reddit.client.secret}")
    private String clientSecret;

    @Value("${reddit.device.id}")
    private String deviceId;

    @Bean
    public RedditClient redditClient() {

        UUID ourDeviceId;

        if ((deviceId != null && !deviceId.trim().isEmpty())) {
            try {
                ourDeviceId = UUID.fromString(deviceId);
            } catch (IllegalArgumentException e) {
                log.warn("Неправильный UUID в конфигурации. Будет сгенерирован новый.");
                ourDeviceId = UUID.randomUUID();
            }
        } else {
            ourDeviceId = UUID.randomUUID();
            log.info("Никакого девайс айди не нашли, сгенерирован новый: {}", ourDeviceId);
        }

        UserAgent userAgent = new UserAgent("bot", "com.lotmtracker", "1.0.0", "u/larrsirae");

        Credentials credentials = Credentials.userless(clientId, clientSecret, ourDeviceId);

        NetworkAdapter adapter = new OkHttpNetworkAdapter(userAgent);

        try {
            RedditClient client = OAuthHelper.automatic(adapter, credentials);
            client.setLogHttp(false); // отключаем логирование http-запросов
            log.info("Реддит-клиент инициализирован");
            return client;
        } catch (Exception e) {
            log.error("Попытка инициализации реддит-клиента провалена", e);
            throw new RuntimeException("Не могу подключиться к Reddit API", e);
        }

    }

}
