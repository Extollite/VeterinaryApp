package pl.gr.veterinaryapp.service;

import org.springframework.security.core.userdetails.User;
import pl.gr.veterinaryapp.model.dto.PetRequestDto;
import pl.gr.veterinaryapp.model.entity.Pet;

import java.util.List;

public interface PetService {

    Pet getPetById(User user, long id);

    Pet createPet(User user, PetRequestDto petRequestDTO);

    void deletePet(long id);

    List<Pet> getAllPets(User user);
}
