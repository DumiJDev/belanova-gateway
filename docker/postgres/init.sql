-- Belanova Gateway Database Initialization Script
-- This script creates the necessary database schema for the Belanova Gateway

-- Create database if it doesn't exist
-- Note: This is handled by POSTGRES_DB environment variable

-- Create extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- Create custom types
DO $$ BEGIN
    CREATE TYPE http_method AS ENUM ('GET', 'POST', 'PUT', 'DELETE', 'PATCH', 'OPTIONS', 'HEAD');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

-- Create backends table
CREATE TABLE IF NOT EXISTS backends (
    id VARCHAR(255) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    base_url VARCHAR(500),
    service_id VARCHAR(255),
    general_path VARCHAR(255),
    enabled BOOLEAN NOT NULL DEFAULT true,
    use_service_discovery BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Create services table
CREATE TABLE IF NOT EXISTS services (
    id VARCHAR(255) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    path VARCHAR(500) NOT NULL,
    backend_id VARCHAR(255) NOT NULL REFERENCES backends(id) ON DELETE CASCADE,
    enabled BOOLEAN NOT NULL DEFAULT true,
    metadata JSONB DEFAULT '{}',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Create service_methods table (many-to-many relationship)
CREATE TABLE IF NOT EXISTS service_methods (
    service_id VARCHAR(255) NOT NULL REFERENCES services(id) ON DELETE CASCADE,
    method http_method NOT NULL,
    PRIMARY KEY (service_id, method)
);

-- Create upstreams table
CREATE TABLE IF NOT EXISTS upstreams (
    id VARCHAR(255) PRIMARY KEY,
    backend_id VARCHAR(255) NOT NULL REFERENCES backends(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    url VARCHAR(500) NOT NULL,
    weight INTEGER NOT NULL DEFAULT 1,
    enabled BOOLEAN NOT NULL DEFAULT true,
    health_check_path VARCHAR(255) DEFAULT '/health',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Create consumers table
CREATE TABLE IF NOT EXISTS consumers (
    id VARCHAR(255) PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(255),
    enabled BOOLEAN NOT NULL DEFAULT true,
    credentials JSONB DEFAULT '{}',
    metadata JSONB DEFAULT '{}',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Create plugins table
CREATE TABLE IF NOT EXISTS plugins (
    id VARCHAR(255) PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    phase VARCHAR(50) NOT NULL,
    order_index INTEGER NOT NULL DEFAULT 0,
    enabled BOOLEAN NOT NULL DEFAULT true,
    configuration JSONB DEFAULT '{}',
    description TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Create routes table
CREATE TABLE IF NOT EXISTS routes (
    id VARCHAR(255) PRIMARY KEY,
    route_id VARCHAR(255) NOT NULL UNIQUE,
    path VARCHAR(500) NOT NULL,
    target_uri VARCHAR(500) NOT NULL,
    methods JSONB DEFAULT '[]',
    filters JSONB DEFAULT '[]',
    enabled BOOLEAN NOT NULL DEFAULT true,
    metadata JSONB DEFAULT '{}',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Create health_checks table
CREATE TABLE IF NOT EXISTS health_checks (
    id VARCHAR(255) PRIMARY KEY,
    backend_id VARCHAR(255) NOT NULL REFERENCES backends(id) ON DELETE CASCADE,
    healthy BOOLEAN NOT NULL DEFAULT false,
    response_time BIGINT,
    status_code INTEGER,
    last_checked TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    message TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Create request_logs table
CREATE TABLE IF NOT EXISTS request_logs (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    timestamp TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    method VARCHAR(10),
    path VARCHAR(500),
    query_string TEXT,
    client_ip VARCHAR(45),
    user_agent TEXT,
    status_code INTEGER,
    response_size BIGINT,
    processing_time_ms BIGINT,
    backend VARCHAR(255),
    service VARCHAR(255),
    user_id VARCHAR(255),
    request_id VARCHAR(255)
);

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_backends_enabled ON backends(enabled);
CREATE INDEX IF NOT EXISTS idx_services_backend_id ON services(backend_id);
CREATE INDEX IF NOT EXISTS idx_services_enabled ON services(enabled);
CREATE INDEX IF NOT EXISTS idx_upstreams_backend_id ON upstreams(backend_id);
CREATE INDEX IF NOT EXISTS idx_upstreams_enabled ON upstreams(enabled);
CREATE INDEX IF NOT EXISTS idx_consumers_username ON consumers(username);
CREATE INDEX IF NOT EXISTS idx_consumers_enabled ON consumers(enabled);
CREATE INDEX IF NOT EXISTS idx_plugins_name ON plugins(name);
CREATE INDEX IF NOT EXISTS idx_plugins_enabled ON plugins(enabled);
CREATE INDEX IF NOT EXISTS idx_routes_route_id ON routes(route_id);
CREATE INDEX IF NOT EXISTS idx_routes_enabled ON routes(enabled);
CREATE INDEX IF NOT EXISTS idx_health_checks_backend_id ON health_checks(backend_id);
CREATE INDEX IF NOT EXISTS idx_health_checks_last_checked ON health_checks(last_checked);
CREATE INDEX IF NOT EXISTS idx_request_logs_timestamp ON request_logs(timestamp);
CREATE INDEX IF NOT EXISTS idx_request_logs_method ON request_logs(method);
CREATE INDEX IF NOT EXISTS idx_request_logs_status_code ON request_logs(status_code);
CREATE INDEX IF NOT EXISTS idx_request_logs_user_id ON request_logs(user_id);

-- Create updated_at trigger function
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Create triggers for updated_at
CREATE TRIGGER update_backends_updated_at BEFORE UPDATE ON backends FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_services_updated_at BEFORE UPDATE ON services FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_upstreams_updated_at BEFORE UPDATE ON upstreams FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_consumers_updated_at BEFORE UPDATE ON consumers FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_plugins_updated_at BEFORE UPDATE ON plugins FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_routes_updated_at BEFORE UPDATE ON routes FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Insert default data
INSERT INTO consumers (id, username, email, enabled) VALUES
('admin-user', 'admin', 'admin@belanova.gateway', true)
ON CONFLICT (id) DO NOTHING;

INSERT INTO plugins (id, name, phase, order_index, enabled, configuration, description) VALUES
('jwt-auth', 'jwt-auth', 'AUTH', 100, true, '{"secret": "default-jwt-secret-key-for-belanova-gateway", "issuer": "belanova-gateway", "audience": "belanova-api"}', 'JWT Authentication Plugin'),
('rate-limit', 'rate-limit', 'PRE_REQUEST', 200, true, '{"maxRequests": 100, "windowSizeMs": 60000}', 'Rate Limiting Plugin'),
('request-logging', 'request-logging', 'POST_REQUEST', 1000, true, '{"logLevel": "INFO", "includeHeaders": false, "includeBody": false}', 'Request Logging Plugin')
ON CONFLICT (id) DO NOTHING;

-- Create a sample backend for testing
INSERT INTO backends (id, name, description, base_url, enabled, use_service_discovery) VALUES
('sample-backend', 'Sample Backend', 'Sample backend service for testing', 'http://sample-backend', true, false)
ON CONFLICT (id) DO NOTHING;

-- Create a sample service
INSERT INTO services (id, name, path, backend_id, enabled) VALUES
('sample-api', 'Sample API', '/api/**', 'sample-backend', true)
ON CONFLICT (id) DO NOTHING;

-- Add HTTP methods for the sample service
INSERT INTO service_methods (service_id, method) VALUES
('sample-api', 'GET'),
('sample-api', 'POST'),
('sample-api', 'PUT'),
('sample-api', 'DELETE')
ON CONFLICT (service_id, method) DO NOTHING;

-- Create a sample upstream
INSERT INTO upstreams (id, backend_id, name, url, weight, enabled) VALUES
('sample-upstream-1', 'sample-backend', 'Sample Upstream 1', 'http://sample-backend', 1, true)
ON CONFLICT (id) DO NOTHING;

-- Grant permissions (if needed for specific users)
-- GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO belanova;
-- GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO belanova;