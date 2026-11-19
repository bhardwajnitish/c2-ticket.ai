package com.example.rag.grounding;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

@Component
public class RagAnswerGroundingReviewer {

    private static final Pattern DURATION =
            Pattern.compile(
                    "(?i)\\b(\\d+)\\s*(minutes?|mins?|hours?|hrs?|days?)\\b");

    private static final Pattern DURATION_SUBJECT =
            Pattern.compile(
                    "(?i)(password\\s+reset|reset\\s+link|expire|expir|validity|duration)");

    public boolean hasDurationContradiction(String answerText, List<ContextChunk> contextChunks) {
        String contextText = joinContext(contextChunks);
        if (!DURATION_SUBJECT.matcher(answerText).find()
                && !DURATION_SUBJECT.matcher(contextText).find()) {
            return false;
        }

        List<Long> contextMinutes = extractMinutes(contextText);
        List<Long> answerMinutes = extractMinutes(answerText);
        if (contextMinutes.isEmpty() || answerMinutes.isEmpty()) {
            return false;
        }

        boolean sharesSubject =
                DURATION_SUBJECT.matcher(answerText).find()
                        && DURATION_SUBJECT.matcher(contextText).find();
        if (!sharesSubject) {
            return false;
        }

        for (long answerMin : answerMinutes) {
            boolean supported = false;
            for (long contextMin : contextMinutes) {
                if (answerMin == contextMin) {
                    supported = true;
                    break;
                }
            }
            if (!supported) {
                return true;
            }
        }
        return false;
    }

    private static String joinContext(List<ContextChunk> contextChunks) {
        StringBuilder builder = new StringBuilder();
        for (ContextChunk chunk : contextChunks) {
            if (!builder.isEmpty()) {
                builder.append(' ');
            }
            builder.append(chunk.content());
        }
        return builder.toString();
    }

    private static List<Long> extractMinutes(String text) {
        List<Long> minutes = new ArrayList<>();
        Matcher matcher = DURATION.matcher(text);
        while (matcher.find()) {
            long value = Long.parseLong(matcher.group(1));
            String unit = matcher.group(2).toLowerCase(Locale.ROOT);
            minutes.add(toMinutes(value, unit));
        }
        return minutes;
    }

    private static long toMinutes(long value, String unit) {
        if (unit.startsWith("day")) {
            return value * 24L * 60L;
        }
        if (unit.startsWith("hour") || unit.startsWith("hr")) {
            return value * 60L;
        }
        return value;
    }
}