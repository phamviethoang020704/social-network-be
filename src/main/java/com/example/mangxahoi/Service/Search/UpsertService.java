package com.example.mangxahoi.Service.Search;

import com.example.mangxahoi.Entity.RecentSearchEntity;
import com.example.mangxahoi.Enums.SearchType;
import com.example.mangxahoi.Repository.SearchRepository;
import com.example.mangxahoi.Repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@AllArgsConstructor
public class UpsertService {
    private final JdbcTemplate jdbcTemplate;
    private static final String UPSERT_SQL = """
        INSERT INTO search_index(target_type, target_id, title, content, updated_at)
        VALUES (?, ?, ?, ?, now())
        ON CONFLICT (target_type, target_id)
        DO UPDATE SET
            title = excluded.title,
            content = excluded.content,
            updated_at = now()
        """;

    private static final String DELETE_SQL = """
        DELETE FROM search_index
        WHERE target_type = ? AND target_id = ?
        """;
    private final SearchRepository searchRepository;
    private final UserRepository userRepository;

    public void upsert(SearchType type, Long targetId, String text) {
        String content = normalize(text);
        String title = excerpt(content, 120);
        jdbcTemplate.update(UPSERT_SQL, type.name(), targetId, title, content);
    }

    public void delete(SearchType type, Long targetId) {
        jdbcTemplate.update(DELETE_SQL, type.name(), targetId);
    }

    // --- Helpers ---

    private String normalize(String s) {
        if (s == null) return "";
        // gọn whitespace + trim
        return s.trim().replaceAll("\\s+", " ");
    }

    private String excerpt(String s, int maxLen) {
        if (s == null || s.isBlank()) return "";
        if (s.length() <= maxLen) return s;
        return s.substring(0, maxLen) + "...";
    }
}
