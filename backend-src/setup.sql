-- Create database
CREATE DATABASE IF NOT EXISTS clicker;
USE clicker;

-- Create responses table
CREATE TABLE IF NOT EXISTS responses (
    id         INT AUTO_INCREMENT PRIMARY KEY,
    questionNo INT          NOT NULL,
    choice     VARCHAR(10)  NOT NULL,
    created_at TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);

-- Count votes per choice (for question 1)
SELECT choice, COUNT(*) AS total
FROM responses
WHERE questionNo = 1
GROUP BY choice
ORDER BY choice;

-- Create users table (extra activity 1: login/register system)
CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE,
    password VARCHAR(50)
);

 -- Comment table (extra activity 2: comment system)
CREATE TABLE IF NOT EXISTS comments (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    comment_text TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);