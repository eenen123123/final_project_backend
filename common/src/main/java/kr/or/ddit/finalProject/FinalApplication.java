package kr.or.ddit.finalProject;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.github.cdimascio.dotenv.Dotenv;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FinalApplication {

    private static Dotenv loadDotenvWithFallback() {
        Path currentDirEnv = Paths.get(".env").toAbsolutePath().normalize();
        if (Files.exists(currentDirEnv)) {
            log.info("Loaded .env from {}", currentDirEnv);
            return Dotenv.configure().ignoreIfMissing().load();
        }

        Path finalDirEnv = Paths.get("final", ".env").toAbsolutePath().normalize();
        if (Files.exists(finalDirEnv)) {
            log.info("Loaded .env from {}", finalDirEnv);
            return Dotenv.configure().ignoreIfMissing().directory("final").load();
        }

        log.warn(
                ".env file not found in current directory or final directory. Continuing without dotenv values.");
        return Dotenv.configure().ignoreIfMissing().load();
    }

    public static void main(String[] args) {
        // test
        Dotenv dotenv = loadDotenvWithFallback();
        dotenv.entries().forEach(entry -> {
            System.setProperty(entry.getKey(), entry.getValue());
            System.setProperty(entry.getKey().toLowerCase(), entry.getValue());
        });
        SpringApplication.run(FinalApplication.class, args);

        String logo = """


                ┌────────────────────────┐
                │                        │
                │   Final Project DDIT   │
                │                        │
                └────────────────────────┘
                """;

        log.info(logo);
    }
}
