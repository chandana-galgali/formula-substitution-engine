-- Illustrative sample data. These are common, publicly known aroma materials;
-- the accords and rules are invented for this demo and are NOT real company formulas.
-- Keep this file in sync with the DATA block in docs/index.html.
-- The BANNED / RESTRICTED entries below reflect real EU rules, but the max_percent
-- value for oakmoss is a simplified, illustrative number for the demo.

INSERT INTO ingredient (id, name, category, is_accord) VALUES
('LIN', 'Linalool',               'Floral',   FALSE),
('LNA', 'Linalyl Acetate',        'Floral',   FALSE),
('LIM', 'Limonene',               'Citrus',   FALSE),
('CIT', 'Citral',                 'Citrus',   FALSE),
('BGM', 'Bergamot Oil',           'Citrus',   FALSE),
('VAN', 'Vanillin',               'Gourmand', FALSE),
('EVN', 'Ethyl Vanillin',         'Gourmand', FALSE),
('COU', 'Coumarin',               'Fougere',  FALSE),
('HED', 'Hedione',                'Floral',   FALSE),
('ISO', 'Iso E Super',            'Woody',    FALSE),
('AMB', 'Ambroxan',               'Amber',    FALSE),
('GAL', 'Galaxolide',             'Musk',     FALSE),
('CED', 'Cedarwood Oil',          'Woody',    FALSE),
('PEA', 'Phenylethyl Alcohol',    'Floral',   FALSE),
('GER', 'Geraniol',               'Floral',   FALSE),
('CTR', 'Citronellol',            'Floral',   FALSE),
('EUG', 'Eugenol',                'Spicy',    FALSE),
('BEN', 'Benzyl Benzoate',        'Solvent',  FALSE),
('DPG', 'Dipropylene Glycol',     'Solvent',  FALSE),
('EVR', 'Evernyl',                'Mossy',    FALSE),
('EBR', 'Ethylene Brassylate',    'Musk',     FALSE),
('ACC-LAV', 'Lavender Accord LV-1',    'Accord', TRUE),
('ACC-CIT', 'Citrus Top Accord CT-2',  'Accord', TRUE),
('ACC-ROS', 'Rose Accord RS-3',        'Accord', TRUE),
('ACC-VAN', 'Vanilla Base VB-4',       'Accord', TRUE),
('ACC-WOD', 'Woody Amber Base WA-5',   'Accord', TRUE),
('ACC-FOU', 'Fougere Base FG-6',       'Accord', TRUE),
('ACC-MSK', 'Clean Musk Base CM-7',    'Accord', TRUE),
('ACC-FRH', 'Fresh Floral Accord FF-8','Accord', TRUE);

INSERT INTO substitution_rule (ingredient_a, ingredient_b, result_ingredient, reason, note) VALUES
('LIN', 'LNA', 'ACC-LAV', 'CONSOLIDATION', 'Pre-blended lavender accord: one weigh-in instead of two'),
('LIM', 'CIT', 'ACC-CIT', 'SUPPLY',        'Use stocked citrus accord when citral supply is short'),
('PEA', 'GER', 'ACC-ROS', 'CONSOLIDATION', 'Rose accord already balances PEA and geraniol'),
('CTR', 'GER', 'ACC-ROS', 'CONSOLIDATION', 'Citronellol + geraniol covered by the rose accord'),
('VAN', 'EVN', 'ACC-VAN', 'COST',          'Vanilla base is cheaper than buying both vanillins'),
('ISO', 'AMB', 'ACC-WOD', 'COST',          'Woody amber base replaces two premium materials'),
('ACC-LAV', 'COU', 'ACC-FOU', 'CONSOLIDATION', 'Chained rule: lavender accord + coumarin = fougere base'),
('GAL', 'BEN', 'ACC-MSK', 'CONSOLIDATION', 'Musk base is supplied pre-diluted'),
('HED', 'ACC-ROS', 'ACC-FRH', 'CONSOLIDATION', 'Chained rule: hedione + rose accord = fresh floral accord');

-- Regulated materials (feature 2). Real regulations, simplified for the demo.
INSERT INTO ingredient (id, name, category, is_accord, status, max_percent, regulatory_note) VALUES
('LIL', 'Lilial (BMHCA)',     'Floral', FALSE, 'BANNED',     0,   'Banned in EU cosmetics from 1 Mar 2022 (Reg. EU 2021/1902)'),
('LYR', 'Lyral (HICC)',       'Floral', FALSE, 'BANNED',     0,   'Banned in EU cosmetics from 23 Aug 2021 (Reg. EU 2017/1410)'),
('OAK', 'Oakmoss Absolute',   'Mossy',  FALSE, 'RESTRICTED', 0.1, 'Atranol/chloroatranol banned in EU; low-atranol oakmoss limited (demo limit 0.1%)');

-- Feature 2: C -> A + B (+ ...). Option 1 is preferred; later options are fallbacks.
INSERT INTO replacement_recipe (id, target, priority, note) VALUES
(1,  'LIL', 1, 'Rebuild the lily-of-the-valley effect from Hedione + Citronellol'),
(2,  'LIL', 2, 'Fallback muguet blend: Phenylethyl Alcohol + Linalool'),
(3,  'LYR', 1, 'Lilial-based swap: skipped automatically, because Lilial is banned too'),
(4,  'LYR', 2, 'Three-part muguet blend'),
(5,  'OAK', 1, 'Evernyl carries the mossy facet, cedarwood adds the dry woody back'),
(6,  'GAL', 1, 'Macrocyclic musk instead of a polycyclic musk'),
(7,  'AMB', 1, 'Woody-amber effect without Ambroxan (cost / supply)'),
(8,  'ISO', 1, 'Dry woody effect without Iso E Super'),
(9,  'EVN', 1, 'Plain vanillin (a real reformulation would also adjust the dose: ethyl vanillin is stronger)'),
(10, 'CIT', 1, 'Citrus freshness from limonene + bergamot instead of citral'),
(11, 'GER', 1, 'Rose alcohols: citronellol + phenylethyl alcohol');

INSERT INTO replacement_part (recipe_id, position, ingredient_id, fraction) VALUES
(1, 1, 'HED', 0.6), (1, 2, 'CTR', 0.4),
(2, 1, 'PEA', 0.5), (2, 2, 'LIN', 0.5),
(3, 1, 'LIL', 1.0),
(4, 1, 'HED', 0.5), (4, 2, 'PEA', 0.3), (4, 3, 'CTR', 0.2),
(5, 1, 'EVR', 0.6), (5, 2, 'CED', 0.4),
(6, 1, 'EBR', 1.0),
(7, 1, 'ISO', 0.7), (7, 2, 'CED', 0.3),
(8, 1, 'CED', 0.6), (8, 2, 'AMB', 0.4),
(9, 1, 'VAN', 1.0),
(10, 1, 'LIM', 0.5), (10, 2, 'BGM', 0.5),
(11, 1, 'CTR', 0.6), (11, 2, 'PEA', 0.4);
