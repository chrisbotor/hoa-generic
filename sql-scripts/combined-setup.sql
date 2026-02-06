-- 1. CLEANUP (Optional: Uncomment the next line if you want to start totally fresh)
DROP SCHEMA IF EXISTS hoa CASCADE;

-- 2. EXTENSIONS & SCHEMA
CREATE EXTENSION IF NOT EXISTS "pgcrypto";
CREATE SCHEMA IF NOT EXISTS hoa;

-- 3. TABLES (Idempotent: will not overwrite if already exists)
CREATE TABLE IF NOT EXISTS hoa.users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    full_name TEXT NOT NULL,
    email TEXT UNIQUE NOT NULL,
    password_hash TEXT NOT NULL,
    role TEXT NOT NULL DEFAULT 'resident' CHECK (role IN ('admin', 'resident')),
    created_at TIMESTAMPTZ DEFAULT now()
);

CREATE TABLE IF NOT EXISTS hoa.houses (
    id SERIAL PRIMARY KEY,
    lot_number TEXT UNIQUE NOT NULL,
    street_address TEXT NOT NULL,
    owner_id UUID REFERENCES hoa.users(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS hoa.maintenance_requests (
    id SERIAL PRIMARY KEY,
    requester_id UUID NOT NULL REFERENCES hoa.users(id),
    house_id INT NOT NULL REFERENCES hoa.houses(id),
    title TEXT NOT NULL,
    description TEXT,
    status TEXT DEFAULT 'pending' CHECK (status IN ('pending', 'in_progress', 'completed', 'cancelled')),
    created_at TIMESTAMPTZ DEFAULT now()
);

-- 4. SEED DATA (Test Users & Relationships)
-- Using 'password123' as a placeholder for these test accounts
INSERT INTO hoa.users (id, full_name, email, password_hash, role)
VALUES 
    ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'Christian Admin', 'admin@hoa.com', 'password123', 'admin'),
    ('b1eebc99-9c0b-4ef8-bb6d-6bb9bd380a22', 'John Resident', 'john@gmail.com', 'password123', 'resident')
ON CONFLICT (email) DO NOTHING;

INSERT INTO hoa.houses (lot_number, street_address, owner_id)
VALUES 
    ('B1-L5', 'Sunflower St.', 'b1eebc99-9c0b-4ef8-bb6d-6bb9bd380a22')
ON CONFLICT (lot_number) DO NOTHING;