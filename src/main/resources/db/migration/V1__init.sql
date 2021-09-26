CREATE TABLE ANIMALS
(
    ID      INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY NOT NULL,
    SPECIES varchar(256) UNIQUE                          NOT NULL
);


CREATE TABLE VETS
(
    ID              INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY NOT NULL,
    NAME            varchar(256)                                 NOT NULL,
    SURNAME         varchar(256)                                 NOT NULL,
    PHOTO_URL       varchar(256),
    WORK_START_TIME time                                         NOT NULL,
    WORK_END_TIME   time                                         NOT NULL
);

CREATE TABLE CLIENTS
(
    ID      INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY NOT NULL,
    name    varchar(256)                                 NOT NULL,
    surname varchar(256)                                 NOT NULL
);

CREATE TABLE VISITS
(
    ID                INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY NOT NULL,
    VET_ID            int                                          NOT NULL,
    PET_ID            int                                          NOT NULL,
    START_DATE_TIME   timestamp                                    NOT NULL,
    DURATION          INTERVAL                                     NOT NULL,
    PRICE             money                                        NOT NULL,
    VISIT_TYPE        int                                          NOT NULL,
    VISIT_DESCRIPTION varchar(1024),
    VISIT_STATUS      int                                          NOT NULL,
    OPERATION_TYPE    int                                          NOT NULL,

    CONSTRAINT fk_vet
        FOREIGN KEY (VET_ID)
            REFERENCES VETS (ID),

    CONSTRAINT fk_animal
        FOREIGN KEY (PET_ID)
            REFERENCES ANIMALS (ID)
);

CREATE TABLE PETS
(
    ID         INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY NOT NULL,
    NAME       varchar(256)                                 NOT NULL,
    BIRTH_DATE date                                         NOT NULL,
    ANIMAL_ID  int                                          NOT NULL,
    CLIENT_ID  int                                          NOT NULL,

    CONSTRAINT fk_animal
        FOREIGN KEY (ANIMAL_ID)
            REFERENCES ANIMALS (ID),

    CONSTRAINT fk_client
        FOREIGN KEY (CLIENT_ID)
            REFERENCES CLIENTS (ID)
);