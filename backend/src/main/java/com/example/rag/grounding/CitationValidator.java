package com.example.rag.grounding;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

@Component
public class CitationValidator {

    private static final Pattern TICKET_ID = Pattern.compile("TKT-[0-9]+");

    public boolean allTicketIdsInKeepSet(String answerText, Set<String> keepSetTicketIds) {
        Matcher matcher = TICKET_ID.matcher(answerText);
        while (matcher.find()) {
            if (!keepSetTicketIds.contains(matcher.group())) {
                return false;
            }
        }
        return true;
    }

    public Set<String> ticketIdsMentioned(String answerText) {
        Set<String> ids = new HashSet<>();
        Matcher matcher = TICKET_ID.matcher(answerText);
        while (matcher.find()) {
            ids.add(matcher.group());
        }
        return ids;
    }
}