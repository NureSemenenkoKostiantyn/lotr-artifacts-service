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
import java.util.concurrent.*;
import java.util.stream.Stream;

public class ArtifactStatsService {


    private final JsonFactory jsonFactory = new JsonFactory();

    private static final String MULTI_VALUE_SEPARATOR = ",";

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
                        String[] values = rawValue.split(MULTI_VALUE_SEPARATOR);

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

        Map<String, Long> result = new HashMap<>();

        if (!Files.isDirectory(folder)) {
            throw new IllegalArgumentException("Not a directory: " + folder);
        }

        List<Path> jsonFiles;
        try (Stream<Path> stream = Files.list(folder)) {
            jsonFiles = stream
                    .filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString()
                            .toLowerCase(Locale.ROOT)
                            .endsWith(".json"))
                    .toList();
        }

        if (jsonFiles.isEmpty()) {
            System.out.println("No JSON files in " + folder);
            return result;
        }



        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {

            List<Callable<Map<String, Long>>> tasks = jsonFiles.stream()
                    .map(path -> (Callable<Map<String, Long>>) () -> processSingleFile(path, attribute))
                    .toList();

            var futures = executor.invokeAll(tasks);

            for (Future<Map<String, Long>> future : futures) {
                try {
                    Map<String, Long> fileStats = future.get();
                    fileStats.forEach((key, value) -> result.merge(key, value,  Long::sum));
                } catch (ExecutionException e) {
                    System.err.println("Error processing file: " + e.getCause());
                }
            }

        }

        return result;
    }
}
