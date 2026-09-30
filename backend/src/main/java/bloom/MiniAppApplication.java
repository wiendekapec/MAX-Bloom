package bloom;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Главный класс запуска Spring Boot приложения MAX Bloom.
 */
@SpringBootApplication(scanBasePackages = {"bloom", "database", "dto", "api", "webhook", "service"})
@EntityScan(basePackages = {"database.entity"})
@EnableJpaRepositories(basePackages = {"database.repository"})
public class MiniAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(MiniAppApplication.class, args);
    }
}
