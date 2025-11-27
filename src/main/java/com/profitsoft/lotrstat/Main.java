package com.profitsoft.lotrstat;

import com.profitsoft.lotrstat.model.ArtifactAttribute;
import com.profitsoft.lotrstat.statistics.ArtifactStatsService;
import com.profitsoft.lotrstat.xml.StatisticsXmlWriter;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;


public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        if (args.length < 2) {
            System.err.println("Usage: java -jar app.jar <folderPath> <attribute>");
            System.exit(1);
        }

        Path path = Paths.get(args[0]);
        ArtifactAttribute attribute = ArtifactAttribute.fromString(args[1]);

        ArtifactStatsService service = new ArtifactStatsService();

        service.processAllFilesInFolder(path, attribute);

        Path xmlOutput = path.resolve("statistics_by_" + attribute.getJsonFieldName() + ".xml");
        StatisticsXmlWriter xmlWriter = new StatisticsXmlWriter();
        try {
            xmlWriter.write(xmlOutput, attribute, service.getStats());
            System.out.println("XML written to: " + xmlOutput.toAbsolutePath());
        } catch (IOException e) {
            System.err.println("Failed to write XML: " + e.getMessage());
        }





    }
}