CREATE TABLE dog(
 id serial PRIMARY KEY,
 name VARCHAR (50) NOT NULL,
 breed VARCHAR (50) NOT NULL,
 timestamp TIMESTAMP NOT NULL
);

INSERT INTO dog (name, breed, timestamp) VALUES ('Rex', 'Labrador', NOW())