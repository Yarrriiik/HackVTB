CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS pgcrypto;
CREATE EXTENSION IF NOT EXISTS citext;

CREATE TABLE IF NOT EXISTS users (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  name TEXT NOT NULL,
  email CITEXT NOT NULL UNIQUE,
  password TEXT NOT NULL,
  role VARCHAR(20) NOT NULL,
  created_at TIMESTAMP DEFAULT NOW()
);

INSERT INTO users (name, email, password, role)
VALUES ('Demo User', 'demo@orchestra.local', crypt('Demo123!', gen_salt('bf')), 'USER')
ON CONFLICT (email) DO NOTHING;

CREATE TABLE IF NOT EXISTS process_diagram (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name TEXT NOT NULL,
    type VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS process_step (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    diagram_id UUID NOT NULL REFERENCES process_diagram(id) ON DELETE CASCADE,
    step_id TEXT NOT NULL,
    name TEXT,
    actor_from TEXT,
    actor_to TEXT,
    action TEXT,
    next_steps JSONB,
    CONSTRAINT unique_step_diagram UNIQUE (diagram_id, step_id)
);

CREATE INDEX IF NOT EXISTS idx_step_diagram ON process_step(diagram_id);
CREATE INDEX IF NOT EXISTS idx_step_step_id ON process_step(step_id);

CREATE TABLE IF NOT EXISTS process_transition (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    diagram_id UUID NOT NULL REFERENCES process_diagram(id) ON DELETE CASCADE,
    from_step UUID NOT NULL REFERENCES process_step(id) ON DELETE CASCADE,
    to_step UUID NOT NULL REFERENCES process_step(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_transition_diagram ON process_transition(diagram_id);
CREATE INDEX IF NOT EXISTS idx_transition_from ON process_transition(from_step);
CREATE INDEX IF NOT EXISTS idx_transition_to ON process_transition(to_step);

CREATE TABLE IF NOT EXISTS step_api_binding (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    step_id UUID NOT NULL REFERENCES process_step(id) ON DELETE CASCADE,
    http_method VARCHAR(10),
    api_path TEXT,
    confidence_score DOUBLE PRECISION
);

CREATE INDEX IF NOT EXISTS idx_api_binding_step ON step_api_binding(step_id);

CREATE TABLE IF NOT EXISTS generated_test_case (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    diagram_id UUID NOT NULL REFERENCES process_diagram(id) ON DELETE CASCADE,
    name TEXT,
    steps_json JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_test_case_diagram ON generated_test_case(diagram_id);

CREATE TABLE IF NOT EXISTS generated_test_data (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    test_case_id UUID NOT NULL REFERENCES generated_test_case(id) ON DELETE CASCADE,
    step_id UUID NOT NULL REFERENCES process_step(id) ON DELETE CASCADE,
    request_payload JSONB,
    response_expected JSONB
);

CREATE TABLE IF NOT EXISTS sequence_diagrams (
  id UUID PRIMARY KEY,
  name TEXT,
  format VARCHAR(20),
  raw_content TEXT NOT NULL,
  created_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS openapi_specs (
  id UUID PRIMARY KEY,
  name TEXT,
  file_name TEXT,
  spec_json JSONB NOT NULL,
  created_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_testdata_case ON generated_test_data(test_case_id);
CREATE INDEX IF NOT EXISTS idx_testdata_step ON generated_test_data(step_id);
