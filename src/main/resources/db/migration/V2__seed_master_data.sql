-- Master reference data

INSERT INTO public.code_country_dial_code (iso2, country_name, dial_code) VALUES
('IN', 'India', '+91'),
('US', 'United States', '+1'),
('GB', 'United Kingdom', '+44'),
('AE', 'United Arab Emirates', '+971'),
('AU', 'Australia', '+61'),
('CA', 'Canada', '+1')
ON CONFLICT DO NOTHING;

INSERT INTO public.arm (arm_type) VALUES
('Right'),
('Left')
ON CONFLICT DO NOTHING;

INSERT INTO public.player_role_types (role_name, description) VALUES
('Batsman', 'Primarily a batter'),
('Bowler', 'Primarily a bowler'),
('Batting All-Rounder', 'Contributes with bat and little on ball'),
('Bowling All-Rounder', 'Contributes little with bat and more on ball'),
('Wicket-Keeper', 'Keeps wicket; may bat as specialist')
ON CONFLICT DO NOTHING;

INSERT INTO public.batting_style (style_name) VALUES
('Right-hand bat'),
('Left-hand bat')
ON CONFLICT DO NOTHING;

INSERT INTO public.bowling_style (style_name) VALUES
('fast'),
('medium'),
('China Man'),
('Finger Spinner'),
('Wrist Spinner'),
('Orthodox Spinner'),
('Un-Orthodox Spinner'),
('Slow Spinner')
ON CONFLICT DO NOTHING;

-- Preferences (linked to any existing bowling_style row)
INSERT INTO public.bowling_preference (bowling_style_id, preference_name)
SELECT bs.id, v.preference_name
FROM (VALUES ('Pace'), ('Swing'), ('Seam'), ('Spin')) v(preference_name)
CROSS JOIN LATERAL (
    SELECT id FROM public.bowling_style ORDER BY id LIMIT 1
) bs
ON CONFLICT DO NOTHING;

-- Batting positions 1..11
INSERT INTO public.batting_position (id, position, role) VALUES
(1, 'Position 1', 'Top Order'),
(2, 'Position 2', 'Top Order'),
(3, 'Position 3', 'Top Order'),
(4, 'Position 4', 'Middle Order'),
(5, 'Position 5', 'Middle Order'),
(6, 'Position 6', 'Middle Order'),
(7, 'Position 7', 'Lower Middle'),
(8, 'Position 8', 'Lower Middle'),
(9, 'Position 9', 'Tail'),
(10, 'Position 10', 'Tail'),
(11, 'Position 11', 'Tail')
ON CONFLICT DO NOTHING;

INSERT INTO public.batting_intent (intent_name, description) VALUES
('Defensive/Anchor', 'Prioritizes wicket preservation; low strike rate, high balls faced.'),
('Balanced', 'Rotates strike effectively; shifts gears based on match situation.'),
('Aggressive/Attacker', 'High boundary percentage; looks to dominate the bowler.'),
('Radical/Innovator', 'Frequent use of 360-degree shots (scoops, reverse sweeps, ramps).'),
('Finisher', 'Specializes in high-pressure, high-strike-rate scoring in death overs.')
ON CONFLICT DO NOTHING;

INSERT INTO public.bowling_tactical_role (role_name, description) VALUES
('Swing Specialist', 'Relies on movement through the air (New Ball)'),
('Hit-the-Deck', 'Relies on extra bounce and pace off the pitch'),
('Death Over Specialist', 'Expert in yorkers and slower-ball variations'),
('Containment/Economical', 'Focuses on bowling dot balls and drying up runs'),
('Wicket-Taker', 'Attacking lines, higher risk of leaking runs but high strike rate')
ON CONFLICT DO NOTHING;

-- Initialize migration state row (will be updated by the app after Flyway finishes).
INSERT INTO public.app_migration_state (id, last_applied_version, updated_at)
VALUES (1, NULL, NOW())
ON CONFLICT (id) DO NOTHING;
