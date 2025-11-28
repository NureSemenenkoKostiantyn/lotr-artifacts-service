package com.profitsoft.lotrstat;

import com.profitsoft.lotrstat.model.ArtifactAttribute;
import com.profitsoft.lotrstat.statistics.ArtifactStatsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class ArtifactStatsServiceTest {
    private ArtifactStatsService service;

    @TempDir
    public Path tempDir;

    @BeforeEach
    public void setup(){
        service = new ArtifactStatsService();
    }

    @Test
    public void processSingleFile_countsSimpleAttribute() throws Exception{
        String jsonTestData = """
                [
                  {
                    "name": "The One Ring",
                    "origin": "Mount Doom"
                  },
                  {
                    "name": "Narsil",
                    "origin": "Nogrod"
                  }
                ]
                """;

        Path file = tempDir.resolve("Artifacts.json");
        Files.writeString(file, jsonTestData);

        ArtifactAttribute attribute = ArtifactAttribute.ORIGIN;
        Map<String, Long> result = service.processSingleFile(file, attribute);

        Assertions.assertEquals(1, result.get("Nogrod"));
        Assertions.assertEquals(1, result.get("Mount Doom"));
        Assertions.assertEquals(2, result.size());
    }

    @Test
    public void processSingleFile_countsMultiValueAttribute() throws Exception{
        String jsonTestData = """
                [
                  {
                    "name": "The One Ring",
                    "tags": "ring,evil,legendary"
                  },
                  {
                    "name": "Narsil",
                    "tags": "sword,dwarf-forged,legendary"
                  }
                ]
                """;

        Path file = tempDir.resolve("Artifacts.json");
        Files.writeString(file, jsonTestData);

        ArtifactAttribute attribute = ArtifactAttribute.TAGS;
        Map<String, Long> result = service.processSingleFile(file, attribute);

        Assertions.assertEquals(2, result.get("legendary"));
        Assertions.assertEquals(1, result.get("sword"));
        Assertions.assertEquals(1, result.get("dwarf-forged"));
        Assertions.assertEquals(1, result.get("ring"));
        Assertions.assertEquals(1, result.get("evil"));
        Assertions.assertEquals(5, result.size());
    }

    @Test
    public void processSingleFile_emptyJsonFile() throws IOException {
        String jsonTestData = "";
        Path file = tempDir.resolve("empty.json");

        Files.writeString(file, jsonTestData);

        ArtifactAttribute attribute = ArtifactAttribute.ORIGIN;
        Map<String, Long> result = service.processSingleFile(file, attribute);

        Assertions.assertEquals(0, result.size());
    }

    @Test
    void processSingleFile_returnsEmptyMap_whenNoMatchingAttribute() throws IOException {
        String json = """
                [
                  { "name": "The One Ring", "origin": "Mordor" }
                ]
                """;

        Path file = tempDir.resolve("no_attribute.json");
        Files.writeString(file, json);

        Map<String, Long> stats = service.processSingleFile(file, ArtifactAttribute.CREATOR);

        Assertions.assertTrue(stats.isEmpty());
    }

    @Test
    void processAllFilesInFolder_aggregatesStatsFromAllFiles() throws Exception{
        String testJsonData1 = """
                [
                  {
                    "name": "The One Ring",
                    "origin": "Mount Doom"
                  },
                  {
                    "name": "Narsil",
                    "origin": "Nogrod"
                  }
                ]
                """;
        Path file1 = tempDir.resolve("Artifacts1.json");
        Files.writeString(file1, testJsonData1);

        String testJsonData2 = """
                [
                  {
                    "name": "Anduril",
                    "origin": "Rivendell"
                  },
                  {
                    "name": "Narsil",
                    "origin": "Nogrod"
                  }
                ]
                """;
        Path file2 = tempDir.resolve("Artifacts2.json");
        Files.writeString(file2, testJsonData2);

        ArtifactAttribute attribute1 = ArtifactAttribute.ORIGIN;

        Map<String, Long> result = service.processAllFilesInFolder(tempDir, attribute1);

        Assertions.assertEquals(2, result.get("Nogrod"));
        Assertions.assertEquals(1, result.get("Rivendell"));
        Assertions.assertEquals(1, result.get("Mount Doom"));
        Assertions.assertEquals(3, result.size());

    }

    @Test
    void processAllFilesInFolder_returnsEmptyMap_whenFolderHasNoJsonFiles() throws Exception {
        Path txtFile = tempDir.resolve("readme.txt");
        Files.writeString(txtFile, "nothing here");

        Map<String, Long> stats =
                service.processAllFilesInFolder(tempDir, ArtifactAttribute.ORIGIN);

        Assertions.assertTrue(stats.isEmpty());
    }

    @Test
    void processAllFilesInFolder_throwsException_whenNotDirectory() {
        Path notDirectory = tempDir.resolve("someFile.json");

        Assertions.assertThrows(IllegalArgumentException.class,
                () -> service.processAllFilesInFolder(notDirectory, ArtifactAttribute.ORIGIN));
    }
}
