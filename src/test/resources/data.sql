-- Users (test data for JDBC slice tests)
INSERT INTO users (id, version, created_date, last_modified_date, created_by, last_modified_by, email, name, password, role, street, city, postal_code)
VALUES
    (1, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system', 'admin@example.com', 'Admin User', 'admin', 'ADMIN', '123 Admin St', 'Adminville', 'A1A 1A1'),
    (2, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system', 'user1@example.com', 'John Doe', 'user1', 'USER', '456 User Ave', 'Userville', 'U1U 1U1'),
    (3, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system', 'user2@example.com', 'Jane Smith', 'user2', 'USER', '789 Guest Rd', 'Guestown', 'G2G 2G2');
