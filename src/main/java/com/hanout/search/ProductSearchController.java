package com.hanout.search;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
public class ProductSearchController {

    private final SearchService searchService;

    public ProductSearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping
    public ResponseEntity<List<SearchService.SearchResultItem>> search(
            @RequestParam(required = false, defaultValue = "") String q,
            @RequestParam(required = false, defaultValue = "ar") String lang) {
        List<SearchService.SearchResultItem> results = searchService.search(q, lang);
        return ResponseEntity.ok(results);
    }
}
