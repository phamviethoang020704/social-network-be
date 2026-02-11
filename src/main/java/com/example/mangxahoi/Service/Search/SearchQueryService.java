package com.example.mangxahoi.Service.Search;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SearchQueryService {
    private final JdbcTemplate searchJdbc;
    public SearchQueryService(@Qualifier("searchJdbcTemplate") JdbcTemplate searchJdbc) {
        this.searchJdbc = searchJdbc;
    }
    //gợi ý nâng cao cho user đã kết bạn, group đã vào
    public List<SearchHit> suggestFuzzyTokensForIds(String targetType, List<Long> ids, String q, int limit) {
        if (ids == null || ids.isEmpty()) return List.of();

        // build IN (?, ?, ?, ...)
        String in = ids.stream().map(x -> "?").collect(java.util.stream.Collectors.joining(","));

        String sql = """
        WITH inp AS (
          SELECT vn_norm(?) AS q
        )
        SELECT target_type, target_id, title,
               (
                 CASE
                   WHEN title_norm = (SELECT q FROM inp) THEN 400
                   WHEN title_norm LIKE (SELECT q FROM inp) || '%%' THEN 300
                   WHEN title_norm LIKE '%%' || (SELECT q FROM inp) || '%%' THEN 200
                   ELSE 150
                 END
                 + similarity(title_norm, (SELECT q FROM inp)) * 50
                 + similarity(content_norm, (SELECT q FROM inp)) * 20
               ) AS score
        FROM search_index
        WHERE target_type = ?
          AND target_id IN (%s)
          AND NOT EXISTS (
                SELECT 1
                FROM unnest(string_to_array((SELECT q FROM inp), ' ')) tok
                WHERE tok <> ''
                  AND title_norm NOT LIKE '%%' || tok || '%%'
                  AND content_norm NOT LIKE '%%' || tok || '%%'
          )
        ORDER BY score DESC, updated_at DESC
        LIMIT ?
    """.formatted(in);

        List<Object> params = new ArrayList<>();
        params.add(q);
        params.add(targetType);

        params.addAll(ids);
        params.add(limit);

        return searchJdbc.query(sql, (rs, i) -> new SearchHit(
                rs.getString("target_type"),
                rs.getLong("target_id"),
                rs.getString("title"),
                rs.getDouble("score")
        ), params.toArray());
    }
    // Gợi ý nhanh: ưu tiên title, nhưng vẫn match content
    public List<SearchHit> suggest(String q, int limit) {
        String sql = """
            WITH inp AS (SELECT vn_norm(?) AS q)
            SELECT target_type, target_id, title,
                   (
                     CASE
                       WHEN title_norm = (SELECT q FROM inp) THEN 400
                       WHEN title_norm LIKE (SELECT q FROM inp) || '%' THEN 300
                       WHEN title_norm LIKE '%' || (SELECT q FROM inp) || '%' THEN 200
                       WHEN content_norm LIKE '%' || (SELECT q FROM inp) || '%' THEN 120
                       ELSE 0
                     END
                     + similarity(title_norm, (SELECT q FROM inp)) * 50
                     + similarity(content_norm, (SELECT q FROM inp)) * 20
                   ) AS score
            FROM search_index
            WHERE title_norm LIKE '%' || (SELECT q FROM inp) || '%'
               OR content_norm LIKE '%' || (SELECT q FROM inp) || '%'
            ORDER BY score DESC, updated_at DESC
            LIMIT ?
        """;

        return searchJdbc.query(sql, (rs, i) -> new SearchHit(
                rs.getString("target_type"),
                rs.getLong("target_id"),
                rs.getString("title"),
                rs.getDouble("score")
        ), q, limit);
    }

    // Search full list: dùng cả full-text + trigram/contains
    public List<SearchHit> search(String q, String typeOrNull, int limit, int offset) {
        String sql = """
                WITH inp AS (
                  SELECT vn_norm(?) AS q,
                         websearch_to_tsquery('simple', vn_norm(?)) AS tsq
                )
                SELECT target_type, target_id, title,
                       (
                         ts_rank_cd(search_vec, (SELECT tsq FROM inp)) * 200
                         +
                         CASE
                           WHEN title_norm = (SELECT q FROM inp) THEN 400
                           WHEN title_norm LIKE (SELECT q FROM inp) || '%' THEN 300
                           WHEN title_norm LIKE '%' || (SELECT q FROM inp) || '%' THEN 200
                           WHEN content_norm LIKE '%' || (SELECT q FROM inp) || '%' THEN 120
                           ELSE 0
                         END
                         + similarity(title_norm, (SELECT q FROM inp)) * 50
                         + similarity(content_norm, (SELECT q FROM inp)) * 20
                       ) AS score
                FROM search_index
                WHERE (
                    search_vec @@ (SELECT tsq FROM inp)
                    OR title_norm LIKE '%' || (SELECT q FROM inp) || '%'
                    OR content_norm LIKE '%' || (SELECT q FROM inp) || '%'
                )
                __TYPE_CLAUSE__
                ORDER BY score DESC, updated_at DESC
                LIMIT ? OFFSET ?
            """;

        String typeClause = "";
        List<Object> params = new ArrayList<>();
        params.add(q);
        params.add(q);

        if (typeOrNull != null && !typeOrNull.equalsIgnoreCase("ALL")) {
            typeClause = "AND target_type = ?";
            params.add(typeOrNull.toUpperCase());
        }

        String finalSql = sql.replace("__TYPE_CLAUSE__", typeClause);

        params.add(limit);
        params.add(offset);

        return searchJdbc.query(finalSql, (rs, i) -> new SearchHit(
                rs.getString("target_type"),
                rs.getLong("target_id"),
                rs.getString("title"),
                rs.getDouble("score")
        ), params.toArray());
    }

    public Map<String, Long> countByType(String q) {
        String sql = """
          WITH inp AS (
            SELECT vn_norm(?) AS q,
                   websearch_to_tsquery('simple', vn_norm(?)) AS tsq
          )
          SELECT target_type, count(*) AS cnt
          FROM search_index
          WHERE (
              search_vec @@ (SELECT tsq FROM inp)
              OR title_norm LIKE '%' || (SELECT q FROM inp) || '%'
              OR content_norm LIKE '%' || (SELECT q FROM inp) || '%'
          )
          GROUP BY target_type
        """;

        var map = new HashMap<String, Long>();
        searchJdbc.query(sql, rs -> {
            map.put(rs.getString("target_type"), rs.getLong("cnt"));
        }, q, q);
        return map;
    }

    public record SearchHit(String type, Long id, String title, double score) {}
}
