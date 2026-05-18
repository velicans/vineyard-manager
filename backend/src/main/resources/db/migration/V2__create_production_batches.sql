CREATE TYPE batch_status AS ENUM ('HARVESTED', 'PRESSED', 'BOTTLED');

CREATE TABLE production_batch (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    parcel_id UUID NOT NULL REFERENCES parcel(id),
    year INTEGER NOT NULL,
    status batch_status NOT NULL
);
