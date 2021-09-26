package pl.gr.veterinaryapp.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.ReportingPolicy;
import pl.gr.veterinaryapp.model.dto.UserDto;
import pl.gr.veterinaryapp.model.entity.VetAppUser;

import java.util.Collection;
import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface VetAppUserMapper {

    @Mappings({
            @Mapping(source = "username", target = "username"),
            @Mapping(source = "role.id", target = "role")
    })
    UserDto map(VetAppUser pet);

    List<UserDto> mapAsList(Collection<VetAppUser> users);
}
