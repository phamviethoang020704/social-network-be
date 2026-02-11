package com.example.mangxahoi.Service.Search;

import com.example.mangxahoi.Entity.RecentSearchEntity;
import com.example.mangxahoi.Repository.SearchRepository;
import com.example.mangxahoi.Repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@AllArgsConstructor
public class RecentSearchService {

    private final SearchRepository searchRepository;
    private final UserRepository userRepository;


    public List<RecentSearchEntity> top8(Long userId) {
        return searchRepository.findTop8ByUserEntity_IdOrderByLastUsedAtDesc(userId);
    }

    public List<RecentSearchEntity> suggest(Long userId, String q, int limit) {
        return searchRepository.suggestRecent(userId, q, PageRequest.of(0, limit));
    }

    @Transactional
    public void record(Long userId, String keyword) {
        if (keyword == null) return;
        String kw = keyword.trim();
        if (kw.isEmpty()) return;

        var existing = searchRepository.findByUserEntity_IdAndKeyword(userId, kw);
        if (existing.isPresent()) {
            var r = existing.get();
            r.setLastUsedAt(LocalDateTime.now());
            searchRepository.save(r);
            return;
        }

        if (searchRepository.countByUserEntity_Id(userId) >= 10) {
            searchRepository.findFirstByUserEntity_IdOrderByLastUsedAtAsc(userId)
                    .ifPresent(searchRepository::delete);
        }

        var userRef = userRepository.getReferenceById(userId);
        var r = new RecentSearchEntity();
        r.setUserEntity(userRef);
        r.setKeyword(kw);
        r.setLastUsedAt(LocalDateTime.now());
        searchRepository.save(r);
    }

    @Transactional
    public void deleteOne(Long userId, Long id) {
        searchRepository.deleteByIdAndUserEntity_Id(id, userId);
    }
}
