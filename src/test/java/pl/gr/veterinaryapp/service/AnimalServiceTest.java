package pl.gr.veterinaryapp.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.gr.veterinaryapp.exception.IncorrectDataException;
import pl.gr.veterinaryapp.exception.ResourceNotFoundException;
import pl.gr.veterinaryapp.mapper.AnimalMapper;
import pl.gr.veterinaryapp.model.dto.AnimalRequestDto;
import pl.gr.veterinaryapp.model.entity.Animal;
import pl.gr.veterinaryapp.repository.AnimalRepository;
import pl.gr.veterinaryapp.service.impl.AnimalServiceImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnimalServiceTest {

    private static final long ANIMAL_ID = 1L;
    @Mock
    private AnimalRepository animalRepository;
    @Mock
    private AnimalMapper mapper;
    @InjectMocks
    private AnimalServiceImpl animalService;

    @Test
    void getAnimalById_WithCorrectId_Returned() {
        Animal animal = new Animal();

        when(animalRepository.findById(anyLong())).thenReturn(Optional.of(animal));

        var result = animalService.getAnimalById(ANIMAL_ID);

        assertThat(result)
                .isNotNull()
                .isEqualTo(animal);

        verify(animalRepository).findById(eq(ANIMAL_ID));
        verifyNoInteractions(mapper);
    }

    @Test
    void getAnimalById_WithWrongId_ExceptionThrown() {
        when(animalRepository.findById(anyLong())).thenReturn(Optional.empty());

        ResourceNotFoundException thrown =
                catchThrowableOfType(() -> animalService.getAnimalById(ANIMAL_ID), ResourceNotFoundException.class);

        assertThat(thrown)
                .hasMessage("Wrong id.");

        verify(animalRepository).findById(eq(ANIMAL_ID));
        verifyNoInteractions(mapper);
    }

    @Test
    void createAnimal_NewAnimal_Created() {
        AnimalRequestDto animalDTO = new AnimalRequestDto();
        animalDTO.setSpecies("test");
        Animal animal = new Animal();
        animal.setSpecies("test");

        when(animalRepository.findBySpecies(anyString())).thenReturn(Optional.empty());
        when(mapper.map(any(AnimalRequestDto.class))).thenReturn(animal);
        when(animalRepository.save(any(Animal.class))).thenReturn(animal);

        var result = animalService.createAnimal(animalDTO);

        assertThat(result)
                .isNotNull()
                .isEqualTo(animal);

        verify(animalRepository).save(eq(animal));
        verify(animalRepository).findBySpecies(eq("test"));
        verify(mapper).map(eq(animalDTO));
    }

    @Test
    void createAnimal_ExistsAnimal_ExceptionThrown() {
        AnimalRequestDto animalDTO = new AnimalRequestDto();
        animalDTO.setSpecies("test");
        Animal animal = new Animal();
        animal.setSpecies("test");

        when(animalRepository.findBySpecies(anyString())).thenReturn(Optional.of(animal));

        IncorrectDataException thrown =
                catchThrowableOfType(() -> animalService.createAnimal(animalDTO), IncorrectDataException.class);

        assertThat(thrown)
                .hasMessage("Species exists.");

        verify(animalRepository).findBySpecies(eq("test"));
        verifyNoInteractions(mapper);
    }

    @Test
    void deleteAnimal_ExistsAnimal_Deleted() {
        Animal animal = new Animal();

        when(animalRepository.findById(anyLong())).thenReturn(Optional.of(animal));

        animalService.deleteAnimal(ANIMAL_ID);

        verify(animalRepository).findById(eq(ANIMAL_ID));
        verify(animalRepository).delete(eq(animal));
        verifyNoInteractions(mapper);
    }

    @Test
    void deleteAnimal_AnimalNotFound_ThrownException() {

        when(animalRepository.findById(anyLong())).thenReturn(Optional.empty());

        ResourceNotFoundException thrown =
                catchThrowableOfType(() -> animalService.deleteAnimal(ANIMAL_ID), ResourceNotFoundException.class);

        assertThat(thrown)
                .hasMessage("Wrong id.");

        verify(animalRepository).findById(eq(ANIMAL_ID));
        verifyNoInteractions(mapper);
    }

    @Test
    void getAllAnimals_ReturnAnimals_Returned() {
        List<Animal> animals = new ArrayList<>();

        when(animalRepository.findAll()).thenReturn(animals);

        var result = animalService.getAllAnimals();

        assertThat(result)
                .isNotNull();

        verify(animalRepository).findAll();
        verifyNoInteractions(mapper);
    }
}