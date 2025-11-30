package com.profitsoft.lotrstat;

import com.profitsoft.lotrstat.model.ArtifactAttribute;
import com.profitsoft.lotrstat.statistics.ArtifactStatsService;
import com.profitsoft.lotrstat.xml.StatisticsXmlWriter;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Optional;


public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        if (args.length < 2) {
            System.err.println("Usage: java -jar app.jar <folderPath> <attribute> [threadCount]");
            System.exit(1);
        }

        Path path = Paths.get(args[0]);
        ArtifactAttribute attribute = ArtifactAttribute.fromString(args[1]);

        ArtifactStatsService service = new ArtifactStatsService();
        Map<String, Long> stats = chooseExecutor(args, service, path, attribute);

        Path xmlOutput = path.resolve("statistics_by_" + attribute.getJsonFieldName() + ".xml");
        StatisticsXmlWriter xmlWriter = new StatisticsXmlWriter();
        try {
            xmlWriter.write(xmlOutput, attribute, stats);
            System.out.println("XML written to: " + xmlOutput.toAbsolutePath());
        } catch (IOException e) {
            System.err.println("Failed to write XML: " + e.getMessage());
        }
    }

    private static Map<String, Long> chooseExecutor(
            String[] args,
            ArtifactStatsService service,
            Path path,
            ArtifactAttribute attribute
    ) throws IOException, InterruptedException {

        Optional<Integer> threadCount = parseThreadCount(args);

        if (threadCount.isPresent()) {
            int poolSize = threadCount.get();
            System.out.printf("Processing with fixed thread pool (%d threads)...%n", poolSize);
            return service.processAllFilesInFolderWithThreadPool(path, attribute, poolSize);
        } else {
            System.out.println("Processing with virtual threads...");
            return service.processAllFilesInFolder(path, attribute);
        }
    }

    private static Optional<Integer> parseThreadCount(String[] args) {
        if (args.length < 3) {
            return Optional.empty();
        }

        try {
            return Optional.of(Integer.parseInt(args[2]));
        } catch (NumberFormatException e) {
            System.err.println("Invalid thread count: " + args[2]);
            System.exit(1);
            return Optional.empty();
        }
    }
}