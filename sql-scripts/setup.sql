-- 1. Ensure the UUID extension is available for our gen_random_uuid() calls
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- 2. Create the dedicated schema
CREATE SCHEMA IF NOT EXISTS hoa;

-- 3. Users Table (Core Identity)
CREATE TABLE IF NOT EXISTS hoa.users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    full_name TEXT NOT NULL,
    email TEXT UNIQUE NOT NULL,
    password_hash TEXT NOT NULL,
    role TEXT NOT NULL DEFAULT 'resident' CHECK (role IN ('admin', 'resident')),
    created_at TIMESTAMPTZ DEFAULT now()
);

-- 4. Houses Table (Property Mapping)
CREATE TABLE IF NOT EXISTS hoa.houses (
    id SERIAL PRIMARY KEY,
    lot_number TEXT UNIQUE NOT NULL,
    street_address TEXT NOT NULL,
    owner_id UUID REFERENCES hoa.users(id) ON DELETE SET NULL
);

-- 5. Maintenance Requests (Resident-Board Interaction)
CREATE TABLE IF NOT EXISTS hoa.maintenance_requests (
    id SERIAL PRIMARY KEY,
    requester_id UUID NOT NULL REFERENCES hoa.users(id),
    house_id INT NOT NULL REFERENCES hoa.houses(id),
    title TEXT NOT NULL,
    description TEXT,
    status TEXT DEFAULT 'pending' CHECK (status IN ('pending', 'in_progress', 'completed', 'cancelled')),
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now()
);

-- 6. Announcements (Bulletin Board)
CREATE TABLE IF NOT EXISTS hoa.announcements (
    id SERIAL PRIMARY KEY,
    author_id UUID REFERENCES hoa.users(id),
    title TEXT NOT NULL,
    content TEXT NOT NULL,
    is_urgent BOOLEAN DEFAULT false,
    created_at TIMESTAMPTZ DEFAULT now()
);

-- 7. Audit Logs (Optional but recommended for MongoDB bridge later)
-- This could also be a Postgres table if you want strict tracking
CREATE TABLE IF NOT EXISTS hoa.activity_logs (
    id SERIAL PRIMARY KEY,
    user_id UUID REFERENCES hoa.users(id),
    action TEXT NOT NULL,
    metadata JSONB, -- For flexible data storage
    created_at TIMESTAMPTZ DEFAULT now()
);