CREATE TABLE pressing (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    batch_id UUID NOT NULL UNIQUE REFERENCES production_batch(id),
    date DATE NOT NULL,
    must_liters NUMERIC(10,2) NOT NULL,
    yield_ratio NUMERIC(6,4) NOT NULL
);
