-- Master reference tables
-- Note: These are intentionally idempotent (`IF NOT EXISTS`) so they work even if Hibernate created tables earlier.

CREATE TABLE IF NOT EXISTS public.code_country_dial_code (
    id BIGSERIAL PRIMARY KEY,
    iso2 VARCHAR(2) NOT NULL,
    country_name VARCHAR(80) NOT NULL,
    dial_code VARCHAR(8) NOT NULL,
    CONSTRAINT uk_country_dial_code_iso2 UNIQUE (iso2),
    CONSTRAINT uk_country_dial_code_dial_code UNIQUE (dial_code)
);

CREATE TABLE IF NOT EXISTS public.arm (
    id SERIAL PRIMARY KEY,
    arm_type VARCHAR(50) NOT NULL,
    CONSTRAINT uk_arm_type UNIQUE (arm_type)
);

CREATE TABLE IF NOT EXISTS public.batting_style (
    id SERIAL PRIMARY KEY,
    style_name VARCHAR(80) NOT NULL,
    CONSTRAINT uk_batting_style_name UNIQUE (style_name)
);

CREATE TABLE IF NOT EXISTS public.bowling_style (
    id SERIAL PRIMARY KEY,
    style_name VARCHAR(80) NOT NULL,
    CONSTRAINT uk_bowling_style_name UNIQUE (style_name)
);

CREATE TABLE IF NOT EXISTS public.bowling_preference (
    id SERIAL PRIMARY KEY,
    bowling_style_id INTEGER,
    preference_name VARCHAR(80) NOT NULL,
    CONSTRAINT fk_bowling_preference_style
        FOREIGN KEY (bowling_style_id) REFERENCES public.bowling_style(id)
);

CREATE TABLE IF NOT EXISTS public.batting_position (
    id INTEGER PRIMARY KEY,
    position VARCHAR(50),
    role VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS public.player_role_types (
    id SERIAL PRIMARY KEY,
    role_name VARCHAR(80) NOT NULL,
    description VARCHAR(200),
    CONSTRAINT uk_player_role_name UNIQUE (role_name)
);

CREATE TABLE IF NOT EXISTS public.batting_intent (
    id SERIAL PRIMARY KEY,
    intent_name VARCHAR(50) NOT NULL,
    CONSTRAINT uk_batting_intent_name UNIQUE (intent_name),
    description TEXT
    );

CREATE TABLE IF NOT EXISTS public.bowling_tactical_role (
   id SERIAL PRIMARY KEY,
   role_name VARCHAR(50) NOT NULL,
   description TEXT,
   CONSTRAINT uk_bowling_tactical_role_name UNIQUE (role_name)
    );

CREATE TABLE IF NOT EXISTS public.app_migration_state (
    id INTEGER PRIMARY KEY,
    last_applied_version VARCHAR(20),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);
