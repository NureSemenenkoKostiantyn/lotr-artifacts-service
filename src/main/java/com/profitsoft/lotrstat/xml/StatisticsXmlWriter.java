package com.profitsoft.lotrstat.xml;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.profitsoft.lotrstat.model.ArtifactAttribute;
import com.profitsoft.lotrstat.model.StatItem;
import com.profitsoft.lotrstat.model.StatisticsXmlDto;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public class StatisticsXmlWriter {

    private final XmlMapper xmlMapper = new XmlMapper();

    public void write(Path outputFile,
                      ArtifactAttribute attribute,
                      Map<String, Long> stats) throws IOException {

        List<StatItem> items = stats.entrySet()
                .stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .map(e -> new StatItem(e.getKey(), e.getValue()))
                .toList();

        StatisticsXmlDto dto =
                new StatisticsXmlDto(attribute.getJsonFieldName(), items);

        String xml = xmlMapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(dto);

        Files.writeString(outputFile, xml);
    }
}

