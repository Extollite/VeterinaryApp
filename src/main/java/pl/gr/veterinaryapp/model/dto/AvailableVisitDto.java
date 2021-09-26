package pl.gr.veterinaryapp.model.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.hateoas.RepresentationModel;

import java.time.OffsetDateTime;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class AvailableVisitDto extends RepresentationModel<PetResponseDto> {

    private List<Long> vetIds;
    private OffsetDateTime startDateTime;
}
