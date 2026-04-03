package com.collabhub.controller;

import com.collabhub.dto.SearchResult;
import com.collabhub.repository.SearchRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/search")
@Tag(name = "Search", description = "Global full-text search")
public class SearchController {

    private final SearchRepository searchRepository;

    public SearchController(SearchRepository searchRepository) {
        this.searchRepository = searchRepository;
    }

    @GetMapping
    @Operation(summary = "Search tasks and projects by keyword")
    public List<SearchResult> search(
            @RequestParam String q,
            @RequestParam(defaultValue = "10") int limit) {

        if (q == null || q.trim().length() < 2) {
            return List.of();
        }
        return searchRepository.search(q.trim(), Math.min(limit, 50));
    }
}
