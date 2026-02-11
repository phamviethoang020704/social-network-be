package com.example.mangxahoi.Service.Search;

import com.example.mangxahoi.DTO.Response.Search.SuggestItem;
import com.example.mangxahoi.Entity.GroupEntity;
import com.example.mangxahoi.Entity.UserEntity;
import com.example.mangxahoi.Repository.FriendRepository;
import com.example.mangxahoi.Repository.GroupMemberRepository;
import com.example.mangxahoi.Repository.GroupRepository;
import com.example.mangxahoi.Repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@AllArgsConstructor
public class SearchSuggestService {
    private final SearchQueryService searchQueryService;
    private final FriendRepository friendRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final UpsertService upsertService;
    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final RecentSearchService recentSearchService;

    public List<SuggestItem> suggest(Long currentUserId, String q) {

        // 1) Nếu q rỗng: trả recent top 8
        if (q == null || q.trim().isEmpty()) {
            return recentSearchService.top8(currentUserId).stream()
                    .map(r -> new SuggestItem(
                            "RECENT", null, r.getKeyword(),
                            "SEARCH", null, null,
                            r.getKeyword(), null,
                            null, r.getId(), true
                    ))
                    .toList();
        }

        String query = q.trim();

        // 2) Recent match lên trước (3-5 cái)
        var recent = recentSearchService.suggest(currentUserId, query, 5).stream()
                .map(r -> new SuggestItem(
                        "RECENT", null, r.getKeyword(),
                        "SEARCH", null, null,
                        r.getKeyword(), null,
                        9999.0, r.getId(), true
                ))
                .toList();

        // 3) Lấy friendIds + joinedGroupIds
        var friendIds = new HashSet<>(friendRepository.findFriendUserIds(currentUserId));
        var joinedGroupIds = new HashSet<>(groupMemberRepository.findJoinedGroupIds(currentUserId));

        // 4) Strict hits (giữ nguyên cho người lạ / group chưa vào / post / share)
        var strictHits = searchQueryService.suggest(query, 20);

        // 5) Fuzzy token-match CHỈ cho friend + joined group
        var fuzzyFriendHits = friendIds.isEmpty()
                ? List.<SearchQueryService.SearchHit>of()
                : searchQueryService.suggestFuzzyTokensForIds("USER", new ArrayList<>(friendIds), query, 10);

        var fuzzyGroupHits = joinedGroupIds.isEmpty()
                ? List.<SearchQueryService.SearchHit>of()
                : searchQueryService.suggestFuzzyTokensForIds("GROUP", new ArrayList<>(joinedGroupIds), query, 10);

        // 6) Fetch dữ liệu MySQL cho các item clickable (friend/joined)
        var needUserIds = Stream.concat(
                        fuzzyFriendHits.stream(),
                        strictHits.stream().filter(h -> "USER".equals(h.type()) && friendIds.contains(h.id()))
                )
                .map(SearchQueryService.SearchHit::id)
                .distinct()
                .toList();

        var needGroupIds = Stream.concat(
                        fuzzyGroupHits.stream(),
                        strictHits.stream().filter(h -> "GROUP".equals(h.type()) && joinedGroupIds.contains(h.id()))
                )
                .map(SearchQueryService.SearchHit::id)
                .distinct()
                .toList();

        var userMap = userRepository.findAllById(needUserIds).stream()
                .collect(Collectors.toMap(UserEntity::getId, u -> u));

        var groupMap = groupRepository.findAllById(needGroupIds).stream()
                .collect(Collectors.toMap(GroupEntity::getId, g -> g));

        // 7) Dedupe
        var items = new ArrayList<SuggestItem>();
        var seen = new HashSet<String>();

        // helper
        java.util.function.Consumer<SuggestItem> addUnique = (it) -> {
            if (it == null) return;
            String key = it.type() + "|" + (it.id() == null ? "" : it.id()) + "|" + it.text();
            // recent khác nhau theo recentId
            if ("RECENT".equals(it.type()) && it.recentId() != null) {
                key = "RECENT|" + it.recentId();
            }
            if (seen.add(key)) items.add(it);
        };

        // 8) Add recent trước
        for (var r : recent) {
            addUnique.accept(r);
            if (items.size() >= 15) return items;
        }

        // 9) Add fuzzy friends (clickable)
        for (var h : fuzzyFriendHits) {
            Long id = h.id();
            var u = userMap.get(id);
            if (u == null) continue;
            addUnique.accept(new SuggestItem(
                    "USER", id, u.getFullName(),
                    "NAVIGATE",
                    u.getAvatar(), null,
                    null, "profile",
                    h.score(), null, false
            ));
            if (items.size() >= 15) return items;
        }

        // 10) Add fuzzy joined groups (clickable)
        for (var h : fuzzyGroupHits) {
            Long id = h.id();
            var g = groupMap.get(id);
            if (g == null) continue;
            addUnique.accept(new SuggestItem(
                    "GROUP", id, g.getGroupName(),
                    "NAVIGATE",
                    null, g.getCoverPhoto(),
                    null, "group",
                    h.score(), null, false
            ));
            if (items.size() >= 15) return items;
        }

        // 11) Add strict hits
        for (var h : strictHits) {
            String type = h.type();
            Long id = h.id();

            if ("USER".equals(type)) {
                if (friendIds.contains(id)) {
                    var u = userMap.get(id);
                    if (u != null) {
                        addUnique.accept(new SuggestItem(
                                "USER", id, u.getFullName(),
                                "NAVIGATE",
                                u.getAvatar(), null,
                                null, "profile",
                                h.score(), null, false
                        ));
                    }
                } else {
                    // user chưa kết bạn: chỉ strict suggest, không fuzzy
                    addUnique.accept(new SuggestItem(
                            "USER", id, h.title(),
                            "SEARCH", null, null,
                            query, null,
                            h.score(), null, false
                    ));
                }
            } else if ("GROUP".equals(type)) {
                if (joinedGroupIds.contains(id)) {
                    var g = groupMap.get(id);
                    if (g != null) {
                        addUnique.accept(new SuggestItem(
                                "GROUP", id, g.getGroupName(),
                                "NAVIGATE",
                                null, g.getCoverPhoto(),
                                null, "group",
                                h.score(), null, false
                        ));
                    }
                } else {
                    // group chưa vào: chỉ strict suggest, không fuzzy
                    addUnique.accept(new SuggestItem(
                            "GROUP", id, h.title(),
                            "SEARCH", null, null,
                            query, null,
                            h.score(), null, false
                    ));
                }
            } else {
                // POST/SHARE: giữ nguyên strict suggest
                addUnique.accept(new SuggestItem(
                        type, id, h.title(),
                        "SEARCH", null, null,
                        query, null,
                        h.score(), null, false
                ));
            }

            if (items.size() >= 15) break;
        }

        return items;
    }
}
