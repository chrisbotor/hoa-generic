-- 1. Insert Test Users
-- Note: In production, these passwords must be hashed (e.g., using BCrypt)
INSERT INTO hoa.users (id, full_name, email, password_hash, role)
VALUES 
    ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'Christian Admin', 'admin@hoa.com', 'hashed_password_here', 'admin'),
    ('b1eebc99-9c0b-4ef8-bb6d-6bb9bd380a22', 'John Resident', 'john@gmail.com', 'hashed_password_here', 'resident'),
    ('c2eebc99-9c0b-4ef8-bb6d-6bb9bd380a33', 'Maria Resident', 'maria@gmail.com', 'hashed_password_here', 'resident')
ON CONFLICT (email) DO NOTHING;

-- 2. Insert Houses and Link to Owners
INSERT INTO hoa.houses (lot_number, street_address, owner_id)
VALUES 
    ('Block 1 Lot 5', 'Sunflower St.', 'b1eebc99-9c0b-4ef8-bb6d-6bb9bd380a22'),
    ('Block 2 Lot 12', 'Orchid St.', 'c2eebc99-9c0b-4ef8-bb6d-6bb9bd380a33')
ON CONFLICT (lot_number) DO NOTHING;

-- 3. Create Sample Maintenance Requests
INSERT INTO hoa.maintenance_requests (requester_id, house_id, title, description, status)
VALUES 
    ('b1eebc99-9c0b-4ef8-bb6d-6bb9bd380a22', 1, 'Street Light Out', 'The light in front of my house is flickering.', 'pending'),
    ('c2eebc99-9c0b-4ef8-bb6d-6bb9bd380a33', 2, 'Water Leak', 'Main pipe near the gate has a small leak.', 'in_progress')
ON CONFLICT DO NOTHING;

-- 4. Post an Announcement
INSERT INTO hoa.announcements (author_id, title, content, is_urgent)
VALUES 
    ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'Community Meeting', 'Join us this Sunday at the clubhouse to discuss the new solar project.', true)
ON CONFLICT DO NOTHING;