package pl.gr.veterinaryapp.model.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.hateoas.RepresentationModel;

import java.time.LocalDate;

@EqualsAndHashCode(callSuper = true)
@Data
public class PetResponseDto extends RepresentationModel<PetResponseDto> {

    private long id;
    private String name;
    private LocalDate birthDate;
    private long animalId;
    private long clientId;
}
