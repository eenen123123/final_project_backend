package kr.or.ddit.service.gemini;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;

import kr.or.ddit.AdminApplication;
import lombok.extern.slf4j.Slf4j;

@SpringBootTest(classes = AdminApplication.class)
@Slf4j
public class GeminiTest {

    @Value("${gemini.api-key}")
    String geminiApiKey;

    @Test
    void apiTest() {
        Client client = Client.builder().apiKey(geminiApiKey).build();

        GenerateContentResponse response
                = client.models.generateContent(
                        "gemini-3.1-flash-lite",
                        "안녕?",
                        null);

        log.info("{}", response.text());

    }
}
