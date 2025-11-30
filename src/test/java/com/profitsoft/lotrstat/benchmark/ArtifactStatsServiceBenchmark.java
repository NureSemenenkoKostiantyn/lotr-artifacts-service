package com.profitsoft.lotrstat.benchmark;

import com.profitsoft.lotrstat.model.ArtifactAttribute;
import com.profitsoft.lotrstat.statistics.ArtifactStatsService;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@State(Scope.Benchmark)
public class ArtifactStatsServiceBenchmark {

    private final ArtifactStatsService service = new ArtifactStatsService();

    @Param({"1", "2", "4", "8"})
    public int threadCount;

    @Param({"tags"})
    public String attributeName;

    private Path tempDir;
    private ArtifactAttribute attribute;

    @Setup(Level.Trial)
    public void setUp() throws IOException {
        attribute = ArtifactAttribute.fromString(attributeName);
        tempDir = Files.createTempDirectory("artifacts-benchmark-");

        int fileCount = 50;
        int recordsPerFile = 200;

        for (int i = 0; i < fileCount; i++) {
            Path file = tempDir.resolve("artifact-" + i + ".json");
            Files.writeString(file, buildJson(recordsPerFile));
        }
    }

    @TearDown(Level.Trial)
    public void tearDown() throws IOException {
        if (tempDir != null && Files.exists(tempDir)) {
            try (var files = Files.list(tempDir)) {
                files.forEach(path -> {
                    try {
                        Files.deleteIfExists(path);
                    } catch (IOException ignored) {
                    }
                });
            }
            Files.deleteIfExists(tempDir);
        }
    }

    private String buildJson(int records) {
        StringBuilder builder = new StringBuilder();
        builder.append('[');
        for (int i = 0; i < records; i++) {
            if (i > 0) {
                builder.append(',');
            }
            builder.append('{')
                    .append("\"name\":\"Artifact ")
                    .append(i)
                    .append('\"')
                    .append(',')
                    .append("\"creator\":\"Creator ")
                    .append(i % 10)
                    .append('\"')
                    .append(',')
                    .append("\"origin\":\"Realm ")
                    .append(i % 5)
                    .append('\"')
                    .append(',')
                    .append("\"tags\":\"")
                    .append(randomTags())
                    .append('\"')
                    .append(',')
                    .append("\"year_created\":\"")
                    .append(thirdAgeYear(i))
                    .append('\"')
                    .append(',')
                    .append("\"power_level\":\"")
                    .append(i % 7)
                    .append('\"')
                    .append('}');
        }
        builder.append(']');
        return builder.toString();
    }

    private String randomTags() {
        String[] tags = {"ring", "blade", "heirloom", "elven", "dwarven", "orcish", "artifact", "relic"};
        ThreadLocalRandom random = ThreadLocalRandom.current();
        String first = tags[random.nextInt(tags.length)];
        String second = tags[random.nextInt(tags.length)];
        return first + ", " + second;
    }

    private String thirdAgeYear(int offset) {
        int baseYear = 1000;
        return "TA-" + (baseYear + offset);
    }

    @Benchmark
    public Map<String, Long> virtualThreads() throws Exception {
        return service.processAllFilesInFolder(tempDir, attribute);
    }

    @Benchmark
    public Map<String, Long> fixedThreadPool() throws Exception {
        return service.processAllFilesInFolderWithThreadPool(tempDir, attribute, threadCount);
    }
}