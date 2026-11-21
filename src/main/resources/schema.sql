-- Runs automatically when the app starts (see application.properties).

CREATE TABLE ingredient (
    id        VARCHAR(16)  PRIMARY KEY,
    name      VARCHAR(100) NOT NULL,
    category  VARCHAR(40)  NOT NULL,
    is_accord BOOLEAN      NOT NULL DEFAULT FALSE
);

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
