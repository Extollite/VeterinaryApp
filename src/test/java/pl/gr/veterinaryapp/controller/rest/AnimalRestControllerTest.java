package pl.gr.veterinaryapp.controller.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import pl.gr.veterinaryapp.config.WebSecurityConfig;
import pl.gr.veterinaryapp.jwt.JwtAuthenticationFilter;
import pl.gr.veterinaryapp.model.dto.AnimalRequestDto;
import pl.gr.veterinaryapp.model.entity.Animal;
import pl.gr.veterinaryapp.service.AnimalService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AnimalRestController.class,
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
                        classes = {WebSecurityConfigurerAdapter.class, JwtAuthenticationFilter.class})
        },
        excludeAutoConfiguration = {WebSecurityConfig.class})
class AnimalRestControllerTest {

    private static final long ID = 1L;
    private static final String SPECIES = "CAT";

    @MockBean
    private AnimalService animalService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser
    void addAnimal_CorrectData_Created() throws Exception {
        AnimalRequestDto animalRequest = new AnimalRequestDto();
        animalRequest.setSpecies(SPECIES);

        Animal animal = new Animal();
        animal.setSpecies(animalRequest.getSpecies());

        when(animalService.createAnimal(any(AnimalRequestDto.class))).thenReturn(animal);

        mockMvc.perform(post("/api/animals")
                .with(csrf())
                .content(objectMapper.writeValueAsString(animalRequest))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.species").value(SPECIES));

        verify(animalService).createAnimal(animalRequest);
    }

    @Test
    @WithMockUser
    void getAnimal_CorrectData_Created() throws Exception {
        Animal animal = new Animal();
        animal.setSpecies(SPECIES);
        animal.setId(ID);

        when(animalService.getAnimalById(anyLong())).thenReturn(animal);

        mockMvc.perform(get("/api/animals/{id}", ID)
                .content(objectMapper.writeValueAsString(animal))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ID))
                .andExpect(jsonPath("$.species").value(SPECIES));

        verify(animalService).getAnimalById(1);
    }

    @Test
    @WithMockUser
    void getAllAnimals_CorrectData_Returned() throws Exception {
        List<Animal> animals = List.of(createNewAnimal("CAT"),
                createNewAnimal("DOG"));

        when(animalService.getAllAnimals()).thenReturn(animals);

        mockMvc.perform(get("/api/animals", ID)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].species").value("CAT"))
                .andExpect(jsonPath("$.[1].species").value("DOG"));

        verify(animalService).getAllAnimals();
    }

    private Animal createNewAnimal(String species) {
        var animal = new Animal();
        animal.setSpecies(species);
        return animal;
    }
}
