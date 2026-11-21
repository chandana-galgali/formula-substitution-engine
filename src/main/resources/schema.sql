-- Runs automatically when the app starts (see application.properties).

CREATE TABLE ingredient (
    id              VARCHAR(16)  PRIMARY KEY,
    name            VARCHAR(100) NOT NULL,
    category        VARCHAR(40)  NOT NULL,
    is_accord       BOOLEAN      NOT NULL DEFAULT FALSE,
    status          VARCHAR(12)  NOT NULL DEFAULT 'ACTIVE',   -- ACTIVE, RESTRICTED, BANNED
    max_percent     DECIMAL(7,4),                             -- NULL = no limit, 0 = not allowed
    regulatory_note VARCHAR(255),
    CONSTRAINT chk_status CHECK (status IN ('ACTIVE', 'RESTRICTED', 'BANNED'))
);

-- Feature 1: consolidation rules, A + B -> C
CREATE TABLE substitution_rule (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    ingredient_a      VARCHAR(16)  NOT NULL REFERENCES ingredient(id),
    ingredient_b      VARCHAR(16)  NOT NULL REFERENCES ingredient(id),
    result_ingredient VARCHAR(16)  NOT NULL REFERENCES ingredient(id),
    reason            VARCHAR(20)  NOT NULL,
    note              VARCHAR(255),
    CONSTRAINT chk_distinct_pair CHECK (ingredient_a <> ingredient_b)
);

CREATE INDEX idx_rule_pair ON substitution_rule(ingredient_a, ingredient_b);

-- Feature 2: replacement recipes, C -> A + B (+ ...)
-- One recipe = one way to replace `target`. Several recipes per target are tried in priority order.
CREATE TABLE replacement_recipe (
    id       BIGINT       PRIMARY KEY,
    target   VARCHAR(16)  NOT NULL REFERENCES ingredient(id),
    priority INT          NOT NULL,
    note     VARCHAR(255),
    CONSTRAINT uq_target_priority UNIQUE (target, priority)
);

-- The ingredients that make up a recipe and each one's share of the replaced amount.
CREATE TABLE replacement_part (
    recipe_id     BIGINT       NOT NULL REFERENCES replacement_recipe(id),
    position      INT          NOT NULL,
    ingredient_id VARCHAR(16)  NOT NULL REFERENCES ingredient(id),
    fraction      DECIMAL(5,4) NOT NULL,
    PRIMARY KEY (recipe_id, position),
    CONSTRAINT chk_fraction CHECK (fraction > 0 AND fraction <= 1)
);
