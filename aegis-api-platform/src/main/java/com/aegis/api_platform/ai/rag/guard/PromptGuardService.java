package com.aegis.api_platform.ai.rag.guard;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Pattern;

@Service
@Slf4j
public class PromptGuardService {

    // Patterns that indicate prompt injection attempts
    private static final List<Pattern> INJECTION_PATTERNS = List.of(
            Pattern.compile("ignore (previous|all|above) instructions",
                    Pattern.CASE_INSENSITIVE),
            Pattern.compile("you are now", Pattern.CASE_INSENSITIVE),
            Pattern.compile("act as (a|an)?\\s+\\w+", Pattern.CASE_INSENSITIVE),
            Pattern.compile("forget (everything|all|previous)",
                    Pattern.CASE_INSENSITIVE),
            Pattern.compile("(system|assistant|user)\\s*:", Pattern.CASE_INSENSITIVE),
            Pattern.compile("jailbreak", Pattern.CASE_INSENSITIVE),
            Pattern.compile("DAN", Pattern.CASE_INSENSITIVE),
            Pattern.compile("<(script|iframe|img)[^>]*>", Pattern.CASE_INSENSITIVE)
    );

    // Only analytics-related queries are allowed
    private static final List<Pattern> ALLOWED_DOMAIN_PATTERNS = List.of(
            Pattern.compile("(usage|request|api|quota|rate|traffic|" +
                            "analytics|tenant|limit|error|latency|performance|" +
                            "calls|hits|count|month|week|day|exceed|fail)",
                    Pattern.CASE_INSENSITIVE)
    );

    private static final int MAX_QUERY_LENGTH = 500;

    public void validate(String query) {

        if (query == null || query.isBlank()) {
            throw new IllegalArgumentException("Query cannot be empty");
        }

        if (query.length() > MAX_QUERY_LENGTH) {
            throw new IllegalArgumentException(
                    "Query too long. Maximum " + MAX_QUERY_LENGTH + " characters allowed"
            );
        }

        // Check for injection patterns
        for (Pattern pattern : INJECTION_PATTERNS) {
            if (pattern.matcher(query).find()) {
                log.warn("Prompt injection attempt detected: {}",
                        query.substring(0, Math.min(query.length(), 100)));
                throw new IllegalArgumentException(
                        "Query contains disallowed content"
                );
            }
        }

        // Check query is analytics-related
        boolean isDomainRelevant = ALLOWED_DOMAIN_PATTERNS.stream()
                .anyMatch(p -> p.matcher(query).find());

        if (!isDomainRelevant) {
            throw new IllegalArgumentException(
                    "Query is outside the analytics domain. " +
                            "Please ask about API usage, quotas, or traffic."
            );
        }
    }
}
