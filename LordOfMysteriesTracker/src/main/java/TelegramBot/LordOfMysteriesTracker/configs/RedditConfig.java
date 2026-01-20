package TelegramBot.LordOfMysteriesTracker.configs;

import net.dean.jraw.RedditClient;
import net.dean.jraw.http.NetworkAdapter;
import net.dean.jraw.http.OkHttpNetworkAdapter;
import net.dean.jraw.http.UserAgent;
import net.dean.jraw.oauth.Credentials;
import net.dean.jraw.oauth.OAuthHelper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedditConfig {

    @Value("${reddit.client.id}")
    private String clientId;

    @Value("${reddit.client.secret}")
    private String clientSecret;

    @Value("${reddit.username}")
    private String username;

    @Value("${reddit.password}")
    private String password;

    @Bean
    public RedditClient redditClient() {
        UserAgent userAgent = new UserAgent("bot", "com.lotmtracker", "1.0.0", username);

        Credentials credentials = Credentials.script(username, password, clientId, clientSecret);

        NetworkAdapter adapter = new OkHttpNetworkAdapter(userAgent);
        RedditClient client = OAuthHelper.automatic(adapter, credentials);
        client.setLogHttp(false); // отключаем логирование http-запросов

        return client;
    }

}
