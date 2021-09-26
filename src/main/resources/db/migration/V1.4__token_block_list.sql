CREATE TABLE BLOCKED_TOKENS
(
    ID              INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY NOT NULL,
    TOKEN           varchar(1000) UNIQUE                         NOT NULL,
    EXPIRATION_TIME timestamp                                    NOT NULL
)