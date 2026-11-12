-- ============================================================================
-- LRock.dev - Supabase PostgreSQL Schema for Contact Form Inquiries
-- ============================================================================
-- Paste this entire script directly into your Supabase SQL Editor.

-- 1. Create the contact_messages table
CREATE TABLE IF NOT EXISTS contact_messages (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name TEXT NOT NULL,
    phone_whatsapp TEXT NOT NULL,
    message TEXT NOT NULL,
    status TEXT DEFAULT 'new' CHECK (status IN ('new', 'contacted', 'closed')),
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now()
);

-- 2. Enable Row Level Security (RLS)
ALTER TABLE contact_messages ENABLE ROW LEVEL SECURITY;

-- 3. RLS Policies
-- 3a. Allow anonymous visitors (public website / mobile app) to submit contact inquiries
CREATE POLICY "Allow public insert on contact_messages" 
    ON contact_messages 
    FOR INSERT 
    TO anon, authenticated 
    WITH CHECK (true);

-- 3b. Allow only authenticated users (administrators / dashboard users) to view messages
CREATE POLICY "Allow authenticated users to select contact_messages" 
    ON contact_messages 
    FOR SELECT 
    TO authenticated 
    USING (true);

-- 3c. Allow only authenticated users to update message status (e.g., mark as 'contacted' or 'closed')
CREATE POLICY "Allow authenticated users to update contact_messages" 
    ON contact_messages 
    FOR UPDATE 
    TO authenticated 
    USING (true) 
    WITH CHECK (true);

-- 3d. Allow only authenticated users to delete messages if necessary
CREATE POLICY "Allow authenticated users to delete contact_messages" 
    ON contact_messages 
    FOR DELETE 
    TO authenticated 
    USING (true);

-- 4. Automatic updated_at timestamp trigger function
CREATE OR REPLACE FUNCTION update_modified_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = now();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- 5. Attach trigger to contact_messages table
DROP TRIGGER IF EXISTS tr_contact_messages_updated_at ON contact_messages;
CREATE TRIGGER tr_contact_messages_updated_at
    BEFORE UPDATE ON contact_messages
    FOR EACH ROW
    EXECUTE FUNCTION update_modified_column();

-- 6. Performance Indexes
CREATE INDEX IF NOT EXISTS idx_contact_messages_status ON contact_messages(status);
CREATE INDEX IF NOT EXISTS idx_contact_messages_created_at ON contact_messages(created_at DESC);


-- ============================================================================
-- FIREBASE AUTH INTEGRATION NOTES:
-- ============================================================================
-- When integrating Firebase Auth with Supabase:
-- 1. Users authenticate in your mobile/web app using Firebase Auth (e.g. Google Sign-In or Email/Password).
-- 2. Upon successful authentication, your app obtains a Firebase ID Token (JWT).
-- 3. To securely query Supabase as an authenticated user, you can either:
--    - Exchange the Firebase JWT for a Supabase-compatible JWT using a Supabase Edge Function / Custom Auth Hooker, OR
--    - Use Supabase Auth directly alongside Firebase, OR
--    - Configure Supabase JWT secret to verify Firebase tokens if using custom JWT validation.
-- The RLS policies above (`TO authenticated`) automatically enforce that only requests 
-- authenticated with a valid Supabase JWT (`role = 'authenticated'`) can perform SELECT, 
-- UPDATE, and DELETE operations, while anon visitors can only INSERT.
-- ============================================================================


-- ============================================================================
-- EXAMPLE SQL QUERIES
-- ============================================================================

-- Example 1: Inserting a new contact message (Simulates public submission from the app)
INSERT INTO contact_messages (name, phone_whatsapp, message)
VALUES ('Kofi Mensah', '+233541234567', 'Hello LRock.dev, I need a custom e-commerce mobile app built for my business in Accra.');

-- Example 2: Selecting all new messages (Run as authenticated admin)
SELECT id, name, phone_whatsapp, message, created_at 
FROM contact_messages 
WHERE status = 'new' 
ORDER BY created_at DESC;

-- Example 3: Updating message status to 'contacted'
UPDATE contact_messages 
SET status = 'contacted' 
WHERE id = 'YOUR_RECORD_UUID_HERE';
