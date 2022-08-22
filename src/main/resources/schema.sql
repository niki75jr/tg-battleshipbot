DROP TABLE IF EXISTS user_games;
DROP TABLE IF EXISTS users;

CREATE TABLE IF NOT EXISTS users
(
    id               BIGSERIAL PRIMARY KEY,
    telegram_user_id BIGINT     NOT NULL,
    first_name       VARCHAR(255),
    last_name        VARCHAR(255),
    lang_code        VARCHAR(2) NOT NULL,
    chat_id          BIGINT     NOT NULL,
    created_at     TIMESTAMPTZ NOT NULL,
    CONSTRAINT users_fist_last_name_check
        CHECK ( (first_name IS NOT NULL AND last_name IS NOT NULL)
            OR (first_name IS NULL AND last_name IS NOT NULL)
            OR (first_name IS NOT NULL AND last_name IS NULL))
);

CREATE TABLE IF NOT EXISTS user_games
(
    id             BIGSERIAL PRIMARY KEY,
    user_record_id BIGINT      NOT NULL,
    opponent_id    BIGINT      NOT NULL,
    scores_game    SMALLINT    NOT NULL,
    total_shots    SMALLINT    NOT NULL,
    is_win         BOOLEAN     NOT NULL,
    created_at     TIMESTAMPTZ NOT NULL,
    FOREIGN KEY (user_record_id) REFERENCES users (id) ON UPDATE CASCADE ON DELETE CASCADE
);