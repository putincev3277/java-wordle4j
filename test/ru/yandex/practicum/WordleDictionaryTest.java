package ru.yandex.practicum;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class WordleDictionaryTest {

    private LoggerService logger;
    private WordleDictionary dictionary;

    @BeforeEach
    void setUp() throws IOException {
        // Для тестов лог пишем в System.out, файл не создаём
        logger = new LoggerService(System.out);
        List<String> words = List.of("арбуз", "банан", "груша", "дыня", "киви");
        dictionary = new WordleDictionary(words);
    }

    @Test
    void validateWord_validWord_doesNotThrow() {
        dictionary.validateWord("арбуз"); // не выбрасывает
        dictionary.validateWord("АРБУЗ"); // регистр не важен
        dictionary.validateWord("АрБуЗ");
    }

    @Test
    void validateWord_invalidWord_throwsException() {
        assertThrows(WordNotFoundInDictionaryException.class, () ->
                dictionary.validateWord("неарбуз")
        );
    }

    @Test
    void compare_correctPosition() {
        char[] result = dictionary.compare("арбуз", "арбуз");
        assertArrayEquals(new char[]{'+', '+', '+', '+', '+'}, result);
    }

    @Test
    void compare_someCorrectSomeWrong() {
        char[] result = dictionary.compare("барин", "арбуз");
        System.out.println("Result: " + java.util.Arrays.toString(result));
        assertArrayEquals(new char[]{'^', '^', '^', '-', '-'}, result);
    }



    @Test
    void compare_yellowLetters() {
        // В загаданном «арбуз» есть «у», но не на 4‑й позиции.
        // В попытке «азука» буква «у» есть, но не на своём месте
        char[] result = dictionary.compare("азука", "арбуз");
        // Позиции: a==a (+), з!=р (-), у!=б (^), к!=у (-), а==з (-)
        // Но логика жёлтых букв учитывает количество букв.
        // Здесь важно, что «у» не на своём месте, но есть в слове.
        assertTrue(result[2] == '^');
    }

    @Test
    void getRandomWord_returnsOneOfTheWords() {
        String word = dictionary.getRandomWord();
        assertTrue(List.of("арбуз", "банан", "груша", "дыня", "киви").contains(word));
    }
}
