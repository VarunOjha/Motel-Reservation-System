-- H2 initialization script for test database
-- This script creates tables compatible with H2 database

CREATE TABLE IF NOT EXISTS motel_chain (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    status VARCHAR(50),
    address CLOB,  -- Using CLOB instead of JSONB for H2 compatibility
    contact_info CLOB,  -- Using CLOB instead of JSONB for H2 compatibility
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP
);

-- Create indexes for common queries
CREATE INDEX IF NOT EXISTS idx_motel_chain_name ON motel_chain(name);
CREATE INDEX IF NOT EXISTS idx_motel_chain_status ON motel_chain(status);
CREATE INDEX IF NOT EXISTS idx_motel_chain_created_at ON motel_chain(created_at);
