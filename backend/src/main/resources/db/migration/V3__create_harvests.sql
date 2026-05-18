CREATE TABLE harvest (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    batch_id UUID NOT NULL UNIQUE REFERENCES production_batch(id),
    date DATE NOT NULL,
    quantity_kg NUMERIC(10,2) NOT NULL
);
