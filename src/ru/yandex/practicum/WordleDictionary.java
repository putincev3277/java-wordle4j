package ru.yandex.practicum;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static ru.yandex.practicum.GameConfig.WORD_LENGTH;

public class WordleDictionary {

    private final List<String> words;

    public WordleDictionary(List<String> words) {
        this.words = new ArrayList<>(words);
    }

    public List<String> getWords() {
        return words;
    }

    public void validateWord(String word) throws WordNotFoundInDictionaryException {
        String normalized = TextUtils.normalize(word);
        if (!words.contains(normalized)) {
            throw new WordNotFoundInDictionaryException("Слово \"" + word + "\" отсутствует в словаре");
        }
    }

    public char[] compare(String guess, String answer) {
        guess = TextUtils.normalize(guess);
        answer = TextUtils.normalize(answer);


        if (guess.length() != WORD_LENGTH || answer.length() != WORD_LENGTH) {
            throw new IllegalArgumentException("Слова должны быть одинаковой длины");
        }

        char[] result = new char[guess.length()];
        Map<Character, Integer> answerCounts = new HashMap<>();

        for (char c : answer.toCharArray()) {
            answerCounts.put(c, answerCounts.getOrDefault(c, 0) + 1);
        }

        // 1. Точные совпадения
        for (int i = 0; i < guess.length(); i++) {
            char g = guess.charAt(i);
            char a = answer.charAt(i);

            if (g == a) {
                result[i] = '+';
                answerCounts.put(g, answerCounts.get(g) - 1);
            } else {
                result[i] = '-';
            }
        }

        // 2. Наличие буквы в другом месте
        for (int i = 0; i < guess.length(); i++) {
            if (result[i] == '+') continue;

            char g = guess.charAt(i);
            Integer count = answerCounts.get(g);

            if (count != null && count > 0) {
                result[i] = '^';
                answerCounts.put(g, count - 1);
            } else {
                result[i] = '-';
            }
        }

        return result;
    }

    public String getRandomWord() {
        if (words.isEmpty()) {
            throw new GameException("Словарь пуст, невозможно выбрать слово.");
        }
        int idx = (int) (Math.random() * words.size());
        return words.get(idx);
    }
}
