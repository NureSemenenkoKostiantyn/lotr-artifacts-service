package com.profitsoft.lotrstat.statistics;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.profitsoft.lotrstat.model.ArtifactAttribute;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.*;
import java.util.stream.Stream;

public class ArtifactStatsService {

    // value -> count (e.g. "Gondor" -> 10)
    private final ConcurrentMap<String, Long> stats = new ConcurrentHashMap<>();

    private final JsonFactory jsonFactory = new JsonFactory();

    public Map<String, Long> getStats() {
        return stats;
    }

    /**
     * Parse ONE file using Jackson Streaming API and update statistics
     * for the given attribute.
     *
     * Assumes each file contains a single JSON object representing Artifact.
     */
    public void processSingleFile(Path file, ArtifactAttribute attribute) {
        System.out.println("Processing Single File");
        System.out.println(file);
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
                        return;
                    }

                    if (!attribute.isMultiValued()) {
                        stats.merge(rawValue.trim(), 1L, Long::sum);
                    } else {
                        String[] values = rawValue.split(",");

                        for (String v : values) {
                            String cleaned = v.trim();
                            if (!cleaned.isEmpty()) {
                                stats.merge(cleaned, 1L, Long::sum);
                            }
                        }
                    }
                }
            }

        } catch (IOException e) {
            System.err.println("Failed to process file " + file + ": " + e.getMessage());
        }
    }

    public void processAllFilesInFolder(Path folder, ArtifactAttribute attribute)
            throws IOException, InterruptedException {

        int threads = Runtime.getRuntime().availableProcessors();
        boolean finished;
        try (ExecutorService executor = Executors.newFixedThreadPool(threads)) {

            try (Stream<Path> files = Files.list(folder)) {
                files
                        .filter(p -> p.toString().toLowerCase().endsWith(".json"))
                        .forEach(path -> executor.submit(() -> processSingleFile(path, attribute)));
            }

            executor.shutdown();
            finished = executor.awaitTermination(10, TimeUnit.MINUTES);
        }
        if (!finished) {
            System.err.println("Timeout while waiting for tasks to finish");
        }
        System.out.println(getStats());

    }
}
