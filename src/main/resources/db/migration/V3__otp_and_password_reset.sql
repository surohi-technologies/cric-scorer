-- OTP + password reset + password history
-- Note: Keep idempotent where possible to work even if Hibernate created tables earlier.

-- 1) User flags for verification flow
ALTER TABLE IF EXISTS public.user_detail
    ADD COLUMN IF NOT EXISTS is_verified boolean NOT NULL DEFAULT true;

ALTER TABLE IF EXISTS public.user_detail
    ADD COLUMN IF NOT EXISTS verified_email boolean NOT NULL DEFAULT true;

ALTER TABLE IF EXISTS public.user_detail
    ADD COLUMN IF NOT EXISTS verified_phone boolean NOT NULL DEFAULT true;

ALTER TABLE IF EXISTS public.user_detail
    ADD COLUMN IF NOT EXISTS verification_required_channels varchar(20);

-- Existing users should not get blocked by the new verification gate.
-- For existing rows, mark verified based on which contact data exists.
UPDATE public.user_detail
SET
    is_verified = true,
    verified_email = (email_id IS NOT NULL AND btrim(email_id) <> ''),
    verified_phone = (phone_number IS NOT NULL AND btrim(phone_number) <> '')
WHERE is_verified IS DISTINCT FROM true;

-- 2) OTP table
CREATE TABLE IF NOT EXISTS public.user_otp (
    id bigserial PRIMARY KEY,
    user_id bigint NOT NULL,
    channel varchar(10) NOT NULL,            -- EMAIL | PHONE
    destination varchar(255) NOT NULL,       -- email or +<code><number>
    otp_hash varchar(64) NOT NULL,           -- sha256 hex
    purpose varchar(30) NOT NULL,            -- REGISTRATION_VERIFY | PASSWORD_RESET
    attempts integer NOT NULL DEFAULT 0,
    max_attempts integer NOT NULL DEFAULT 5,
    created_at timestamptz NOT NULL DEFAULT now(),
    expires_at timestamptz NOT NULL,
    consumed_at timestamptz NULL,

    CONSTRAINT fk_user_otp_user
        FOREIGN KEY (user_id) REFERENCES public.user_detail(id) ON DELETE CASCADE,

    CONSTRAINT ck_user_otp_channel
        CHECK (channel IN ('EMAIL','PHONE')),

    CONSTRAINT ck_user_otp_purpose
        CHECK (purpose IN ('REGISTRATION_VERIFY','PASSWORD_RESET')),

    CONSTRAINT ck_user_otp_attempts_nonneg
        CHECK (attempts >= 0 AND max_attempts > 0 AND attempts <= max_attempts)
);

CREATE INDEX IF NOT EXISTS ix_user_otp_user_purpose_active
    ON public.user_otp (user_id, purpose, channel, consumed_at);

CREATE INDEX IF NOT EXISTS ix_user_otp_expires
    ON public.user_otp (expires_at);

-- 3) Password history (keep last 3 in code)
CREATE TABLE IF NOT EXISTS public.user_password_history (
    id bigserial PRIMARY KEY,
    user_id bigint NOT NULL,
    password_hash varchar(255) NOT NULL,     -- bcrypt
    created_at timestamptz NOT NULL DEFAULT now(),

    CONSTRAINT fk_pwd_hist_user
        FOREIGN KEY (user_id) REFERENCES public.user_detail(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS ix_pwd_hist_user_created
    ON public.user_password_history (user_id, created_at DESC);

-- Seed initial history for existing users (one entry each) if not present.
INSERT INTO public.user_password_history (user_id, password_hash, created_at)
SELECT ud.id, ud.password, now()
FROM public.user_detail ud
WHERE NOT EXISTS (
    SELECT 1 FROM public.user_password_history h WHERE h.user_id = ud.id
);

-- 4) Password reset token (issued after OTP verify)
CREATE TABLE IF NOT EXISTS public.password_reset_token (
    id bigserial PRIMARY KEY,
    user_id bigint NOT NULL,
    token_hash varchar(64) NOT NULL,         -- sha256 hex
    created_at timestamptz NOT NULL DEFAULT now(),
    expires_at timestamptz NOT NULL,
    consumed_at timestamptz NULL,

    CONSTRAINT fk_reset_token_user
        FOREIGN KEY (user_id) REFERENCES public.user_detail(id) ON DELETE CASCADE
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_reset_token_hash
    ON public.password_reset_token (token_hash);

CREATE INDEX IF NOT EXISTS ix_reset_token_user_active
    ON public.password_reset_token (user_id, consumed_at);

CREATE INDEX IF NOT EXISTS ix_reset_token_expires
    ON public.password_reset_token (expires_at);

