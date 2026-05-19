package kr.or.ddit.finalProject.util;

import java.util.Random;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RandomSixDigits {
    public static String generate() {
        Random random = new Random();
        int number = random.nextInt(900000) + 100000;
        String sixDigits = String.valueOf(number);
        log.info("Generated 6-digit code: {}", sixDigits);
        return sixDigits;
    }
}
