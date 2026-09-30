package bloom;

import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.HashMap;
import java.util.Map;

/**
 * Конвертирует DATABASE_URL формата Render (postgres://user:pass@host/db)
 * в jdbc:postgresql://host/db?user=user&password=pass для Spring Datasource.
 */
public class DatabaseUrlConverter implements ApplicationListener<ApplicationEnvironmentPreparedEvent> {

    @Override
    public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
        ConfigurableEnvironment env = event.getEnvironment();

        // Если SPRING_DATASOURCE_URL уже задан — ничего не делаем
        String existingUrl = env.getProperty("SPRING_DATASOURCE_URL");
        if (existingUrl != null && !existingUrl.isBlank()) {
            return;
        }

        String databaseUrl = env.getProperty("DATABASE_URL");
        if (databaseUrl == null || databaseUrl.isBlank()) {
            return;
        }

        // Render даёт: postgres://username:password@host:port/dbname
        // или:         postgresql://username:password@host:port/dbname
        try {
            String normalized = databaseUrl
                    .replaceFirst("^postgres://", "postgresql://")
                    .replaceFirst("^postgresql://", "");

            // normalized = "username:password@host:port/dbname"
            int atIdx = normalized.lastIndexOf('@');
            String userInfo = normalized.substring(0, atIdx);      // "username:password"
            String hostAndDb = normalized.substring(atIdx + 1);    // "host:port/dbname"

            String username = "";
            String password = "";
            if (userInfo.contains(":")) {
                int colonIdx = userInfo.indexOf(':');
                username = userInfo.substring(0, colonIdx);
                password = userInfo.substring(colonIdx + 1);
            } else {
                username = userInfo;
            }

            String jdbcUrl = "jdbc:postgresql://" + hostAndDb + "?sslmode=require";

            Map<String, Object> props = new HashMap<>();
            props.put("spring.datasource.url", jdbcUrl);
            if (!username.isBlank()) {
                props.put("spring.datasource.username", username);
            }
            if (!password.isBlank()) {
                props.put("spring.datasource.password", password);
            }

            env.getPropertySources().addFirst(
                    new MapPropertySource("renderDatabaseUrl", props)
            );
        } catch (Exception e) {
            // Не смогли распарсить — продолжаем со старыми настройками
            System.err.println("[DatabaseUrlConverter] Failed to parse DATABASE_URL: " + e.getMessage());
        }
    }
}
