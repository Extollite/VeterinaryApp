package pl.gr.veterinaryapp.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.ReportingPolicy;
import pl.gr.veterinaryapp.model.dto.PetResponseDto;
import pl.gr.veterinaryapp.model.entity.Pet;

import java.util.Collection;
import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PetMapper {

    @Mappings({
            @Mapping(source = "animal.id", target = "animalId"),
            @Mapping(source = "client.id", target = "clientId")
    })
    PetResponseDto map(Pet pet);

    List<PetResponseDto> mapAsList(Collection<Pet> pets);
}
