package kr.or.ddit.finalProject.config;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import io.github.cdimascio.dotenv.Dotenv;

public class DotenvEnvironmentPostProcessor implements EnvironmentPostProcessor {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        File dir = new File(".").getAbsoluteFile();
        while (dir != null) {
            if (new File(dir, ".env").exists()) {
                Dotenv dotenv = Dotenv.configure().directory(dir.getAbsolutePath()).load();
                Map<String, Object> props = new HashMap<>();
                dotenv.entries().forEach(e -> props.put(e.getKey(), e.getValue()));
                environment.getPropertySources().addFirst(new MapPropertySource("dotenv", props));
                return;
            }
            dir = dir.getParentFile();
        }
    }
}
