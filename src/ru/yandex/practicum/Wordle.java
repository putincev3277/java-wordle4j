package ru.yandex.practicum;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

public class Wordle {

    public static void main(String[] args) {
        // Формируем путь к файлу лога через Path — это кроссплатформенно и надёжно
        Path logPath = Paths.get(System.getProperty("user.dir"), "wordle.log");

        try (LoggerService logger = new LoggerService(logPath.toString())) {
            logger.log("Игра Wordle запущена.");

            Path dictionaryPath = Paths.get("words_ru.txt");
            WordleDictionaryLoader loader = new WordleDictionaryLoader(logger);
            WordleDictionary dictionary;

            try {
                dictionary = loader.load(dictionaryPath);
                logger.log("Словарь успешно загружен из: " + dictionaryPath);
            } catch (DictionaryLoadException e) {
                logger.log("Ошибка загрузки основного словаря: " + e.getMessage());
                System.out.println("Не удалось загрузить основной словарь. Используем встроенный мини‑словарь...");
                dictionary = loader.createFallbackDictionary(logger);
                logger.log("Fallback-словарь создан.");
            }

            String answer = dictionary.getRandomWord();
            logger.log("Загадано слово: " + answer);

            int maxSteps = 6;
            WordleGame game = new WordleGame(answer, dictionary, maxSteps, logger);
            logger.log("Игра создана. Максимум попыток: " + maxSteps);

            Scanner scanner = new Scanner(System.in);
            printRules();

            while (!game.isFinished()) {
                System.out.print("Введите слово (5 букв) или нажмите Enter для подсказки: ");
                String input = scanner.nextLine().trim();

                if (input.isEmpty()) {
                    String hint = game.getHint();
                    System.out.println("[💡 Подсказка: \"" + hint + "\"]");
                    logger.log("Подсказка: " + hint);


                    try {
                        char[] result = game.makeMove(hint);
                        logger.log("Ход (подсказка): " + hint + " -> " + String.valueOf(result));
                        printResult(result);
                    } catch (WordNotFoundInDictionaryException e) {
                        System.out.println("\n⚠️ Подсказка оказалась не в словаре: " + e.getMessage());
                    } catch (GameException e) {
                        System.out.println("\n⚠️ Ошибка хода: " + e.getMessage());
                        logger.log("Игровая ошибка: " + e.getMessage());

                        if (e.getMessage().contains("лимит попыток") || e.getMessage().contains("достигнут лимит")) {
                            break;
                        }
                    }


                    continue;
                }

                try {
                    char[] result = game.makeMove(input);
                    logger.log("Ход " + game.getSteps() + ": " + input + " -> " + String.valueOf(result));
                    printResult(result);
                } catch (WordNotFoundInDictionaryException e) {
                    System.out.println("\n⚠️ " + e.getMessage() + ". Попробуйте другое слово.\n");
                    logger.log("Игровая ошибка: " + e.getMessage());
                } catch (GameException e) {
                    System.out.println("\n⚠️ Ошибка хода: " + e.getMessage() + "\n");
                    logger.log("Игровая ошибка: " + e.getMessage());

                    if (e.getMessage().contains("лимит попыток") || e.getMessage().contains("достигнут лимит")) {
                        break;
                    }
                }
            }

            printGameSummary(game);
            logger.log("Игра завершена.");
        } catch (IOException e) {
            System.err.println("❌ Не удалось создать лог-файл: " + e.getMessage());
        } catch (Throwable t) {
            System.err.println("❌ Произошла непредвиденная ошибка.");
            System.err.println("Тип: " + t.getClass().getSimpleName());
            System.err.println("Сообщение: " + t.getMessage());
        }
    }

    private static void printRules() {
        System.out.println("\n=== Игра Wordle ===");
        System.out.println("Правила:");
        System.out.println("- '+' = буква есть и на своём месте");
        System.out.println("- '^' = буква есть, но не там");
        System.out.println("- '-' = такой буквы нет");
        System.out.println("- У Вас шесть попыток\n");
    }

    private static void printResult(char[] result) {
        StringBuilder sb = new StringBuilder();
        for (char c : result) {
            sb.append(c);
        }
        System.out.println(sb);
    }

    private static void printGameSummary(WordleGame game) {
        if (game == null) {
            System.out.println("Игра не была инициализирована.");
            return;
        }

        System.out.println("=== Итог игры ===");
        if (game.hasWon()) {
            System.out.println("🎉 Поздравляем! Вы угадали слово. Кол‑во попыток: " + game.getSteps());
        } else {
            System.out.println("😞 Вы исчерпали все попытки. Загаданное слово: " + game.getAnswer());
        }
        System.out.println("История ходов: " + game.getUsedWords());
    }
}
