# Bristol OOP: Database Project

Coursework for the Bristol 'Object-Oriented Programming with Java' module.

This is a custom SQL-like database language and server written in Java.

## Features

Basic commands:

```
USE database_name;
CREATE DATABASE database_name;
CREATE TABLE table_name;
INSERT INTO table_name VALUES ...;
UPDATE table_name SET ... WHERE ...;
DELETE FROM table_name WHERE ...;
SELECT * FROM table_name WHERE ...;
```

Joins are supported:

```
JOIN table_1 AND table_2 ON table_1_attribute AND table_2_attribute
```

Selects with nested AND/OR conditions are supported:

```
SELECT * FROM people WHERE ((id==1) and (Name=='Bob')) or (id == 2);
```

For full syntax see the [BNF Specification](BNF.txt)

For examples see the unit tests: [DBTests](src/test/java/edu/uob/DBTests.java)

## Components

[DBServer](src/main/java/edu/uob/DBServer.java) - listens on a TCP socket for incoming connections,
and responds to database commands.

[DBClient](src/main/java/edu/uob/DBClient.java) - A command line application that opens a connection
to the `DBServer`, and allows sending commands and receiving responses.
