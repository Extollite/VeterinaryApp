package pl.gr.veterinaryapp.controller.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import pl.gr.veterinaryapp.config.WebSecurityConfig;
import pl.gr.veterinaryapp.jwt.JwtAuthenticationFilter;
import pl.gr.veterinaryapp.mapper.PetMapper;
import pl.gr.veterinaryapp.model.dto.PetRequestDto;
import pl.gr.veterinaryapp.model.dto.PetResponseDto;
import pl.gr.veterinaryapp.model.entity.Animal;
import pl.gr.veterinaryapp.model.entity.Client;
import pl.gr.veterinaryapp.model.entity.Pet;
import pl.gr.veterinaryapp.service.PetService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PetRestController.class,
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
                        classes = {WebSecurityConfigurerAdapter.class, JwtAuthenticationFilter.class})
        },
        excludeAutoConfiguration = {WebSecurityConfig.class})
class PetRestControllerTest {

    private static final long ID = 1L;
    private static final String PET_NAME = "Puszek";

    @MockBean
    private PetService petService;

    @MockBean
    private PetMapper petMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser
    void createPet_CorrectData_Created() throws Exception {
        Animal animal = new Animal();
        animal.setId(ID);
        Client client = new Client();
        client.setId(ID);

        var petRequest = preparePetRequest(PET_NAME, LocalDate.of(1999, 10, 2), ID, ID);
        var pet = preparePet(PET_NAME, LocalDate.of(1999, 10, 2), animal, client);

        var petResponse = preparePetResponse(pet);

        when(petService.createPet(any(User.class), any(PetRequestDto.class))).thenReturn(pet);
        when(petMapper.map(any(Pet.class))).thenReturn(petResponse);

        var result = mockMvc.perform(post("/api/pets")
                .with(csrf())
                .content(objectMapper.writeValueAsString(petRequest))
                .contentType(MediaType.APPLICATION_JSON));

        verifyJson(result, petResponse);

        verify(petMapper).map(eq(pet));
        verify(petService).createPet(any(User.class), eq(petRequest));
    }

    @Test
    @WithMockUser
    void getPet_CorrectData_Returned() throws Exception {
        Animal animal = new Animal();
        animal.setId(ID);
        Client client = new Client();
        client.setId(ID);

        var pet = preparePet(PET_NAME, LocalDate.of(1999, 10, 2), animal, client);

        var petResponse = preparePetResponse(pet);

        when(petService.getPetById(any(User.class), anyLong())).thenReturn(pet);
        when(petMapper.map(any(Pet.class))).thenReturn(petResponse);

        var result = mockMvc.perform(get("/api/pets/{id}", ID)
                .contentType(MediaType.APPLICATION_JSON));

        verifyJson(result, petResponse);

        verify(petMapper).map(eq(pet));
        verify(petService).getPetById(any(User.class), eq(ID));
    }

    @Test
    @WithMockUser
    void deletePet_petDeleted_StatusOk() throws Exception {
        mockMvc.perform(delete("/api/pets/{id}", ID)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(petService).deletePet(ID);
        verifyNoInteractions(petMapper);
    }

    @Test
    @WithMockUser
    void getAllPets_NonEmptyList_Returned() throws Exception {
        Animal animal = new Animal();
        animal.setId(ID);
        Client client = new Client();
        client.setId(ID);

        List<PetResponseDto> petResponses = new ArrayList<>();
        List<Pet> pets = Collections.emptyList();

        var petResponse = preparePetResponse(
                preparePet(PET_NAME, LocalDate.of(1999, 10, 2), animal, client));

        for (int i = 0; i < 2; i++) {
            petResponses.add(petResponse);
        }

        when(petMapper.mapAsList(anyList())).thenReturn(petResponses);
        when(petService.getAllPets(any(User.class))).thenReturn(pets);

        var result = mockMvc.perform(get("/api/pets", ID)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        for (int i = 0; i < 2; i++) {
            verifyJsonList(result, petResponse, i);
        }

        verify(petService).getAllPets(any(User.class));
        verify(petMapper).mapAsList(eq(pets));
    }

    private void verifyJson(ResultActions result, PetResponseDto petResponse) throws Exception {
        result
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaTypes.HAL_JSON))
                .andExpect(jsonPath("$.name").value(PET_NAME))
                .andExpect(jsonPath("$.birthDate").value(petResponse.getBirthDate().toString()))
                .andExpect(jsonPath("$.animalId").value(ID))
                .andExpect(jsonPath("$.clientId").value(ID))
                .andExpect(jsonPath("$._links.animal.href", endsWith("/api/animals/" + ID)))
                .andExpect(jsonPath("$._links.client.href", endsWith("/api/clients/" + ID)));
    }

    private void verifyJsonList(ResultActions result, PetResponseDto petResponse, int index) throws Exception {
        String i = "[" + index + "]";
        result
                .andExpect(content().contentType(MediaTypes.HAL_JSON))
                .andExpect(jsonPath("$." + i + ".name").value(PET_NAME))
                .andExpect(jsonPath("$." + i + ".birthDate").value(petResponse.getBirthDate().toString()))
                .andExpect(jsonPath("$." + i + ".animalId").value(ID))
                .andExpect(jsonPath("$." + i + ".clientId").value(ID))
                .andExpect(jsonPath("$." + i + ".links" + "[" + 0 + "].rel", is("animal")))
                .andExpect(jsonPath("$." + i + ".links" + "[" + 0 + "].href", endsWith("/api/animals/" + ID)))
                .andExpect(jsonPath("$." + i + ".links" + "[" + 1 + "].rel", is("client")))
                .andExpect(jsonPath("$." + i + ".links" + "[" + 1 + "].href", endsWith("/api/clients/" + ID)))
                .andExpect(jsonPath("$." + i + ".links" + "[" + 2 + "].rel", is("self")))
                .andExpect(jsonPath("$." + i + ".links" + "[" + 2 + "].href", endsWith("/api/pets/" + ID)));
    }

    private Pet preparePet(String name, LocalDate birthDate, Animal animal, Client client) {
        var pet = new Pet();
        pet.setName(name);
        pet.setBirthDate(birthDate);
        pet.setAnimal(animal);
        pet.setClient(client);
        return pet;
    }

    private PetResponseDto preparePetResponse(Pet pet) {
        var petResponse = new PetResponseDto();
        petResponse.setName(pet.getName());
        petResponse.setId(ID);
        petResponse.setAnimalId(ID);
        petResponse.setClientId(ID);
        petResponse.setBirthDate(pet.getBirthDate());
        return petResponse;
    }

    private PetRequestDto preparePetRequest(String name, LocalDate birthDate, Long animalId, Long clientId) {
        var petRequest = new PetRequestDto();
        petRequest.setName(name);
        petRequest.setBirthDate(birthDate);
        petRequest.setAnimalId(animalId);
        petRequest.setClientId(clientId);
        return petRequest;
    }
}
