CREATE DATABASE library_db;  
USE library_db;

CREATE TABLE IF NOT EXISTS books (
    book_id        INT          NOT NULL AUTO_INCREMENT,
    title          VARCHAR(200) NOT NULL,
    author         VARCHAR(150) NOT NULL,
    category       VARCHAR(100),
    quantity       INT          NOT NULL DEFAULT 1,   -- available copies
    total_quantity INT          NOT NULL DEFAULT 1,   -- original stock
    PRIMARY KEY (book_id)
);
SELECT * FROM books;

CREATE TABLE IF NOT EXISTS members (
    member_id  INT          NOT NULL AUTO_INCREMENT,
    name       VARCHAR(150) NOT NULL,
    email      VARCHAR(150),
    phone      VARCHAR(20),
    join_date  DATE         NOT NULL DEFAULT (CURDATE()),
    PRIMARY KEY (member_id)
);

CREATE TABLE IF NOT EXISTS transactions (
    transaction_id INT          NOT NULL AUTO_INCREMENT,
    member_id      INT          NOT NULL,
    book_id        INT          NOT NULL,
    issue_date     DATE         NOT NULL DEFAULT (CURDATE()),
    due_date       DATE         NOT NULL,
    return_date    DATE                  DEFAULT NULL,   -- NULL = not yet returned
    fine           DECIMAL(8,2)          DEFAULT 0.00,
    PRIMARY KEY (transaction_id),
    FOREIGN KEY (member_id) REFERENCES members(member_id) ON DELETE CASCADE,
    FOREIGN KEY (book_id)   REFERENCES books(book_id)     ON DELETE CASCADE
);

CREATE INDEX idx_tx_member  ON transactions(member_id);
CREATE INDEX idx_tx_book    ON transactions(book_id);
CREATE INDEX idx_tx_status  ON transactions(return_date, due_date);


INSERT INTO books (title, author, category, quantity, total_quantity) VALUES
  ('Clean Code',              'Robert C. Martin',  'Programming',   3, 3),
  ('The Pragmatic Programmer','David Thomas',       'Programming',   2, 2),
  ('Design Patterns',         'Gang of Four',       'Architecture',  2, 2),
  ('Sapiens',                 'Yuval Noah Harari',  'History',       5, 5),
  ('Atomic Habits',           'James Clear',        'Self-Help',     4, 4);
  
  
  
   
INSERT INTO members (name, email, phone) VALUES
  ('Alice Kumar',   'alice@example.com',  '9876543210'),
  ('Bob Ramesh',    'bob@example.com',    '9123456789'),
  ('Carol Nair',    'carol@example.com',  '9000000001');

SHOW tables;
select * from books;
select * from members;
select * from transactions;