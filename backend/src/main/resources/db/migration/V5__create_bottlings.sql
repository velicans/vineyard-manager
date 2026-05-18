CREATE TYPE bottle_volume AS ENUM ('L075', 'L150');

CREATE TABLE bottling (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    batch_id UUID NOT NULL UNIQUE REFERENCES production_batch(id),
    date DATE NOT NULL,
    bottle_count INTEGER NOT NULL,
    bottle_volume bottle_volume NOT NULL
);
