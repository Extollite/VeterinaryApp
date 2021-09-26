ALTER TABLE visits drop constraint fk_animal;
ALTER TABLE visits add constraint fk_pet
    FOREIGN KEY (PET_ID)
        REFERENCES PETS (ID);