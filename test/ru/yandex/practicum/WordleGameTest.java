package ru.yandex.practicum;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class WordleGameTest {

    private LoggerService logger;
    private WordleDictionary dictionary;
    private WordleGame game;

    @BeforeEach
    void setUp() throws IOException {
        logger = new LoggerService(System.out);
        List<String> words = List.of("арбуз", "банан", "груша", "дыня", "киви");
        dictionary = new WordleDictionary(words);
        game = new WordleGame("арбуз", dictionary, 6, logger);
    }

    @Test
    void makeMove_correctWord_marksAllAsPlus() {
        char[] result = game.makeMove("арбуз");
        assertArrayEquals(new char[]{'+', '+', '+', '+', '+'}, result);
        assertTrue(game.hasWon());
    }

    @Test
    void makeMove_invalidLength_throwsGameException() {
        assertThrows(GameException.class, () -> game.makeMove("арб"));
        assertThrows(GameException.class, () -> game.makeMove("арбузы"));
    }

    @Test
    void makeMove_repeatedWord_throwsGameException() {
        game.makeMove("банан");
        assertThrows(GameException.class, () -> game.makeMove("банан"));
    }

    @Test
    void isFinished_trueWhenWon() {
        game.makeMove("арбуз");
        assertTrue(game.isFinished());
        assertTrue(game.hasWon());
    }




    @Test
    void getHint_doesNotReturnAlreadyUsedWord() {
        game.makeMove("банан");
        String hint = game.getHint();
        assertNotEquals("банан", hint);
        // Подсказка должна быть одним из слов словаря
        assertTrue(dictionary.getWords().contains(hint));
    }

    @Test
    void getUsedWords_returnsCopy() {
        game.makeMove("груша");
        List<String> used = game.getUsedWords();
        assertEquals(1, used.size());
        used.add("случайное"); // попытка изменить снаружи
        assertEquals(1, game.getUsedWords().size()); // внутреннее состояние не изменилось
    }
}
