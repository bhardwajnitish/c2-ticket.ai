package com.example.eval;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class RetrievalCalibrationScore {

    private RetrievalCalibrationScore() {}

    public record Result(
            String caseId,
            boolean passed,
            Set<String> matchedIncludeTitles,
            Set<String> matchedExcludeTitles) {}

    public static Result score(
            String caseId,
            List<String> mustIncludeTitles,
            List<String> mustExcludeTitles,
            Set<String> retrievedTitles) {
        Set<String> includesFound = new HashSet<>();
        for (String title : mustIncludeTitles) {
            if (retrievedTitles.contains(title)) {
                includesFound.add(title);
            }
        }
        Set<String> excludesHit = new HashSet<>();
        for (String title : mustExcludeTitles) {
            if (retrievedTitles.contains(title)) {
                excludesHit.add(title);
            }
        }
        boolean passed =
                includesFound.size() == mustIncludeTitles.size() && excludesHit.isEmpty();
        return new Result(caseId, passed, includesFound, excludesHit);
    }
}