package pl.gr.veterinaryapp.model.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class PetRequestDto {

    private String name;
    private LocalDate birthDate;
    private long animalId;
    private long clientId;
}
