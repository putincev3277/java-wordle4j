package ru.yandex.practicum;

import org.junit.jupiter.api.Test;
import java.io.IOException;
import static org.junit.jupiter.api.Assertions.*;

class WordleTest {

    @Test
    void mainLogic_noCrashWithFallback() throws IOException {
        // Проверяем, что игра корректно стартует даже без файла словаря,
        // используя fallback.
        LoggerService logger = new LoggerService(System.out);

        try (logger) {
            var loader = new WordleDictionaryLoader(logger);
            WordleDictionary dictionary = loader.createFallbackDictionary(logger);
            String answer = dictionary.getRandomWord();
            WordleGame game = new WordleGame(answer, dictionary, 6, logger);

            // Сделаем один ход
            char[] result = game.makeMove(answer);
            assertArrayEquals(new char[]{'+', '+', '+', '+', '+'}, result);
            assertTrue(game.hasWon());
        }
    }
}
