# LOTR Artifacts Statistics Service
---

## 📌 Core Components

- **`Artifact`** — model representing a single artifact with fields `name`, `creator`, `origin`, `tags`, `year_created`, `power_level`.
- **`ArtifactAttribute`** — enum of available attributes and metadata (e.g., whether the field contains multiple comma-separated values like `tags`).
- **`ArtifactStatsService`** — reads JSON files via virtual threads, extracts attribute values, and produces a `Map<value → count>`.
- **`StatItem`** / **`StatisticsXmlDto`** — DTOs defining XML serialization format.
- **`StatisticsXmlWriter`** — converts the statistics map into sorted XML and writes it to disk.
- **`Main`** — entry point that accepts a directory path and an attribute name.

---

## 📂 Input Example

Each JSON file in the folder must contain an array of artifact objects:

```json
[
  {
    "name": "The One Ring",
    "creator": "Sauron",
    "origin": "Mount Doom",
    "tags": "ring,evil,legendary",
    "year_created": 1600,
    "power_level": 100
  },
  {
    "name": "Narsil",
    "creator": "Telchar of Nogrod",
    "origin": "Nogrod",
    "tags": "sword,dwarf-forged,legendary",
    "year_created": 450,
    "power_level": 88
  }
]
```

---

## 📄 Output Example (XML)

```xml
<statistics attribute="tags">
  <item>
    <value>legendary</value>
    <count>4</count>
  </item>
  <item>
    <value>sword</value>
    <count>4</count>
  </item>
  <item>
    <value>elven</value>
    <count>3</count>
  </item>
</statistics>
```

---

## ▶️ Running the Application

```bash
java -jar app.jar <folderPath> <attribute>
```

The result will be saved as:

```
statistics_by_<attribute>.xml
```

in the same directory.

---

# 📊 Benchmark Results (JMH)

The benchmarks measure:

- **Throughput** (`thrpt`) — operations per second  
- **Average Time** (`avgt`) — seconds per operation  

Workloads vary by:

- number of files (`fileCount`)
- objects per file (`recordsPerFile`)
- thread strategy (fixed thread pool vs virtual threads)

---

## 🔥 Throughput (ops/sec)

| fileCount | records | fixed-1 | fixed-2 | fixed-4 | fixed-8 | virtual |
|-----------|---------|---------|---------|---------|---------|---------|
| **50**    | **200**     | 98.934 | 138.160 | 181.069 | 219.272 | **260.437** |
| **50**    | **20 000**  | 2.148  | 2.888   | 4.608   | 7.733   | **7.785** |
| **500**   | **200**     | 9.900  | 16.315  | 24.944  | 26.078  | **28.225** |
| **500**   | **20 000**  | 0.188  | 0.228   | 0.495   | 0.629   | **0.854** |

---

## ⏱️ Average Time (seconds per operation)

| fileCount | records | fixed-1 | fixed-2 | fixed-4 | fixed-8 | virtual |
|-----------|---------|---------|---------|---------|---------|---------|
| **50**    | **200**     | 0.011 | 0.009 | 0.006 | 0.005 | **0.004** |
| **50**    | **20 000**  | 0.471 | 0.358 | 0.227 | 0.134 | **0.125** |
| **500**   | **200**     | 0.097 | 0.068 | 0.050 | 0.040 | **0.036** |
| **500**   | **20 000**  | 4.714 | 4.477 | 1.965 | 1.313 | **1.225** |

---

# 🧠 Benchmark Summary

### ✔ Many small files → **Virtual Threads are significantly faster**  
(IO-bound, low CPU pressure)

### ✔ Few large files → **Performance becomes CPU-bound**  
(Virtual Threads ≈ Fixed Thread Pool)

### ✔ Many large files → **Virtual Threads still lead**  
(lower scheduling overhead, less contention)