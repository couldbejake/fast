CREATE TABLE proxies (
  id SERIAL PRIMARY KEY,
  conn_string VARCHAR(255) NOT NULL,
  ip_address VARCHAR(255) NOT NULL,
  port INTEGER NOT NULL,
  is_socks BOOLEAN NOT NULL,
  usage_count INTEGER DEFAULT 0,
  retry_count INTEGER DEFAULT 0,
  next_available TIMESTAMP DEFAULT '1970-01-01 00:00:00'::TIMESTAMP,
  guest_token VARCHAR(255),
  guest_token_updated TIMESTAMP DEFAULT '1970-01-01 00:00:00'::TIMESTAMP,
  success_delta INTEGER DEFAULT 0,
  failed_count INTEGER DEFAULT 0,
  last_updated TIMESTAMP DEFAULT '1970-01-01 00:00:00'::TIMESTAMP
);

GRANT SELECT, INSERT, UPDATE, DELETE ON TABLE proxies TO scrapium_user;
GRANT USAGE, SELECT ON SEQUENCE proxies_id_seq1 TO scrapium_user;

#

-- Create the test_proxy table
CREATE TABLE test_proxy (
    id SERIAL PRIMARY KEY,
    connection_string VARCHAR(255) UNIQUE,
    usage_count INTEGER DEFAULT 0,
    success_count INTEGER DEFAULT 0,
    failed_count INTEGER DEFAULT 0,
    fail_streak INTEGER DEFAULT 0,
    cooldown_until TIMESTAMPTZ
);

-- Grant privileges to scrapium_user
GRANT ALL PRIVILEGES ON TABLE test_proxy TO scrapium_user;
GRANT USAGE, SELECT ON SEQUENCE test_proxy_id_seq TO scrapium_user;


#



CREATE TABLE test_proxy (
    id SERIAL PRIMARY KEY,
    connection_string VARCHAR(255),
    usage_count INTEGER NOT NULL DEFAULT 0,
    success_count INTEGER NOT NULL DEFAULT 0,
    failed_count INTEGER NOT NULL DEFAULT 0,
    fail_streak INTEGER NOT NULL DEFAULT 0,
    cooldown_until TIMESTAMP WITH TIME ZONE,
    last_used TIMESTAMP WITH TIME ZONE,
    status VARCHAR(50) NOT NULL DEFAULT 'active'
);

CREATE INDEX idx_cooldown ON test_proxy (cooldown_until);
CREATE INDEX idx_usage_count ON test_proxy (usage_count);
CREATE INDEX idx_last_used ON test_proxy (last_used);
GRANT ALL PRIVILEGES ON TABLE test_proxy TO scrapium_user;
GRANT USAGE, SELECT ON SEQUENCE test_proxy_id_seq TO scrapium_user;