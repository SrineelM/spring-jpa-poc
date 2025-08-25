-- Users
INSERT INTO users (id, version, created_date, last_modified_date, created_by, last_modified_by, email, name, password, role, street, city, postal_code)
VALUES
    (1, 0, NOW(), NOW(), 'system', 'system', 'admin@example.com', 'Admin User', 'admin', 'ADMIN', '123 Admin St', 'Adminville', 'A1A 1A1'),
    (2, 0, NOW(), NOW(), 'system', 'system', 'user1@example.com', 'John Doe', 'user1', 'USER', '456 User Ave', 'Userville', 'U1U 1U1'),
    (3, 0, NOW(), NOW(), 'system', 'system', 'user2@example.com', 'Jane Smith', 'user2', 'USER', '789 Guest Rd', 'Guestown', 'G2G 2G2');

-- Profiles
INSERT INTO profile (user_id, bio)
VALUES
    (1, 'I am the administrator.'),
    (2, 'I am a regular user.'),
    (3, 'I am another regular user.');

-- Posts
INSERT INTO posts (id, version, created_date, last_modified_date, created_by, last_modified_by, title, content, user_id)
VALUES
    (1, 0, NOW(), NOW(), 'user1@example.com', 'user1@example.com', 'First Post', 'This is the content of the first post.', 2),
    (2, 0, NOW(), NOW(), 'user2@example.com', 'user2@example.com', 'Second Post', 'This is the content of the second post.', 3),
    (3, 0, NOW(), NOW(), 'user1@example.com', 'user1@example.com', 'Third Post', 'This is some more content.', 2);

-- Comments
INSERT INTO comments (id, version, created_date, last_modified_date, created_by, last_modified_by, content, post_id, user_id)
VALUES
    (1, 0, NOW(), NOW(), 'user2@example.com', 'user2@example.com', 'Great post!', 1, 3),
    (2, 0, NOW(), NOW(), 'user1@example.com', 'user1@example.com', 'Thanks!', 1, 2),
    (3, 0, NOW(), NOW(), 'admin@example.com', 'admin@example.com', 'Interesting discussion.', 1, 1),
    (4, 0, NOW(), NOW(), 'user2@example.com', 'user2@example.com', 'I agree.', 2, 3);

-- Groups
INSERT INTO `group` (id, version, created_date, last_modified_date, created_by, last_modified_by, name)
VALUES
    (1, 0, NOW(), NOW(), 'system', 'system', 'Developers'),
    (2, 0, NOW(), NOW(), 'system', 'system', 'Testers');

-- User-Group Associations
INSERT INTO user_groups (user_id, group_id)
VALUES
    (1, 1),
    (2, 1),
    (3, 2);

-- User Tags
INSERT INTO user_tags (user_id, tag)
VALUES
    (1, 'java'),
    (1, 'spring'),
    (2, 'jpa'),
    (2, 'hibernate'),
    (3, 'sql');
