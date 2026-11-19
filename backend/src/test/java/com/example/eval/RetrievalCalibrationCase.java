package com.example.eval;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record RetrievalCalibrationCase(
        String id,
        String query,
        List<String> mustIncludeTitles,
        List<String> mustExcludeTitles) {}