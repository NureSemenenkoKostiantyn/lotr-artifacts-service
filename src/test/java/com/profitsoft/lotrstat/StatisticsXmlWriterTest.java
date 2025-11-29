package com.profitsoft.lotrstat;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.profitsoft.lotrstat.model.ArtifactAttribute;
import com.profitsoft.lotrstat.model.StatItem;
import com.profitsoft.lotrstat.model.StatisticsXmlDto;
import com.profitsoft.lotrstat.xml.StatisticsXmlWriter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StatisticsXmlWriterTest {
    private StatisticsXmlWriter xmlWriter;
    private final XmlMapper xmlMapper = new XmlMapper();

    @TempDir
    public Path tempDir;

    @BeforeEach
    public void setup(){
        xmlWriter = new StatisticsXmlWriter();
    }

    @Test
    void write_shouldSortItemsByCountDescending() throws IOException {
        Path outputFile = tempDir.resolve("sorted_stats.xml");

        Map<String, Long> stats = new HashMap<>();
        stats.put("A", 1L);
        stats.put("B", 10L);
        stats.put("C", 5L);

        xmlWriter.write(outputFile, ArtifactAttribute.NAME, stats);

        StatisticsXmlDto dto =
                xmlMapper.readValue(outputFile.toFile(), StatisticsXmlDto.class);

        Assertions.assertNotNull(dto);
        List<StatItem> items = dto.getItems();
        Assertions.assertNotNull(items);
        Assertions.assertEquals(3, items.size(), "There should be 3 items");

        Assertions.assertEquals("B", items.get(0).getValue());
        Assertions.assertEquals(10L, items.get(0).getCount());

        Assertions.assertEquals("C", items.get(1).getValue());
        Assertions.assertEquals(5L, items.get(1).getCount());

        Assertions.assertEquals("A", items.get(2).getValue());
        Assertions.assertEquals(1L, items.get(2).getCount());
    }

    @Test
    public void write_shouldCreateXmlFileWithContent() throws IOException {
        Path outputFile = tempDir.resolve("stats.xml");

        Map<String, Long> stats = new HashMap<>();
        stats.put("ring", 1L);
        stats.put("evil", 5L);
        stats.put("legendary", 2L);

        xmlWriter.write(outputFile, ArtifactAttribute.TAGS, stats);

        Assertions.assertTrue(Files.exists(outputFile), "XML file should be created");
        String xml = Files.readString(outputFile);
        Assertions.assertNotNull(xml);
        Assertions.assertFalse(xml.isBlank(), "XML file should not be empty");
    }

    @Test
    void write_shouldWriteItemsWithCorrectValuesAndCounts() throws IOException {
        Path outputFile = tempDir.resolve("stats_items.xml");

        Map<String, Long> stats = new HashMap<>();
        stats.put("Mordor", 10L);
        stats.put("Gondor", 5L);

        xmlWriter.write(outputFile, ArtifactAttribute.ORIGIN, stats);

        StatisticsXmlDto dto =
                xmlMapper.readValue(outputFile.toFile(), StatisticsXmlDto.class);

        Assertions.assertNotNull(dto);
        List<StatItem> items = dto.getItems();
        Assertions.assertNotNull(items);
        Assertions.assertEquals(2, items.size());

        boolean hasMordor = items.stream()
                .anyMatch(i -> "Mordor".equals(i.getValue()) && i.getCount() == 10L);
        boolean hasGondor = items.stream()
                .anyMatch(i -> "Gondor".equals(i.getValue()) && i.getCount() == 5L);

        Assertions.assertTrue(hasMordor, "Items should contain Mordor with count 10");
        Assertions.assertTrue(hasGondor, "Items should contain Gondor with count 5");
    }
}
