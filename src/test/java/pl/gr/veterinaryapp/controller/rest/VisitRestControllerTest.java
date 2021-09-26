package pl.gr.veterinaryapp.controller.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import pl.gr.veterinaryapp.common.OperationType;
import pl.gr.veterinaryapp.common.VisitStatus;
import pl.gr.veterinaryapp.common.VisitType;
import pl.gr.veterinaryapp.config.WebSecurityConfig;
import pl.gr.veterinaryapp.jwt.JwtAuthenticationFilter;
import pl.gr.veterinaryapp.mapper.VisitMapper;
import pl.gr.veterinaryapp.model.dto.AvailableVisitDto;
import pl.gr.veterinaryapp.model.dto.VisitEditDto;
import pl.gr.veterinaryapp.model.dto.VisitRequestDto;
import pl.gr.veterinaryapp.model.dto.VisitResponseDto;
import pl.gr.veterinaryapp.model.entity.Visit;
import pl.gr.veterinaryapp.service.VisitService;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = VisitRestController.class,
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
                        classes = {WebSecurityConfigurerAdapter.class, JwtAuthenticationFilter.class})
        },
        excludeAutoConfiguration = {WebSecurityConfig.class})
class VisitRestControllerTest {

    private static final long ID = 1L;

    @MockBean
    private VisitService visitService;

    @MockBean
    private VisitMapper visitMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");

    private static Stream<Arguments> visitResponseDataProvider() {
        return Stream.of(
                Arguments.of(ID, 2, 2, 1, OffsetDateTime.of(LocalDateTime.of(2020, 11, 3, 5, 30), ZoneOffset.UTC),
                        Duration.ofMinutes(30), BigDecimal.valueOf(100), VisitType.STATIONARY
                        , OperationType.OPERATION, VisitStatus.SCHEDULED)
        );
    }

    @ParameterizedTest
    @MethodSource("visitResponseDataProvider")
    @WithMockUser
    void getVisit_CorrectData_Returned(long id, long vetId, long petId, long treatmentRoomId,
                                       OffsetDateTime startDateTime, Duration duration,
                                       BigDecimal price, VisitType visitType, OperationType operationType,
                                       VisitStatus visitStatus) throws Exception {
        var visitResponse = VisitResponseDto.builder()
                .id(id)
                .vetId(vetId)
                .petId(petId)
                .treatmentRoomId(treatmentRoomId)
                .startDateTime(startDateTime)
                .duration(duration)
                .price(price)
                .visitType(visitType)
                .operationType(operationType)
                .visitStatus(visitStatus)
                .build();

        Visit visit = new Visit();

        when(visitService.getVisitById(any(User.class), anyLong())).thenReturn(visit);
        when(visitMapper.map(any(Visit.class))).thenReturn(visitResponse);

        var resultActions = mockMvc.perform(get("/api/visits/{id}", id)
                .contentType(MediaType.APPLICATION_JSON));

        verifyJson(resultActions, visitResponse);

        verify(visitService).getVisitById(any(User.class), eq(id));
        verify(visitMapper).map(eq(visit));
    }

    @ParameterizedTest
    @MethodSource("visitResponseDataProvider")
    @WithMockUser
    void createVisit_CreateData_Created(long id, long vetId, long petId, long treatmentRoomId,
                                        OffsetDateTime startDateTime, Duration duration,
                                        BigDecimal price, VisitType visitType, OperationType operationType,
                                        VisitStatus visitStatus) throws Exception {
        var visitRequest = VisitRequestDto.builder()
                .visitType(visitType)
                .operationType(operationType)
                .duration(duration)
                .vetId(vetId)
                .petId(petId)
                .price(price)
                .startDateTime(startDateTime)
                .build();

        var visitResponse = VisitResponseDto.builder()
                .id(id)
                .vetId(vetId)
                .petId(petId)
                .treatmentRoomId(treatmentRoomId)
                .startDateTime(startDateTime)
                .duration(duration)
                .price(price)
                .visitType(visitType)
                .operationType(operationType)
                .visitStatus(visitStatus)
                .build();

        var visit = new Visit();

        when(visitService.createVisit(any(User.class), any(VisitRequestDto.class))).thenReturn(visit);
        when(visitMapper.map(any(Visit.class))).thenReturn(visitResponse);

        var resultActions = mockMvc.perform(post("/api/visits")
                .with(csrf())
                .content(objectMapper.writeValueAsString(visitRequest))
                .contentType(MediaType.APPLICATION_JSON));

        verifyJson(resultActions, visitResponse);

        verify(visitService).createVisit(any(User.class), eq(visitRequest));
        verify(visitMapper).map(eq(visit));
    }

