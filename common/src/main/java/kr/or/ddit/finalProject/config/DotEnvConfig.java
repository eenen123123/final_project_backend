package kr.or.ddit.finalProject.config;

import java.io.File;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.github.cdimascio.dotenv.Dotenv;
import kr.or.ddit.finalProject.exception.ErrorCode;
import kr.or.ddit.finalProject.exception.FinalProjectException;

@Configuration
public class DotEnvConfig {

    @Bean
    public Dotenv dotenv() {
        File dir = new File(".").getAbsoluteFile();
        while (dir != null) {
            if (new File(dir, ".env").exists()) {
                return Dotenv.configure().directory(dir.getAbsolutePath()).load();
            }
            dir = dir.getParentFile();
        }
        throw new FinalProjectException(ErrorCode.DOTENV_FILE_NOT_FOUND);
    }
}
