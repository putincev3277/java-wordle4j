package ru.yandex.practicum;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class WordleDictionaryLoaderTest {

    private WordleDictionaryLoader loader;
    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() throws IOException {
        LoggerService logger = new LoggerService(tempDir.resolve("test.log").toString());
        loader = new WordleDictionaryLoader(logger);
    }

    // Тест: ровно 5 валидных слов без дублей после нормализации
    @Test
    void load_validFile_returnsExactlyFiveWords() throws Exception {
        Path file = tempDir.resolve("words.txt");
        String content = """
            зерно
            банан
            груша
            дырка
            робот
            зерно
            """;
        Files.writeString(file, content, StandardCharsets.UTF_8);

        WordleDictionary dict = loader.load(file);
        List<String> words = dict.getWords();

        assertEquals(5, words.size(), "Должно быть ровно 5 валидных уникальных слов");
        assertTrue(words.contains("зерно"));
        assertTrue(words.contains("банан"));
        assertTrue(words.contains("груша"));
        assertTrue(words.contains("дырка"));
        assertTrue(words.contains("робот"));
        assertTrue(words.contains("зерно"));
    }

    // Тест: пробелы, пустые строки, но без дублей после нормализации
    @Test
    void load_fileWithSpacesAndEmptyLines_filtersCorrectly() throws Exception {
        Path file = tempDir.resolve("words_with_spaces.txt");

        String content = """
            арбуз
              банан

            груша
            лаваш
            слишкомдлинное
            лимон
            """;
        Files.writeString(file, content, StandardCharsets.UTF_8);

        WordleDictionary dict = loader.load(file);
        List<String> words = dict.getWords();

        // "слишкомдлинное" отфильтруется по длине, остальные 5 — валидные
        assertEquals(5, words.size());

        assertTrue(words.contains("арбуз"));
        assertTrue(words.contains("банан"));
        assertTrue(words.contains("груша"));
        assertTrue(words.contains("лаваш"));
        assertTrue(words.contains("лимон"));
    }

    // Тест: слова с «ё», но после нормализации нет дублей
    @Test
    void load_fileWithYo_normalizesToE() throws Exception {
        Path file = tempDir.resolve("words_with_yo.txt");
        String content = """
            ёёёёё
            тёлка
            ёёёёё
            ёоооё
            Ёёёоо
            оооёЁ
            """;
        Files.writeString(file, content, StandardCharsets.UTF_8);

        WordleDictionary dict = loader.load(file);
        List<String> words = dict.getWords();

        assertEquals(5, words.size(), "После нормализации должно остаться 5 уникальных слов");
        assertTrue(words.contains("еееее"));
        assertTrue(words.contains("телка"));
        assertTrue(words.contains("еооое"));
        assertTrue(words.contains("еееоо"));
        assertTrue(words.contains("оооее"));
    }

    // Тест: смешанный контент, остаются только 5 валидных слова
    @Test
    void load_mixedContent_filtersByLengthAndNormalizes() throws Exception {
        Path file = tempDir.resolve("mixed.txt");
        String content = """
            АрБуЗ
            БАНАН
            КИВИ
            груш
            денЬ
            12345
            abcde
            """;
        Files.writeString(file, content, StandardCharsets.UTF_8);

        WordleDictionary dict = loader.load(file);
        List<String> words = dict.getWords();

        assertEquals(2, words.size(), "Должны остаться только валидные кириллические слова длиной 5");
        assertTrue(words.contains("арбуз"));
        assertTrue(words.contains("банан"));
        assertFalse(words.contains("12345"));
        assertFalse(words.contains("abcde"));

    }
}
