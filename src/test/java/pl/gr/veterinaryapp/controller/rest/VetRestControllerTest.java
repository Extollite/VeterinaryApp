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
import pl.gr.veterinaryapp.model.dto.VetRequestDto;
import pl.gr.veterinaryapp.model.entity.Vet;
import pl.gr.veterinaryapp.service.VetService;

import java.time.LocalTime;
import java.time.OffsetTime;
import java.time.ZoneOffset;
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

@WebMvcTest(controllers = VetRestController.class,
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
                        classes = {WebSecurityConfigurerAdapter.class, JwtAuthenticationFilter.class})
        },
        excludeAutoConfiguration = {WebSecurityConfig.class})
class VetRestControllerTest {

    private static final long ID = 1L;
    private static final String VET_NAME = "Kazimierz";
    private static final String VET_SURNAME = "Wieloglowy";
    private static final String IMAGE_URL = "url.jpg";

    @MockBean
    private VetService vetService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser
    void addVet_CorrectData_Created() throws Exception {
        OffsetTime workStartTime = OffsetTime.of(LocalTime.MIN, ZoneOffset.MIN);
        OffsetTime workEndTime = OffsetTime.of(LocalTime.MAX, ZoneOffset.MAX);
        var vetRequest = prepareVetRequest(VET_NAME, VET_SURNAME, IMAGE_URL, workStartTime, workEndTime);

        var vet = prepareVet(VET_NAME, VET_SURNAME, IMAGE_URL, workStartTime, workEndTime);

        when(vetService.createVet(any(VetRequestDto.class))).thenReturn(vet);

        mockMvc.perform(post("/api/vets")
                .with(csrf())
                .content(objectMapper.writeValueAsString(vetRequest))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(VET_NAME))
                .andExpect(jsonPath("$.workStartTime").value(workStartTime.toString()))
                .andExpect(jsonPath("$.workEndTime").value(workEndTime.toString()))
                .andExpect(jsonPath("$.photoUrl").value(IMAGE_URL));

        verify(vetService).createVet(vetRequest);
    }

    @Test
    @WithMockUser
    void getVet_CorrectData_Returned() throws Exception {
        OffsetTime workStartTime = OffsetTime.of(LocalTime.MIN, ZoneOffset.MIN);
        OffsetTime workEndTime = OffsetTime.of(LocalTime.MAX, ZoneOffset.MAX);

        var vet = prepareVet(VET_NAME, VET_SURNAME, IMAGE_URL, workStartTime, workEndTime);

        when(vetService.getVetById(anyLong())).thenReturn(vet);

        mockMvc.perform(get("/api/vets/{id}", ID)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(VET_NAME))
                .andExpect(jsonPath("$.surname").value(VET_SURNAME))
                .andExpect(jsonPath("$.photoUrl").value(IMAGE_URL))
                .andExpect(jsonPath("$.workStartTime").value(workStartTime.toString()))
                .andExpect(jsonPath("$.workEndTime").value(workEndTime.toString()));

        verify(vetService).getVetById(ID);
    }

    @Test
    @WithMockUser
    void getAllVets_CorrectData_Returned() throws Exception {
        OffsetTime workStartTime = OffsetTime.of(LocalTime.MIN, ZoneOffset.MIN);
        OffsetTime workEndTime = OffsetTime.of(LocalTime.MAX, ZoneOffset.MAX);

        var vet = prepareVet(VET_NAME, VET_SURNAME, IMAGE_URL, workStartTime, workEndTime);

        List<Vet> vets = List.of(vet, vet);

        when(vetService.getAllVets()).thenReturn(vets);

        mockMvc.perform(get("/api/vets")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].name").value(VET_NAME))
                .andExpect(jsonPath("$.[0].surname").value(VET_SURNAME))
                .andExpect(jsonPath("$.[0].photoUrl").value(IMAGE_URL))
                .andExpect(jsonPath("$.[0].workStartTime").value(workStartTime.toString()))
                .andExpect(jsonPath("$.[0].workEndTime").value(workEndTime.toString()))
                .andExpect(jsonPath("$.[1].name").value(VET_NAME))
                .andExpect(jsonPath("$.[1].surname").value(VET_SURNAME))
                .andExpect(jsonPath("$.[1].photoUrl").value(IMAGE_URL))
                .andExpect(jsonPath("$.[1].workStartTime").value(workStartTime.toString()))
                .andExpect(jsonPath("$.[1].workEndTime").value(workEndTime.toString()));

        verify(vetService).getAllVets();
    }

    private Vet prepareVet(String name, String surname, String photoUrl, OffsetTime workStartTime,
                           OffsetTime workEndTime) {
        var vet = new Vet();
        vet.setName(name);
        vet.setSurname(surname);
        vet.setPhotoUrl(photoUrl);
        vet.setWorkStartTime(workStartTime);
        vet.setWorkEndTime(workEndTime);
        vet.setId(ID);
        return vet;
    }

    private VetRequestDto prepareVetRequest(String name, String surname, String photoUrl, OffsetTime workStartTime,
                                            OffsetTime workEndTime) {
        var vetRequest = new VetRequestDto();
        vetRequest.setName(name);
        vetRequest.setSurname(surname);
        vetRequest.setPhotoUrl(photoUrl);
        vetRequest.setWorkStartTime(workStartTime);
        vetRequest.setWorkEndTime(workEndTime);
        return vetRequest;
    }
}
