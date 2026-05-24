CREATE EXTENSION IF NOT EXISTS unaccent;
CREATE EXTENSION IF NOT EXISTS pg_trgm;

CREATE OR REPLACE FUNCTION vn_norm(input text)
RETURNS text
LANGUAGE sql
IMMUTABLE
AS $$
SELECT trim(
               regexp_replace(
                       lower(unaccent(coalesce(input, ''))),
                       '\s+',
                       ' ',
                       'g'
               )
       );
$$;

CREATE TABLE IF NOT EXISTS search_index (
                                            id BIGSERIAL PRIMARY KEY,

                                            target_type VARCHAR(50) NOT NULL,
    target_id BIGINT NOT NULL,

    title TEXT,
    content TEXT,

    title_norm TEXT,
    content_norm TEXT,

    search_vec tsvector,

    updated_at TIMESTAMP DEFAULT NOW(),

    CONSTRAINT uk_search_target UNIQUE(target_type, target_id)
    );

CREATE OR REPLACE FUNCTION search_index_update_fields()
RETURNS trigger
LANGUAGE plpgsql
AS $$
BEGIN
    NEW.title_norm := vn_norm(NEW.title);
    NEW.content_norm := vn_norm(NEW.content);

    NEW.search_vec :=
        setweight(to_tsvector('simple', coalesce(NEW.title_norm, '')), 'A') ||
        setweight(to_tsvector('simple', coalesce(NEW.content_norm, '')), 'B');

RETURN NEW;
END;
$$;

DROP TRIGGER IF EXISTS trg_search_index_update_fields ON search_index;

CREATE TRIGGER trg_search_index_update_fields
    BEFORE INSERT OR UPDATE ON search_index
                         FOR EACH ROW
                         EXECUTE FUNCTION search_index_update_fields();

CREATE INDEX IF NOT EXISTS idx_search_vec
    ON search_index USING GIN(search_vec);

CREATE INDEX IF NOT EXISTS idx_title_norm_trgm
    ON search_index USING GIN(title_norm gin_trgm_ops);

CREATE INDEX IF NOT EXISTS idx_content_norm_trgm
    ON search_index USING GIN(content_norm gin_trgm_ops);