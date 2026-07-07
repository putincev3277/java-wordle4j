package ru.yandex.practicum;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LoggerService implements AutoCloseable {
    private final PrintWriter writer;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    // Конструктор для файла
    public LoggerService(String logFilePath) throws IOException {
        Path path = Paths.get(logFilePath).toAbsolutePath();
        Path parent = path.getParent();

        // Создаём родительскую директорию, если её нет (но только если parent != null)
        if (parent != null && !Files.exists(parent)) {
            Files.createDirectories(parent);
        }

        this.writer = new PrintWriter(Files.newBufferedWriter(path, StandardCharsets.UTF_8), true);
    }

    // Конструктор для вывода в поток (удобно для тестов)
    public LoggerService(java.io.OutputStream out) {
        this.writer = new PrintWriter(out, true);
    }

    public void log(String message) {
        String timestamp = LocalDateTime.now().format(formatter);
        writer.println("[" + timestamp + "] " + message);
        writer.flush(); // сразу сбрасываем на диск
    }

    @Override
    public void close() {
        writer.close();
    }
}
