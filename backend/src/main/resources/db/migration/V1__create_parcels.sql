CREATE TABLE parcel (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL,
    grape_variety VARCHAR(100) NOT NULL,
    area_sq_m INTEGER NOT NULL
);