    @ParameterizedTest
    @MethodSource("visitResponseDataProvider")
    @WithMockUser
    void getAllVisits_VisitsExist_Returned(long id, long vetId, long petId, long treatmentRoomId,
                                           OffsetDateTime startDateTime, Duration duration,
                                           BigDecimal price, VisitType visitType, OperationType operationType,
                                           VisitStatus visitStatus) throws Exception {

        var visitResponse = VisitResponseDto.builder()
                .id(id)
                .vetId(vetId)
                .petId(petId)
                .treatmentRoomId(treatmentRoomId)
                .startDateTime(startDateTime)
                .duration(duration)
                .price(price)
                .visitType(visitType)
                .operationType(operationType)
                .visitStatus(visitStatus)
                .build();

        List<VisitResponseDto> visits = List.of(visitResponse, visitResponse);

        when(visitService.getAllVisits(any(User.class))).thenReturn(Collections.emptyList());
        when(visitMapper.mapAsList(anyList())).thenReturn(visits);

        var resultActions = mockMvc.perform(get("/api/visits")
                .contentType(MediaType.APPLICATION_JSON));

        for (int i = 0; i < 2; i++) {
            verifyJsonList(resultActions, visitResponse, i);
        }

        verify(visitService).getAllVisits(any(User.class));
        verify(visitMapper).mapAsList(Collections.emptyList());
    }

    @Test
    @WithMockUser
    void finalizeVisit_CorrectData_Returned() throws Exception {
        var visitEditDto = new VisitEditDto();

        var visit = new Visit();

        var visitResponse = VisitResponseDto.builder()
                .id(ID)
                .vetId(1)
                .petId(1)
                .treatmentRoomId(1)
                .startDateTime(OffsetDateTime.of(LocalDateTime.of(2020, 11, 3, 5, 30), ZoneOffset.UTC))
                .duration(Duration.ofMinutes(30))
                .price(BigDecimal.valueOf(100))
                .visitType(VisitType.STATIONARY)
                .operationType(OperationType.OPERATION)
                .visitStatus(VisitStatus.SCHEDULED)
                .build();

        when(visitService.finalizeVisit(any(VisitEditDto.class))).thenReturn(visit);
        when(visitMapper.map(any(Visit.class))).thenReturn(visitResponse);

        var resultActions = mockMvc.perform(patch("/api/visits")
                .with(csrf())
                .content(objectMapper.writeValueAsString(visitEditDto))
                .contentType(MediaType.APPLICATION_JSON));

        verifyJson(resultActions, visitResponse);

        verify(visitService).finalizeVisit(eq(visitEditDto));
        verify(visitMapper).map(eq(visit));
    }

