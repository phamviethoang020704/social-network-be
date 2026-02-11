package com.example.mangxahoi.Controller;

import com.example.mangxahoi.DTO.Response.Search.SearchListResponse;
import com.example.mangxahoi.DTO.Response.Search.SearchResponse;
import com.example.mangxahoi.DTO.Response.Search.SuggestItem;
import com.example.mangxahoi.Entity.UserEntity;
import com.example.mangxahoi.Security.CustomUserDetails;
import com.example.mangxahoi.Service.Search.RecentSearchService;
import com.example.mangxahoi.Service.Search.SearchRenderService;
import com.example.mangxahoi.Service.Search.SearchService;
import com.example.mangxahoi.Service.Search.SearchSuggestService;
import com.sun.security.auth.UserPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/search")
public class SearchController {

    private final SearchSuggestService searchSuggestService;
    private final SearchService searchService;
    private final RecentSearchService recentSearchService;
    private final SearchRenderService searchRenderService;

    public SearchController(SearchSuggestService searchSuggestService, SearchService searchService, RecentSearchService recentSearchService, SearchRenderService searchRenderService) {
        this.searchSuggestService = searchSuggestService;
        this.searchService = searchService;
        this.recentSearchService = recentSearchService;
        this.searchRenderService = searchRenderService;
    }
    private Long currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            throw new RuntimeException("Unauthenticated");
        }

        Object principal = auth.getPrincipal();

        if (principal instanceof CustomUserDetails userDetails) {
            return userDetails.getId();
        }

        throw new RuntimeException("Invalid authentication principal");
    }
    @GetMapping("/suggest")
    public List<SuggestItem> suggest(@RequestParam(required = false) String q) {
        return searchSuggestService.suggest(currentUserId(), q);
    }
    @GetMapping
    public SearchListResponse search(
            @RequestParam String q,
            @RequestParam(defaultValue = "ALL") String type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return searchRenderService.search(currentUserId(), q, type, page, size);
    }

    @GetMapping("/recent")
    public List<SuggestItem> recent() {
        return searchSuggestService.suggest(currentUserId(), ""); // q rỗng trả recent top 8
    }

    @DeleteMapping("/recent/{id}")
    public void deleteRecent(@PathVariable Long id) {
        recentSearchService.deleteOne(currentUserId(), id);
    }
}
