CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE tokens (
    id serial not null PRIMARY KEY,
    userid serial not null,
    token varchar(255) not null default encode(gen_random_bytes(32), 'hex'),
    created_at timestamp default CURRENT_TIMESTAMP,
    expires_at timestamp default CURRENT_TIMESTAMP + interval '5 minute'
)