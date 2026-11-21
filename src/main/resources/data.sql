-- Illustrative sample data. These are common, publicly known aroma materials;
-- the accords and rules are invented for this demo and are NOT real company formulas.
-- Keep this file in sync with the DATA block in docs/index.html.

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
