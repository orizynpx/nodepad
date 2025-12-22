CREATE TABLE IF NOT EXISTS recent_files
(
    file_path
    TEXT
    PRIMARY
    KEY,
    last_opened
    INTEGER,
    is_pinned
    INTEGER
    DEFAULT
    0
);

CREATE TABLE IF NOT EXISTS api_cache
(
    isbn
    TEXT
    PRIMARY
    KEY,
    title
    TEXT,
    description
    TEXT,
    image_url
    TEXT,
    fetched_at
    INTEGER
);