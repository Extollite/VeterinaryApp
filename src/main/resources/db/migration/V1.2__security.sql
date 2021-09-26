CREATE TABLE USER_ROLE
(
    ID   INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY NOT NULL,
    NAME varchar(256) UNIQUE                          NOT NULL
);

CREATE TABLE USERS
(
    ID       INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY NOT NULL,
    ROLE_ID  INT                                          NOT NULL,
    PASSWORD varchar(1024)                                NOT NULL,
    USERNAME varchar(256) UNIQUE                          NOT NULL
);

INSERT INTO USER_ROLE(NAME)
VALUES ('ADMIN');

INSERT INTO USER_ROLE(NAME)
VALUES ('CLIENT');

INSERT INTO USER_ROLE(NAME)
VALUES ('VET');