package ru.yandex.practicum;

import java.util.*;
import java.util.stream.Collectors;

public class WordleGame {
    private final String answer;
    private final WordleDictionary dictionary;
    private final int maxSteps;
    private final LoggerService logger;

    // История ходов: порядок важен, нужна индексация — ArrayList оптимален
    private final List<Move> history = new ArrayList<>();

    // Для быстрой проверки «слово уже было» — HashSet
    private final Set<String> triedWordsSet = new HashSet<>();

    public WordleGame(String answer, WordleDictionary dictionary, int maxSteps, LoggerService logger) {
        if (answer == null || answer.isBlank()) {
            // Более корректный тип исключения для неверного аргумента
            throw new IllegalArgumentException("Ответ не может быть пустым или null.");
        }
        if (maxSteps <= 0) {
            throw new IllegalArgumentException("maxSteps должен быть > 0.");
        }

        this.answer = answer;
        this.dictionary = dictionary;
        this.maxSteps = maxSteps;
        this.logger = logger;

        logger.log("WordleGame создан: ответ = " + answer + ", макс. попыток = " + maxSteps);
    }

    public static class Move {
        public final String word;
        public final char[] result;

        public Move(String word, char[] result) {
            this.word = word;
            this.result = result;
        }
    }

    public int getSteps() {
        return history.size();
    }

    public boolean isFinished() {
        boolean guessed = hasWon();
        boolean limitReached = history.size() >= maxSteps;
        boolean finished = guessed || limitReached;

        // Улучшенное логирование состояния
        String state = "isFinished: guessed = " + guessed +
                ", limitReached = " + limitReached +
                ", steps = " + history.size() +
                ", maxSteps = " + maxSteps;
        logger.log(state);

        return finished;
    }

    public boolean hasWon() {
        if (history.isEmpty()) {
            return false;
        }
        Move last = history.getLast();
        for (char c : last.result) {
            if (c != '+') {
                return false;
            }
        }
        return true;
    }

    public String getAnswer() {
        return answer;
    }

    public List<String> getUsedWords() {
        // Возвращаем копию, чтобы внешний код не мог сломать внутреннее состояние
        return new ArrayList<>(triedWordsSet);
    }

    public Map<String, Integer> getWordFrequency() {
        Map<String, Integer> frequency = new LinkedHashMap<>();
        for (Move move : history) {
            frequency.put(move.word, frequency.getOrDefault(move.word, 0) + 1);
        }
        return frequency;
    }

    /**
     * Делает ход. Выбрасывает специализированные исключения для игровых ситуаций.
     */
    public char[] makeMove(String input) throws GameException, WordNotFoundInDictionaryException {
        if (isFinished()) {
            throw new GameException("Игра уже завершена. Нельзя делать новые ходы.");
        }
        if (history.size() >= maxSteps) {
            throw new GameException("Достигнут лимит попыток.");
        }
        if (input == null || input.trim().isEmpty()) {
            throw new GameException("Ввод не может быть пустым.");
        }

        String guess = TextUtils.normalize(input);
        if (guess.length() != 5) {
            throw new GameException("Слово должно быть длиной ровно 5 букв.");
        }

        if (triedWordsSet.contains(guess)) {
            throw new GameException("Вы уже вводили это слово.");
        }

        // Явная проверка наличия слова в словаре — выбрасывает WordNotFoundInDictionaryException
        dictionary.validateWord(guess);

        logger.log("Ход: игрок ввёл \"" + guess + "\", ответ = \"" + answer + "\"");
        char[] result = dictionary.compare(guess, answer);

        history.add(new Move(guess, result));
        triedWordsSet.add(guess);

        StringBuilder resStr = new StringBuilder();
        resStr.append("Результат хода: [");
        for (int i = 0; i < result.length; i++) {
            resStr.append(result[i]);
            if (i < result.length - 1) resStr.append(",");
        }
        resStr.append("]");
        logger.log(resStr.toString());

        return result;
    }

