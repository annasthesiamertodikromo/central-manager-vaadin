package com.bludots;

import com.bludots.entities.TomcatInstanceEntity;
import com.bludots.repositories.TomcatInstanceRepository;
import org.springframework.boot.CommandLineRunner;
import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.theme.Theme;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
/**
 * The entry point of the Spring Boot application.
 *
 * Use the @PWA annotation make the application installable on phones, tablets
 * and some desktop browsers.
 *
 */
@EnableAsync
@SpringBootApplication
@Theme(value = "central-manager")
public class Application implements AppShellConfigurator {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    CommandLineRunner initDatabase(TomcatInstanceRepository repository) {
        return args -> {
            if (repository.count() == 0) {
                repository.save(new TomcatInstanceEntity("Client A", "Running", "10.0.0.10"));
                repository.save(new TomcatInstanceEntity("Client B", "Stopped", "10.0.0.15"));
                repository.save(new TomcatInstanceEntity("Client C", "Deploying", "10.0.0.22"));
            }
        };
    }
}
