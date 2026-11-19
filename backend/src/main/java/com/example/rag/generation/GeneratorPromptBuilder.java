package com.example.rag.generation;

import org.springframework.stereotype.Component;

@Component
public class GeneratorPromptBuilder {

    public static final String GROUNDING_INSTRUCTIONS_VERBATIM =
            """
            Answer the user's question using only the supplied ticket context.
            Do not use general knowledge to answer support-specific questions.
            If the supplied context does not contain enough information to answer the question, explicitly state that no relevant tickets were found.
            Do not invent ticket IDs, events, causes, resolutions, users, or dates.
            Every factual claim about a ticket must be supported by the retrieved ticket context.
            """
                    .trim();

    public String build(String userQuery, String ticketContext) {
        return GROUNDING_INSTRUCTIONS_VERBATIM
                + "\n\n--- Ticket context ---\n"
                + ticketContext
                + "\n--- End ticket context ---\n\nUser question: "
                + userQuery
                + "\n\nAnswer:";
    }
}