    public String getHint() {
        logger.log("Запрос подсказки. Текущая история ходов: " + history.size());

        Map<String, Integer> wordFrequency = getWordFrequency();

        Set<Character> overusedLetters = new HashSet<>();
        for (Map.Entry<String, Integer> e : wordFrequency.entrySet()) {
            if (e.getValue() >= 3) {
                for (char c : e.getKey().toCharArray()) {
                    overusedLetters.add(c);
                }
            }
        }

        if (!overusedLetters.isEmpty()) {
            logger.log("Обнаружены перегруженные буквы (встречались >=3 раз): " + overusedLetters);
        }

        // 1. Формируем начальный список кандидатов (не использованные слова)
        List<String> candidates = dictionary.getWords().stream()
                .filter(word -> !triedWordsSet.contains(word))
                .filter(word -> {
                    if (overusedLetters.isEmpty()) return true;
                    for (char c : overusedLetters) {
                        if (word.indexOf(c) != -1) {
                            return false;
                        }
                    }
                    return true;
                })
                .collect(Collectors.toList());

        StringBuilder sb = new StringBuilder();
        sb.append("После фильтрации по перегруженным буквам кандидатов осталось: ").append(candidates.size());
        logger.log(sb.toString());

        // 2. Фильтруем по истории ходов (подсказки должны сужать круг, но не исключать ответ)
        for (Move move : history) {
            int before = candidates.size();
            candidates = filterByMove(candidates, move.word, move.result);
            int after = candidates.size();

            sb = new StringBuilder();
            sb.append("Фильтрация по ходу \"").append(move.word)
                    .append("\": кандидатов было ").append(before)
                    .append(", стало ").append(after);
            logger.log(sb.toString());
        }

        // Критическая проверка: загаданное слово не должно исчезнуть из кандидатов
        if (!candidates.contains(answer)) {
            logger.log("КРИТИЧЕСКАЯ ОШИБКА: загаданное слово \"" + answer + "\" исключено из кандидатов!");
            throw new IllegalStateException("Логическая ошибка: загаданное слово потеряно при фильтрации подсказок.");
        }

        // 3. Пытаемся выбрать случайную подсказку из отфильтрованных кандидатов
        String hint = null;
        if (!candidates.isEmpty()) {
            int idx = (int) (Math.random() * candidates.size());
            hint = candidates.get(idx);
        }

        // 4. Fallback: если кандидатов нет или выбранный вариант уже использован — ищем любое неиспользованное слово
        if (hint == null || triedWordsSet.contains(hint)) {
            logger.log("Отфильтрованные кандидаты пусты или все варианты уже использованы. Переходим к fallback.");

            List<String> allWords = dictionary.getWords();
            List<String> fallbackCandidates = allWords.stream()
                    .filter(w -> !triedWordsSet.contains(w))
                    .toList();

            if (!fallbackCandidates.isEmpty()) {
                int idx = (int) (Math.random() * fallbackCandidates.size());
                hint = fallbackCandidates.get(idx);
                logger.log("Подсказка (fallback): \"" + hint + "\" выбрана из полного словаря.");
            } else {
                // Крайний случай: все слова уже использованы — такого быть не должно при корректной логике игры,
                // но на всякий случай выбрасываем исключение вместо зацикливания
                logger.log("Невозможно подобрать подсказку: все слова словаря уже использованы.");
                throw new GameException("Невозможно предложить подсказку: все допустимые слова уже были введены.");
            }
        }

        sb = new StringBuilder();
        sb.append("Подсказка: \"").append(hint)
                .append("\" (из ").append(candidates.isEmpty() ? "fallback" : candidates.size())
                .append(" подходящих вариантов)");
        logger.log(sb.toString());

        return hint;
    }


    private List<String> filterByMove(List<String> candidates, String guess, char[] result) {
        return candidates.stream()
                .filter(candidate -> isCandidateValid(candidate, guess, result))
                .collect(Collectors.toList());
    }

    private boolean isCandidateValid(String candidate, String guess, char[] result) {
        Map<Character, Integer> requiredCounts = new HashMap<>();

        for (int i = 0; i < result.length; i++) {
            char r = result[i];
            char g = guess.charAt(i);
            char c = candidate.charAt(i);

            if (r == '+') {
                if (c != g) {
                    return false;
                }
                requiredCounts.put(g, requiredCounts.getOrDefault(g, 0) + 1);
            } else if (r == '^') {
                if (c == g) {
                    return false;
                }
                if (!candidate.contains(String.valueOf(g))) {
                    return false;
                }
                requiredCounts.put(g, requiredCounts.getOrDefault(g, 0) + 1);
            } else if (r == '-') {
                boolean hasGreenOrYellow = false;
                for (int j = 0; j < result.length; j++) {
                    if ((result[j] == '+' || result[j] == '^') && guess.charAt(j) == g) {
                        hasGreenOrYellow = true;
                        break;
                    }
                }
                if (!hasGreenOrYellow) {
                    if (candidate.contains(String.valueOf(g))) {
                        return false;
                    }
                }
            }
        }

        for (Map.Entry<Character, Integer> e : requiredCounts.entrySet()) {
            char ch = e.getKey();
            int need = e.getValue();
            long countInCandidate = candidate.chars().filter(c -> c == ch).count();
            if (countInCandidate < need) {
                return false;
            }
        }

        return true;
    }
}
