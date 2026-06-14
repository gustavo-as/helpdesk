package dev.poc.helpdesk.ai;

import dev.poc.helpdesk.domain.enumerator.Category;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ClassificationService {

    private final ChatClient chatClient;

    public ClassificationService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    public Optional<Category> suggestCategory(String title, String description) {
        String answer = chatClient.prompt()
                .system("""
                        You classify support tickets into exactly one category.
                        Valid categories: TECHNICAL, BILLING, ACCOUNT, GENERAL.
                        Reply with ONLY the category word, nothing else.
                        """)
                .user("Title: %s%nDescription: %s".formatted(title, description))
                .call()
                .content();

        return parse(answer);
    }

    private Optional<Category> parse(String raw) {
        if (raw == null) {
            return Optional.empty();
        }
        String cleaned = raw.trim().toUpperCase();
        for (Category category : Category.values()) {
            if (cleaned.equals(category.name())) {
                return Optional.of(category);
            }
        }
        return Optional.empty();
    }
}