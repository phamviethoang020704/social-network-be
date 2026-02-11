package com.example.mangxahoi.Service.Search;

import com.example.mangxahoi.DTO.Response.Search.SearchResponse;
import com.example.mangxahoi.DTO.Response.Search.SearchResultItem;
import com.example.mangxahoi.Entity.GroupEntity;
import com.example.mangxahoi.Entity.PostEntity;
import com.example.mangxahoi.Entity.ShareEntity;
import com.example.mangxahoi.Entity.UserEntity;
import com.example.mangxahoi.Repository.GroupRepository;
import com.example.mangxahoi.Repository.PostRepository;
import com.example.mangxahoi.Repository.ShareRepository;
import com.example.mangxahoi.Repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class SearchService {
    private final SearchQueryService searchQueryService;
    private final RecentSearchService recent;
    private final UserRepository userRepo;
    private final GroupRepository groupRepo;
    private final PostRepository postRepo;
    private final ShareRepository shareRepo;

    @Transactional
    public SearchResponse search(Long userId, String q, String type, int page, int size) {
        String query = q == null ? "" : q.trim();
        if (query.isEmpty()) {
            return new SearchResponse(Map.of(), List.of(), page, size);
        }

        // lưu recent keyword (khi user thực sự search)
        recent.record(userId, query);

        int offset = page * size;

        var counts = searchQueryService.countByType(query);
        var hits = searchQueryService.search(query, type, size, offset);

        // group ids theo type
        var userIds = hits.stream().filter(h -> "USER".equals(h.type())).map(h -> h.id()).toList();
        var groupIds = hits.stream().filter(h -> "GROUP".equals(h.type())).map(h -> h.id()).toList();
        var postIds = hits.stream().filter(h -> "POST".equals(h.type())).map(h -> h.id()).toList();
        var shareIds = hits.stream().filter(h -> "SHARE".equals(h.type())).map(h -> h.id()).toList();

        var users = userRepo.findAllById(userIds).stream().collect(Collectors.toMap(UserEntity::getId, u -> u));
        var groups = groupRepo.findAllById(groupIds).stream().collect(Collectors.toMap(GroupEntity::getId, g -> g));
        var posts = postRepo.findAllById(postIds).stream().collect(Collectors.toMap(PostEntity::getId, p -> p));
        var shares = shareRepo.findAllById(shareIds).stream().collect(Collectors.toMap(ShareEntity::getId, s -> s));

        var items = new ArrayList<SearchResultItem>();

        for (var h : hits) {
            switch (h.type()) {
                case "USER" -> {
                    var u = users.get(h.id());
                    items.add(new SearchResultItem(
                            "USER", h.id(), h.score(),
                            u != null ? u.getFullName() : h.title(),
                            u != null ? u.getAvatar() : null,
                            null, null, null
                    ));
                }
                case "GROUP" -> {
                    var g = groups.get(h.id());
                    items.add(new SearchResultItem(
                            "GROUP", h.id(), h.score(),
                            null, null,
                            g != null ? g.getGroupName() : h.title(),
                            g != null ? g.getCoverPhoto() : null,
                            null
                    ));
                }
                case "POST" -> {
                    var p = posts.get(h.id());
                    String text = p != null ? excerpt(p.getContent(), 200) : h.title();
                    items.add(new SearchResultItem("POST", h.id(), h.score(), null, null, null, null, text));
                }
                case "SHARE" -> {
                    var s = shares.get(h.id());
                    String text = s != null ? excerpt(s.getCaption(), 200) : h.title();
                    items.add(new SearchResultItem("SHARE", h.id(), h.score(), null, null, null, null, text));
                }
            }
        }

        return new SearchResponse(counts, items, page, size);
    }

    private String excerpt(String s, int max) {
        if (s == null) return "";
        String c = s.trim().replaceAll("\\s+", " ");
        return c.length() <= max ? c : c.substring(0, max) + "…";
    }

}
