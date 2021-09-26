package pl.gr.veterinaryapp.service;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.User;
import pl.gr.veterinaryapp.common.OperationType;
import pl.gr.veterinaryapp.common.VisitStatus;
import pl.gr.veterinaryapp.common.VisitType;
import pl.gr.veterinaryapp.exception.IncorrectDataException;
import pl.gr.veterinaryapp.exception.ResourceNotFoundException;
import pl.gr.veterinaryapp.model.dto.AvailableVisitDto;
import pl.gr.veterinaryapp.model.dto.VisitEditDto;
import pl.gr.veterinaryapp.model.dto.VisitRequestDto;
import pl.gr.veterinaryapp.model.entity.Client;
import pl.gr.veterinaryapp.model.entity.Pet;
import pl.gr.veterinaryapp.model.entity.TreatmentRoom;
import pl.gr.veterinaryapp.model.entity.Vet;
import pl.gr.veterinaryapp.model.entity.Visit;
import pl.gr.veterinaryapp.repository.PetRepository;
import pl.gr.veterinaryapp.repository.TreatmentRoomRepository;
import pl.gr.veterinaryapp.repository.VetRepository;
import pl.gr.veterinaryapp.repository.VisitRepository;
import pl.gr.veterinaryapp.service.impl.VisitServiceImpl;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyIterable;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VisitServiceTest {

    private static final long VISIT_ID = 1L;
    private static final long PET_ID = 1L;
    private static final long VET_ID = 1L;
    private static final String VISIT_DESCRIPTION = "description";
    private static final User USER = new User("name", "passwd", Collections.emptySet());

    @Mock
    private VisitRepository visitRepository;
    @Mock
    private PetRepository petRepository;
    @Mock
    private VetRepository vetRepository;
    @Mock
    private TreatmentRoomRepository treatmentRoomRepository;
    @Mock
    private Clock clock;
    @InjectMocks
    private VisitServiceImpl visitService;

    private static Clock fixedClock;

    @BeforeAll
    static void setupTest() {
        fixedClock = Clock.fixed(Instant.now(), ZoneOffset.UTC);
    }

    @Test
    void deleteVisitById_WithCorrectId_Deleted() {
        Visit visit = new Visit();

        when(visitRepository.findById(anyLong())).thenReturn(Optional.of(visit));

        visitService.deleteVisit(VISIT_ID);

        verify(visitRepository).findById(eq(VISIT_ID));
        verify(visitRepository).delete(eq(visit));
        verifyNoInteractions(petRepository, vetRepository, treatmentRoomRepository);
    }

    @Test
    void deleteVisit_VisitNotFound_ThrownException() {
        when(visitRepository.findById(anyLong())).thenReturn(Optional.empty());

        ResourceNotFoundException thrown =
                catchThrowableOfType(() -> visitService.deleteVisit(VISIT_ID), ResourceNotFoundException.class);

        assertThat(thrown)
                .hasMessage("Wrong id.");

        verify(visitRepository).findById(eq(VISIT_ID));
        verifyNoInteractions(petRepository, vetRepository, treatmentRoomRepository);
    }

    @Test
    void getVisitById_WithCorrectId_Returned() {
        Pet pet = new Pet();
        pet.setClient(new Client());
        Visit visit = new Visit();
        visit.setPet(pet);

        when(visitRepository.findById(anyLong())).thenReturn(Optional.of(visit));

        var result = visitService.getVisitById(USER, VISIT_ID);

        assertThat(result)
                .isNotNull()
                .isEqualTo(visit);

        verify(visitRepository).findById(eq(VISIT_ID));
        verifyNoInteractions(petRepository, vetRepository, treatmentRoomRepository);
    }

    @Test
    void getVisitById_WithWrongId_ExceptionThrown() {
        when(visitRepository.findById(anyLong())).thenReturn(Optional.empty());

        ResourceNotFoundException thrown =
                catchThrowableOfType(() -> visitService.getVisitById(USER, VISIT_ID), ResourceNotFoundException.class);

        assertThat(thrown)
                .hasMessage("Wrong id.");

        verify(visitRepository).findById(eq(VISIT_ID));
        verifyNoInteractions(petRepository, vetRepository, treatmentRoomRepository);
    }

    @Test
    void getAllVisits_ReturnVisitsList_Returned() {
        List<Visit> visits = emptyList();

        when(visitRepository.findAll()).thenReturn(visits);

        var result = visitService.getAllVisits(USER);

        assertThat(result)
                .isNotNull()
                .isEqualTo(visits);

        verify(visitRepository).findAll();
        verifyNoInteractions(petRepository, vetRepository, treatmentRoomRepository);
    }

    @ParameterizedTest
    @CsvFileSource(resources = "/visitCorrectData.csv", numLinesToSkip = 1)
    void createVisit_WithCorrectData_Created(Duration timeOffsetFromNow) {
        OffsetDateTime startDateTime = OffsetDateTime.now(fixedClock).plus(timeOffsetFromNow);
        Duration duration = Duration.ofMinutes(30);

        VisitRequestDto request = prepareVisitRequestDto(startDateTime, duration);

        var pet = new Pet();
        var vet = new Vet();
        vet.setWorkStartTime(OffsetTime.of(LocalTime.MIN, ZoneOffset.UTC));
        vet.setWorkEndTime(OffsetTime.of(LocalTime.MAX, ZoneOffset.UTC));
        var treatmentRoom = new TreatmentRoom();

        when(vetRepository.findById(anyLong())).thenReturn(Optional.of(vet));
        when(clock.instant()).thenReturn(fixedClock.instant());
        when(clock.getZone()).thenReturn(fixedClock.getZone());
        when(visitRepository.findAllOverlapping(anyLong(), any(OffsetDateTime.class), any(OffsetDateTime.class)))
                .thenReturn(emptyList());
        when(petRepository.findById(anyLong())).thenReturn(Optional.of(pet));
        when(visitRepository.findAllOverlappingInDateRange(any(OffsetDateTime.class), any(OffsetDateTime.class)))
                .thenReturn(emptyList());
        when(treatmentRoomRepository.findAll()).thenReturn(singletonList(treatmentRoom));
        when(visitRepository.save(any(Visit.class)))
                .thenAnswer(invocation -> {
                    Visit visit = invocation.getArgument(0);
                    visit.setId(VISIT_ID);
                    return visit;
                });

        var result = visitService.createVisit(USER, request);

        assertThat(result)
                .isNotNull()
                .matches(visit -> Objects.equals(visit.getId(), VISIT_ID))
                .matches(visit -> Objects.equals(visit.getPet(), pet))
                .matches(visit -> Objects.equals(visit.getVet(), vet))
                .matches(visit -> Objects.equals(visit.getTreatmentRoom(), treatmentRoom))
                .matches(visit -> Objects.equals(visit.getStartDateTime(), startDateTime))
                .matches(visit -> Objects.equals(visit.getDuration(), duration))
                .matches(visit -> Objects.equals(visit.getPrice(), request.getPrice()))
                .matches(visit -> Objects.equals(visit.getOperationType(), request.getOperationType()))
                .matches(visit -> Objects.equals(visit.getVisitType(), request.getVisitType()))
                .matches(visit -> Objects.equals(visit.getVisitStatus(), VisitStatus.SCHEDULED));

        verify(vetRepository).findById(eq(VET_ID));
        verify(petRepository).findById(eq(PET_ID));
        verify(visitRepository).findAllOverlapping(eq(VET_ID), eq(request.getStartDateTime()),
                eq(request.getStartDateTime().plus(request.getDuration())));
        verify(visitRepository).findAllOverlappingInDateRange(eq(request.getStartDateTime()),
                eq(request.getStartDateTime().plus(request.getDuration())));
        verify(visitRepository).save(any(Visit.class));
        verify(treatmentRoomRepository).findAll();
    }

    @ParameterizedTest
    @CsvFileSource(resources = "/visitIncorrectData.csv", numLinesToSkip = 1)
    void createVisit_WithIncorrectData_ExceptionThrown(Duration timeOffsetFromNow, String expectedExceptionMessage) {
        OffsetDateTime startDateTime = OffsetDateTime.now(fixedClock).plus(timeOffsetFromNow);
        Duration duration = Duration.ofMinutes(30);

        VisitRequestDto request = prepareVisitRequestDto(startDateTime, duration);

        var vet = new Vet();

        when(vetRepository.findById(anyLong())).thenReturn(Optional.of(vet));
        when(clock.instant()).thenReturn(fixedClock.instant());
        when(clock.getZone()).thenReturn(fixedClock.getZone());

        IncorrectDataException thrown =
                catchThrowableOfType(() -> visitService.createVisit(USER, request), IncorrectDataException.class);

        assertThat(thrown)
                .hasMessage(expectedExceptionMessage);

        verify(vetRepository).findById(eq(VET_ID));
        verifyNoInteractions(petRepository, treatmentRoomRepository, visitRepository);
    }

    @Test
    void createVisit_WithOverlappingDate_ExceptionThrown() {
        List<Visit> visits = List.of(new Visit());
        Vet vet = new Vet();
        vet.setId(VET_ID);

        OffsetDateTime startDateTime = OffsetDateTime.now(fixedClock).plusDays(1);
        Duration duration = Duration.ofMinutes(30);

        VisitRequestDto request = prepareVisitRequestDto(startDateTime, duration);

        when(vetRepository.findById(anyLong())).thenReturn(Optional.of(vet));
        when(clock.instant()).thenReturn(fixedClock.instant());
        when(clock.getZone()).thenReturn(fixedClock.getZone());
        when(visitRepository.findAllOverlapping(anyLong(), any(OffsetDateTime.class), any(OffsetDateTime.class)))
                .thenReturn(visits);

        IncorrectDataException thrown =
                catchThrowableOfType(() -> visitService.createVisit(USER, request), IncorrectDataException.class);

        assertThat(thrown)
                .hasMessage("This date is not available.");

        verify(vetRepository).findById(eq(VET_ID));
        verify(visitRepository).findAllOverlapping(eq(VET_ID), eq(request.getStartDateTime()),
                eq(request.getStartDateTime().plus(request.getDuration())));
        verifyNoInteractions(petRepository, treatmentRoomRepository);
    }

    @Test
    void createVisit_WithWrongAnimalId_ExceptionThrown() {
        List<Visit> visits = emptyList();
        Vet vet = new Vet();
        vet.setId(VET_ID);

        OffsetDateTime startDateTime = OffsetDateTime.now(fixedClock).plusDays(1);
        Duration duration = Duration.ofMinutes(30);

        VisitRequestDto request = prepareVisitRequestDto(startDateTime, duration);

        when(vetRepository.findById(anyLong())).thenReturn(Optional.of(vet));
        when(clock.instant()).thenReturn(fixedClock.instant());
        when(clock.getZone()).thenReturn(fixedClock.getZone());
        when(visitRepository.findAllOverlapping(anyLong(), any(OffsetDateTime.class), any(OffsetDateTime.class)))
                .thenReturn(visits);
        when(petRepository.findById(anyLong())).thenReturn(Optional.empty());

        IncorrectDataException thrown =
                catchThrowableOfType(() -> visitService.createVisit(USER, request), IncorrectDataException.class);

        assertThat(thrown)
                .hasMessage("Wrong pet id.");

        verify(vetRepository).findById(eq(VET_ID));
        verify(visitRepository).findAllOverlapping(eq(VET_ID), eq(request.getStartDateTime()),
                eq(request.getStartDateTime().plus(request.getDuration())));
        verify(petRepository).findById(eq(PET_ID));
        verifyNoInteractions(treatmentRoomRepository);
    }

    @Test
    void createVisit_WithWrongVetId_ExceptionThrown() {
        OffsetDateTime startDateTime = OffsetDateTime.now(fixedClock).plusDays(1);
        Duration duration = Duration.ofMinutes(30);

        VisitRequestDto request = prepareVisitRequestDto(startDateTime, duration);

        when(vetRepository.findById(anyLong())).thenReturn(Optional.empty());

        IncorrectDataException thrown =
                catchThrowableOfType(() -> visitService.createVisit(USER, request), IncorrectDataException.class);

        assertThat(thrown)
                .hasMessage("Wrong vet id.");

        verify(vetRepository).findById(eq(VET_ID));
        verifyNoInteractions(petRepository, visitRepository, treatmentRoomRepository);
    }

    @Test
    void createVisit_NoFreeRooms_ExceptionThrown() {
        OffsetDateTime startDateTime = OffsetDateTime.now(fixedClock).plusDays(1);
        Duration duration = Duration.ofMinutes(30);

        VisitRequestDto request = prepareVisitRequestDto(startDateTime, duration);

        var pet = new Pet();
        var vet = new Vet();

        when(vetRepository.findById(anyLong())).thenReturn(Optional.of(vet));
        when(clock.instant()).thenReturn(fixedClock.instant());
        when(clock.getZone()).thenReturn(fixedClock.getZone());
        when(visitRepository.findAllOverlapping(anyLong(), any(OffsetDateTime.class), any(OffsetDateTime.class)))
                .thenReturn(emptyList());
        when(petRepository.findById(anyLong())).thenReturn(Optional.of(pet));
        when(visitRepository.findAllOverlappingInDateRange(any(OffsetDateTime.class), any(OffsetDateTime.class)))
                .thenReturn(emptyList());
        when(treatmentRoomRepository.findAll()).thenReturn(emptyList());

        IncorrectDataException thrown =
                catchThrowableOfType(() -> visitService.createVisit(USER, request), IncorrectDataException.class);

        assertThat(thrown)
                .hasMessage("There is no free treatment room.");

        verify(vetRepository).findById(eq(VET_ID));
        verify(petRepository).findById(eq(PET_ID));
        verify(visitRepository).findAllOverlapping(eq(VET_ID), eq(request.getStartDateTime()),
                eq(request.getStartDateTime().plus(request.getDuration())));
        verify(visitRepository).findAllOverlappingInDateRange(eq(request.getStartDateTime()),
                eq(request.getStartDateTime().plus(request.getDuration())));
        verify(treatmentRoomRepository).findAll();
    }

    private VisitRequestDto prepareVisitRequestDto
            (OffsetDateTime startDateTime, Duration duration) {
        VisitRequestDto request = new VisitRequestDto();
        request.setPetId(PET_ID);
        request.setStartDateTime(startDateTime);
        request.setDuration(duration);
        request.setOperationType(OperationType.OPERATION);
        request.setVisitType(VisitType.REMOTE);
        request.setPrice(BigDecimal.ONE);
        request.setVetId(VET_ID);
        return request;
    }

    @Test
    void checkExpiredVisits_WithCorrectData_StatusUpdated() {
        var visit = new Visit();
        visit.setVisitStatus(VisitStatus.SCHEDULED);

        when(clock.instant()).thenReturn(fixedClock.instant());
        when(clock.getZone()).thenReturn(fixedClock.getZone());
        when(visitRepository
                .findAllByEndDateAndEndTimeBeforeAndVisitStatus(any(OffsetDateTime.class), any(VisitStatus.class)))
                .thenReturn(singletonList(visit));

        visitService.checkExpiredVisits();

        assertThat(visit.getVisitStatus())
                .isEqualTo(VisitStatus.EXPIRED);
        verify(visitRepository)
                .findAllByEndDateAndEndTimeBeforeAndVisitStatus(any(OffsetDateTime.class), eq(VisitStatus.SCHEDULED));
        verifyNoInteractions(petRepository, vetRepository, treatmentRoomRepository);
    }

    private static Stream<Arguments> finalizeVisitDataProvider() {
        return Stream.of(
                Arguments.of(VisitStatus.SCHEDULED, VisitStatus.FINISHED, VisitStatus.FINISHED),
                Arguments.of(VisitStatus.EXPIRED, VisitStatus.DID_NOT_APPEAR, VisitStatus.DID_NOT_APPEAR),
                Arguments.of(VisitStatus.SCHEDULED, VisitStatus.EXPIRED, VisitStatus.SCHEDULED)
        );
    }

    @ParameterizedTest
    @MethodSource("finalizeVisitDataProvider")
    void finalizeVisit_WithCorrectData_Returned(VisitStatus currentStatus, VisitStatus changedStatus,
                                                VisitStatus expectedStatus) {
        Visit visit = new Visit();
        visit.setVisitStatus(currentStatus);
        visit.setVisitDescription("other desc");
        var visitEditDto = new VisitEditDto();
        visitEditDto.setId(VISIT_ID);
        visitEditDto.setVisitStatus(changedStatus);
        visitEditDto.setDescription(VISIT_DESCRIPTION);

        when(visitRepository.findById(anyLong())).thenReturn(Optional.of(visit));

        var result = visitService.finalizeVisit(visitEditDto);

        assertThat(result)
                .isNotNull()
                .matches(visitDto -> Objects.equals(visitDto.getVisitStatus(), expectedStatus))
                .matches(visitDto -> Objects.equals(visitDto.getVisitDescription(), VISIT_DESCRIPTION));

        verify(visitRepository).findById(eq(VISIT_ID));
        verifyNoInteractions(petRepository, vetRepository, treatmentRoomRepository);
    }

    @Test
    void finalizeVisit_WithWrongId_ExceptionThrown() {
        var visitEditDto = new VisitEditDto();
        visitEditDto.setId(VISIT_ID);

        when(visitRepository.findById(anyLong())).thenReturn(Optional.empty());

        ResourceNotFoundException thrown =
                catchThrowableOfType(() -> visitService.finalizeVisit(visitEditDto), ResourceNotFoundException.class);

        assertThat(thrown)
                .hasMessage("Wrong id.");

        verify(visitRepository).findById(eq(VISIT_ID));
        verifyNoInteractions(petRepository, vetRepository, treatmentRoomRepository);
    }

    @Test
    void getAvailableVisits_WithVetIdsProvided_Returned() {
        final long vet1stId = 1L;
        final long vet2ndId = 2L;
        var vetIds = Set.of(vet1stId, vet2ndId);
        final var startDateTime = OffsetDateTime.now(fixedClock);
        final var endDateTime = startDateTime.plusMinutes(45);

        var vet1st = new Vet();
        vet1st.setId(vet1stId);
        vet1st.setWorkStartTime(OffsetTime.now(fixedClock));
        vet1st.setWorkEndTime(OffsetTime.now(fixedClock).plus(Duration.ofMinutes(45)));

        var vet2nd = new Vet();
        vet2nd.setId(vet2ndId);
        vet2nd.setWorkStartTime(OffsetTime.now(fixedClock).plus(Duration.ofMinutes(30)));
        vet2nd.setWorkEndTime(OffsetTime.now(fixedClock).plus(Duration.ofMinutes(45)));

        var visit = new Visit();
        visit.setId(VISIT_ID);
        visit.setStartDateTime(startDateTime.plusMinutes(15));
        visit.setDuration(Duration.ofMinutes(15));
        visit.setVet(vet1st);

        when(vetRepository.findAllById(anyIterable())).thenReturn(List.of(vet1st, vet2nd));
        when(visitRepository.findAllInDateTimeRangeAndVetIdIn(
                any(OffsetDateTime.class), any(OffsetDateTime.class), anyCollection()))
                .thenReturn(List.of(visit));

        List<AvailableVisitDto> result = visitService.getAvailableVisits(startDateTime, endDateTime, vetIds);

        assertThat(result)
                .hasSize(2)
                .extracting("vetIds", "startDateTime")
                .containsExactly(tuple(List.of(vet1stId), startDateTime),
                        tuple(List.of(vet1stId, vet2ndId), startDateTime.plusMinutes(30)));

        verify(vetRepository).findAllById(eq(vetIds));
        verify(visitRepository).findAllInDateTimeRangeAndVetIdIn(eq(startDateTime), eq(endDateTime), eq(vetIds));
        verifyNoInteractions(petRepository, treatmentRoomRepository);
    }

    @Test
    void getAvailableVisits_WithEmptyVetIds_Returned() {
        final long vet1stId = 1L;
        final long vet2ndId = 2L;
        var vetIds = Set.of(vet1stId, vet2ndId);
        final var startDateTime = OffsetDateTime.now(fixedClock);
        final var endDateTime = startDateTime.plusMinutes(45);

        var vet1st = new Vet();
        vet1st.setId(vet1stId);
        vet1st.setWorkStartTime(OffsetTime.now(fixedClock));
        vet1st.setWorkEndTime(OffsetTime.now(fixedClock).plus(Duration.ofMinutes(45)));

        var vet2nd = new Vet();
        vet2nd.setId(vet2ndId);
        vet2nd.setWorkStartTime(OffsetTime.now(fixedClock).plus(Duration.ofMinutes(30)));
        vet2nd.setWorkEndTime(OffsetTime.now(fixedClock).plus(Duration.ofMinutes(45)));

        var visit = new Visit();
        visit.setId(VISIT_ID);
        visit.setStartDateTime(startDateTime.plusMinutes(15));
        visit.setDuration(Duration.ofMinutes(15));
        visit.setVet(vet1st);

        when(vetRepository.findAll()).thenReturn(List.of(vet1st, vet2nd));
        when(visitRepository.findAllInDateTimeRangeAndVetIdIn(
                any(OffsetDateTime.class), any(OffsetDateTime.class), anyCollection()))
                .thenReturn(List.of(visit));

        List<AvailableVisitDto> result = visitService.getAvailableVisits(startDateTime, endDateTime, emptySet());

        assertThat(result)
                .hasSize(2)
                .extracting("vetIds", "startDateTime")
                .containsExactly(tuple(List.of(vet1stId), startDateTime),
                        tuple(List.of(vet1stId, vet2ndId), startDateTime.plusMinutes(30)));

        verify(vetRepository).findAll();
        verify(visitRepository).findAllInDateTimeRangeAndVetIdIn(eq(startDateTime), eq(endDateTime), eq(vetIds));
        verifyNoInteractions(petRepository, treatmentRoomRepository);
    }

    @Test
    void createVisit_WithWrongVetWorkTime_ExceptionThrown() {
        OffsetDateTime startDateTime = OffsetDateTime.now(fixedClock).plusDays(1);
        Duration duration = Duration.ofMinutes(30);

        VisitRequestDto request = prepareVisitRequestDto(startDateTime, duration);

        var treatmentRoom = new TreatmentRoom();
        var pet = new Pet();
        var vet = new Vet();
        vet.setWorkStartTime(OffsetTime.of(13,0,0,0,ZoneOffset.UTC));
        vet.setWorkEndTime(OffsetTime.of(14,0,0,0,ZoneOffset.UTC));


        when(vetRepository.findById(anyLong())).thenReturn(Optional.of(vet));
        when(clock.instant()).thenReturn(fixedClock.instant());
        when(clock.getZone()).thenReturn(fixedClock.getZone());
        when(visitRepository.findAllOverlapping(anyLong(), any(OffsetDateTime.class), any(OffsetDateTime.class)))
                .thenReturn(emptyList());
        when(petRepository.findById(anyLong())).thenReturn(Optional.of(pet));
        when(visitRepository.findAllOverlappingInDateRange(any(OffsetDateTime.class), any(OffsetDateTime.class)))
                .thenReturn(emptyList());
        when(treatmentRoomRepository.findAll()).thenReturn(singletonList(treatmentRoom));

        IncorrectDataException thrown =
                catchThrowableOfType(() -> visitService.createVisit(USER, request), IncorrectDataException.class);

        assertThat(thrown)
                .hasMessage("This vet doesn't work at this hour.");

        verify(vetRepository).findById(eq(VET_ID));
        verify(petRepository).findById(eq(PET_ID));
        verify(visitRepository).findAllOverlapping(eq(VET_ID), eq(request.getStartDateTime()),
                eq(request.getStartDateTime().plus(request.getDuration())));
        verify(visitRepository).findAllOverlappingInDateRange(eq(request.getStartDateTime()),
                eq(request.getStartDateTime().plus(request.getDuration())));
        verify(treatmentRoomRepository).findAll();
    }

}
