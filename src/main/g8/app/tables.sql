CREATE TABLE users (
  uuid UUID PRIMARY KEY,
  name VARCHAR UNIQUE NOT NULL,
  password VARCHAR NOT NULL
);