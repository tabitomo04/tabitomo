-- Create likes table
CREATE TABLE IF NOT EXISTS likes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(50) NOT NULL,
    storybook_id INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (email) REFERENCES member(email) ON DELETE CASCADE,
    FOREIGN KEY (storybook_id) REFERENCES storybook(book_num) ON DELETE CASCADE,
    UNIQUE KEY uk_likes_member_storybook (email, storybook_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
