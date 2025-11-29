package com.profitsoft.lotrstat.xml;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.profitsoft.lotrstat.model.ArtifactAttribute;
import com.profitsoft.lotrstat.model.StatItem;
import com.profitsoft.lotrstat.model.StatisticsXmlDto;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public class StatisticsXmlWriter {

    private static final XmlMapper xmlMapper = new XmlMapper();

    public void write(Path outputFile,
                      ArtifactAttribute attribute,
                      Map<String, Long> stats) throws IOException {

        List<StatItem> items = toSortedStatItems(stats);

        StatisticsXmlDto dto = new StatisticsXmlDto(attribute.getJsonFieldName(), items);

        xmlMapper.writerWithDefaultPrettyPrinter()
                .writeValue(outputFile.toFile(), dto);
    }

    private List<StatItem> toSortedStatItems(Map<String, Long> stats) {
        return stats.entrySet()
                .stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .map(e -> new StatItem(e.getKey(), e.getValue()))
                .toList();
    }
}

