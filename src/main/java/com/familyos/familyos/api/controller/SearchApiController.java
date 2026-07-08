package com.familyos.familyos.api.controller;

import com.familyos.familyos.api.dto.SearchResponse;
import com.familyos.familyos.api.service.SearchApiService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/search")
public class SearchApiController {

    private final SearchApiService searchApiService;

    public SearchApiController(SearchApiService searchApiService) {
        this.searchApiService = searchApiService;
    }

    @GetMapping
    public SearchResponse search(@RequestParam("q") String query) {
        return searchApiService.search(query);
    }
}
