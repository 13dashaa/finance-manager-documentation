package com.example.fmanager.service;

import com.example.fmanager.exception.NoDataToFileException;
import com.example.fmanager.exception.ProcessingFileException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Getter;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Getter
public class LogService {

    private static final String LOG_FILE_PATH = "logs/application.log";
    private static final String LOGS_DIR = "logs/";

    private final Map<String, String> logFiles = new ConcurrentHashMap<>();
    private final Map<String, Boolean> taskStatus = new ConcurrentHashMap<>();

    @Async
    public CompletableFuture<String> generateLogFileForDateAsync(String date) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String existingLogId = findLogIdByDate(date);
                if (existingLogId != null) {
                    return existingLogId;
                }
                Path logPath = Paths.get(LOG_FILE_PATH);
                if (!Files.exists(logPath)) {
                    throw new FileNotFoundException("Log file not found: " + LOG_FILE_PATH);
                }

                List<String> filteredLines;
                try (var lines = Files.lines(logPath)) {
                    filteredLines = lines
                            .filter(line -> line.startsWith(date))
                            .toList();
                }

                if (filteredLines.isEmpty()) {
                    throw new NoDataToFileException("No logs found for the given date");
                }

                Files.createDirectories(Paths.get(LOGS_DIR));
                String logFileName = LOGS_DIR + "logs-" + date + ".log";
                Path logFilePath = Paths.get(logFileName);

                if (Files.exists(logFilePath)) {
                    String logId = UUID.randomUUID().toString();
                    logFiles.put(logId, logFileName);
                    taskStatus.put(logId, true);
                    return logId;
                }

                Files.write(logFilePath, filteredLines);

                String logId = UUID.randomUUID().toString();
                logFiles.put(logId, logFileName);
                taskStatus.put(logId, true);

                return logId;
            } catch (FileNotFoundException e) {
                throw new ProcessingFileException("Log file not found"); // Явное преобразование
            } catch (IOException e) {
                String logId = UUID.randomUUID().toString();
                taskStatus.put(logId, false);
                throw new ProcessingFileException("Error processing file");
            }
        });
    }

    private String findLogIdByDate(String date) {
        for (Map.Entry<String, String> entry : logFiles.entrySet()) {
            if (entry.getValue().contains(date)) {
                return entry.getKey();
            }
        }
        return null;
    }

    public String getLogFilePath(String logId) {
        return logFiles.get(logId);
    }

    public boolean isTaskCompleted(String logId) {
        return taskStatus.getOrDefault(logId, false);
    }
}
