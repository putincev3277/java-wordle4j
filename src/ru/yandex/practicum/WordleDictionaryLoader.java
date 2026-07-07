package ru.yandex.practicum;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import static ru.yandex.practicum.GameConfig.WORD_LENGTH;

public class WordleDictionaryLoader {

    private final LoggerService logger;


    public WordleDictionaryLoader(LoggerService logger) {
        this.logger = logger;
    }

    /**
     * Загружает словарь из файла.
     */
    public WordleDictionary load(Path path) throws DictionaryLoadException {
        Set<String> uniqueWords = new LinkedHashSet<>();

        int totalLines = 0;
        int skippedEmpty = 0;
        int skippedLength = 0;
        int skippedInvalidChars = 0;
        int skippedDuplicate = 0;

        logger.log("Начинаем загрузку словаря из файла: " + path);

        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                totalLines++;
                String word = line.trim();

                if (word.isEmpty()) {
                    skippedEmpty++;
                    continue;
                }

                // Сначала нормализуем
                word = TextUtils.normalize(word);

                // Проверяем уже нормализованное слово
                if (word.length() != WORD_LENGTH) {
                    skippedLength++;
                    continue;
                }

                // Важно: включаем «ё» в диапазон
                if (!word.matches("[а-яё]+")) {
                    skippedInvalidChars++;
                    continue;
                }

                if (!uniqueWords.add(word)) {
                    skippedDuplicate++;

                }
            }
        } catch (IOException e) {
            logger.log("Ошибка при чтении файла словаря: " + e.getMessage());
            throw new DictionaryLoadException("Не удалось загрузить словарь из файла: " + path, e);
        }

        String stats = "Загрузка завершена: всего строк = " + totalLines +
                ", пропущено пустых = " + skippedEmpty +
                ", пропущено по длине = " + skippedLength +
                ", пропущено по символам = " + skippedInvalidChars +
                ", пропущено дубликатов = " + skippedDuplicate +
                ", загружено валидных слов = " + uniqueWords.size();
        logger.log(stats);

        if (uniqueWords.isEmpty()) {
            String msg = "Словарь пуст после фильтрации.";
            logger.log(msg);
            throw new DictionaryLoadException(msg, null);
        }

        return new WordleDictionary(new ArrayList<>(uniqueWords));
    }

    /**
     * Создаёт запасной словарь (fallback) со словами по умолчанию.
     * Используется, если основной словарь недоступен.
     */
    public WordleDictionary createFallbackDictionary(LoggerService logger) {
        // Логируем, что используем fallback
        logger.log("Используется запасной (fallback) словарь.");

        Set<String> fallbackWords = new LinkedHashSet<>();
        List<String> rawWords = List.of(
                "арбуз",
                "банан",
                "груша",
                "дыня",
                "киви",
                "лимон",
                "манго",
                "персик",
                "яблоко",
                "абрикос" // это 6 букв — будет отфильтровано, просто пример
        );

        for (String word : rawWords) {
            // Применяем ту же нормализацию, что и в load
            String normalized = TextUtils.normalize(word.trim());

            // Проверяем длину уже после нормализации (на случай, если trim что‑то изменил)
            if (normalized.length() == WORD_LENGTH && normalized.matches("[а-яё]+")) {
                fallbackWords.add(normalized);
            } else {
                logger.log("Слово исключено из fallback по длине или символам: '" + word + "' -> '" + normalized + "'");
            }
        }

        if (fallbackWords.isEmpty()) {
            logger.log("Fallback‑словарь пуст после фильтрации!");
            // Можно выбросить исключение, но для fallback чаще делают хотя бы одно слово
            fallbackWords.add("арбуз");
        }

        logger.log("Запасной словарь содержит " + fallbackWords.size() + " валидных слов.");
        return new WordleDictionary(new ArrayList<>(fallbackWords));
    }
}