    @Test
    @WithMockUser
    void getAvailableVisits_NonEmptyVetIds_Returned() throws Exception {
        var availableVisitDto = new AvailableVisitDto();
        availableVisitDto.setVetIds(List.of(1L));
        OffsetDateTime offsetDateTime = OffsetDateTime.of(LocalDateTime.of(2020, 11, 3, 5, 30), ZoneOffset.UTC);
        availableVisitDto.setStartDateTime(offsetDateTime);

        List<AvailableVisitDto> availableVisits = new ArrayList<>();

        availableVisits.add(availableVisitDto);

        when(visitService.getAvailableVisits(any(OffsetDateTime.class), any(OffsetDateTime.class), anyCollection()))
                .thenReturn(availableVisits);

        mockMvc.perform(get("/api/visits/available")
                .param("startDateTime", "2021-04-14T01:00:00+00:00")
                .param("endDateTime", "2021-04-17T15:30:00+00:00")
                .param("vetIds", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].vetIds[0]").value(1))
                .andExpect(jsonPath("$.[0].startDateTime").value(formatter.format(offsetDateTime)))
                .andExpect(jsonPath("$.[0].links[0].rel", is("vet")))
                .andExpect(jsonPath("$.[0].links[0].href", endsWith("/api/vets/1")));
    }

    @Test
    @WithMockUser
    void getAvailableVisits_EmptyVetIds_Returned() throws Exception {
        var availableVisitDto = new AvailableVisitDto();
        availableVisitDto.setVetIds(List.of(1L));
        OffsetDateTime offsetDateTime = OffsetDateTime.of(LocalDateTime.of(2020, 11, 3, 5, 30), ZoneOffset.UTC);
        availableVisitDto.setStartDateTime(offsetDateTime);

        List<AvailableVisitDto> availableVisits = List.of(availableVisitDto);

        when(visitService.getAvailableVisits(any(OffsetDateTime.class), any(OffsetDateTime.class), anyCollection()))
                .thenReturn(availableVisits);

        mockMvc.perform(get("/api/visits/available")
                .param("startDateTime", "2021-04-14T01:00:00+00:00")
                .param("endDateTime", "2021-04-17T15:30:00+00:00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].vetIds[0]").value(1))
                .andExpect(jsonPath("$.[0].startDateTime").value(formatter.format(offsetDateTime)))
                .andExpect(jsonPath("$.[0].links[0].rel", is("vet")))
                .andExpect(jsonPath("$.[0].links[0].href", endsWith("/api/vets/1")));
    }

    private void verifyJson(ResultActions resultAction, VisitResponseDto result) throws Exception {
        resultAction
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(result.getId()))
                .andExpect(jsonPath("$.petId").value(result.getPetId()))
                .andExpect(jsonPath("$.treatmentRoomId").value(result.getTreatmentRoomId()))
                .andExpect(jsonPath("$.startDateTime").value(formatter.format(result.getStartDateTime())))
                .andExpect(jsonPath("$.duration").value(result.getDuration().toString()))
                .andExpect(jsonPath("$.price").value(result.getPrice().toString()))
                .andExpect(jsonPath("$.visitType").value(result.getVisitType().toString()))
                .andExpect(jsonPath("$.operationType").value(result.getOperationType().toString()))
                .andExpect(jsonPath("$.visitStatus").value(result.getVisitStatus().toString()))
                .andExpect(jsonPath("$._links.vet.href", endsWith("/api/vets/" + result.getVetId())))
                .andExpect(jsonPath("$._links.pet.href", endsWith("/api/pets/" + result.getPetId())));
    }

    private void verifyJsonList(ResultActions resultAction, VisitResponseDto result, int index) throws Exception {
        String i = "[" + index + "]";

        resultAction
                .andExpect(status().isOk())
                .andExpect(jsonPath("$." + i + ".id").value(result.getId()))
                .andExpect(jsonPath("$." + i + ".petId").value(result.getPetId()))
                .andExpect(jsonPath("$." + i + ".treatmentRoomId").value(result.getTreatmentRoomId()))
                .andExpect(jsonPath("$." + i + ".startDateTime").value(formatter.format(result.getStartDateTime())))
                .andExpect(jsonPath("$." + i + ".duration").value(result.getDuration().toString()))
                .andExpect(jsonPath("$." + i + ".price").value(result.getPrice().toString()))
                .andExpect(jsonPath("$." + i + ".visitType").value(result.getVisitType().toString()))
                .andExpect(jsonPath("$." + i + ".operationType").value(result.getOperationType().toString()))
                .andExpect(jsonPath("$." + i + ".visitStatus").value(result.getVisitStatus().toString()))
                .andExpect(jsonPath("$." + i + ".links" + "[" + 0 + "].rel", is("vet")))
                .andExpect(jsonPath("$." + i + ".links" + "[" + 1 + "].rel", is("pet")))
                .andExpect(jsonPath("$." + i + ".links" + "[" + 2 + "].rel", is("self")))
                .andExpect(jsonPath("$." + i + ".links" + "[" + 0 + "].href", endsWith("/api/vets/" + result.getVetId())))
                .andExpect(jsonPath("$." + i + ".links" + "[" + 1 + "].href", endsWith("/api/pets/" + result.getPetId())))
                .andExpect(jsonPath("$." + i + ".links" + "[" + 2 + "].href", endsWith("/api/visits/" + result.getId())));
    }
}