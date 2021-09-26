package pl.gr.veterinaryapp.service;

import pl.gr.veterinaryapp.model.dto.AnimalRequestDto;
import pl.gr.veterinaryapp.model.entity.Animal;

import java.util.List;

public interface AnimalService {

    Animal getAnimalById(long id);

    Animal createAnimal(AnimalRequestDto animalRequestDTO);

    void deleteAnimal(long id);

    List<Animal> getAllAnimals();
}
