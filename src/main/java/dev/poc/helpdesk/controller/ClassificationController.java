package dev.poc.helpdesk.controller;

import dev.poc.helpdesk.ai.ClassificationService;
import dev.poc.helpdesk.controller.dto.CategorySuggestion;
import dev.poc.helpdesk.controller.dto.SuggestionRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/classification")
public class ClassificationController {

    private final ClassificationService classificationService;

    public ClassificationController(ClassificationService classificationService) {
        this.classificationService = classificationService;
    }

    @PostMapping("/category")
    public CategorySuggestion suggestCategory(@Valid @RequestBody SuggestionRequest req) {
        return classificationService.suggestCategory(req.title(), req.description())
                .map(CategorySuggestion::of)
                .orElseGet(CategorySuggestion::unavailable);
    }
}