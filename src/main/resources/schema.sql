DROP TABLE IF EXISTS users, requests, items, bookings, comments CASCADE;

-- create table users
CREATE TABLE IF NOT EXISTS users
(
    id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL,
    UNIQUE(email)
);

-- create table requests
CREATE TABLE IF NOT EXISTS requests
(
    id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    description VARCHAR(200) NOT NULL,
    requestor_id INT REFERENCES users(id)
);

-- create table items
CREATE TABLE IF NOT EXISTS items
(
    id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(300) NOT NULL,
    is_available BOOLEAN NOT NULL,
    owner_id INT REFERENCES users(id),
    request_id INT REFERENCES requests(id)
);

-- create table bookings
CREATE TABLE IF NOT EXISTS bookings
(
    id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    start_date TIMESTAMP WITH TIME ZONE NOT NULL,
    end_date TIMESTAMP WITH TIME ZONE NOT NULL,
    status VARCHAR(8) NOT NULL,
    item_id INT REFERENCES items(id),
    booker_id INT REFERENCES users(id)
);

-- create table comments
CREATE TABLE IF NOT EXISTS comments
(
    id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    text VARCHAR(200) NOT NULL,
    created TIMESTAMP WITH TIME ZONE NOT NULL,
    item_id INT REFERENCES items(id),
    author_id INT REFERENCES users(id)
);
