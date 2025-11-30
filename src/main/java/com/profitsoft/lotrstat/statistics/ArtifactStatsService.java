package com.profitsoft.lotrstat.statistics;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.profitsoft.lotrstat.model.ArtifactAttribute;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Stream;

public class ArtifactStatsService {


    private final JsonFactory jsonFactory = new JsonFactory();

    public Map<String, Long> processSingleFile(Path file, ArtifactAttribute attribute) throws IOException {
        Map<String, Long> result = new HashMap<>();

        try (JsonParser parser = jsonFactory.createParser(file.toFile())) {

            String currentField = null;

            while (!parser.isClosed()) {
                JsonToken token = parser.nextToken();
                if (token == null) {
                    break;
                }

                if (token == JsonToken.FIELD_NAME) {
                    currentField = parser.currentName();
                } else if (token.isScalarValue() &&
                        attribute.getJsonFieldName().equals(currentField)) {

                    String rawValue = parser.getValueAsString();

                    if (rawValue == null || rawValue.isBlank()) {
                            continue;
                        }

                    if (!attribute.isMultiValued()) {
                            result.merge(rawValue.trim(), 1L, Long::sum);
                        } else {
                            String[] values = rawValue.split(",");

                            for (String v : values) {
                                String cleaned = v.trim();
                                if (!cleaned.isEmpty()) {
                                    result.merge(cleaned, 1L, Long::sum);
                                }
                            }
                        }
                    }
                }
            }
            return result;
        }

        public Map<String, Long> processAllFilesInFolder(Path folder, ArtifactAttribute attribute)
            throws IOException, InterruptedException {
            return processWithExecutor(folder, attribute, Executors.newVirtualThreadPerTaskExecutor());
        }

        public Map<String, Long> processAllFilesInFolderWithThreadPool(
                Path folder,
                ArtifactAttribute attribute,
        int threadCount
    ) throws IOException, InterruptedException {

            if (threadCount < 1) {
                throw new IllegalArgumentException("Thread count must be at least 1");
            }

            ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
            try {
                return processWithExecutor(folder, attribute, executorService);
            } finally {
                executorService.shutdown();
            }
        }

        private Map<String, Long> processWithExecutor(
                Path folder,
                ArtifactAttribute attribute,
                ExecutorService executor
    ) throws IOException, InterruptedException {

            Map<String, Long> result = new HashMap<>();

            if (!Files.isDirectory(folder)) {
                throw new IllegalArgumentException("Not a directory: " + folder);
            }

            List<Path> jsonFiles = listJsonFiles(folder);

            if (jsonFiles.isEmpty()) {
                System.out.println("No JSON files in " + folder);
                return result;
            }

            List<Callable<Map<String, Long>>> tasks = jsonFiles.stream()
                    .map(path -> (Callable<Map<String, Long>>) () -> processSingleFile(path, attribute))
                    .toList();

            var futures = executor.invokeAll(tasks);

            for (Future<Map<String, Long>> future : futures) {
                try {
                    Map<String, Long> fileStats = future.get();
                    fileStats.forEach((key, value) -> result.merge(key, value, Long::sum));
                } catch (ExecutionException e) {
                    System.err.println("Error processing file: " + e.getCause());
                }
            }

            return result;
        }

        private List<Path> listJsonFiles(Path folder) throws IOException {
            try (Stream<Path> stream = Files.list(folder)) {
                return stream
                        .filter(Files::isRegularFile)
                        .filter(p -> p.getFileName().toString()
                                .toLowerCase(Locale.ROOT)
                                .endsWith(".json"))
                        .toList();
            }
        }
    }