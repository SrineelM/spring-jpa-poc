-- Minimal schema for JDBC slice tests; JPA tests will ignore because of defer-datasource-initialization
CREATE TABLE IF NOT EXISTS users (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  version BIGINT,
  created_date TIMESTAMP,
  last_modified_date TIMESTAMP,
  created_by VARCHAR(255),
  last_modified_by VARCHAR(255),
  email VARCHAR(255) NOT NULL UNIQUE,
  name VARCHAR(255) NOT NULL,
  password VARCHAR(255) NOT NULL,
  role VARCHAR(32),
  street VARCHAR(255),
  city VARCHAR(255),
  postal_code VARCHAR(64)
);

CREATE TABLE IF NOT EXISTS profiles (
  user_id BIGINT PRIMARY KEY,
  bio CLOB
);

CREATE TABLE IF NOT EXISTS posts (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  version BIGINT,
  created_date TIMESTAMP,
  last_modified_date TIMESTAMP,
  created_by VARCHAR(255),
  last_modified_by VARCHAR(255),
  title VARCHAR(255),
  content CLOB,
  user_id BIGINT
);

CREATE TABLE IF NOT EXISTS comments (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  version BIGINT,
  created_date TIMESTAMP,
  last_modified_date TIMESTAMP,
  created_by VARCHAR(255),
  last_modified_by VARCHAR(255),
  content CLOB,
  post_id BIGINT,
  user_id BIGINT
);

CREATE TABLE IF NOT EXISTS groups (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  version BIGINT,
  created_date TIMESTAMP,
  last_modified_date TIMESTAMP,
  created_by VARCHAR(255),
  last_modified_by VARCHAR(255),
  name VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS user_groups (
  user_id BIGINT,
  group_id BIGINT
);

CREATE TABLE IF NOT EXISTS user_tags (
  user_id BIGINT,
  tag VARCHAR(255)
);
