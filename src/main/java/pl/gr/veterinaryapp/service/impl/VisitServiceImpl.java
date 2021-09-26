package pl.gr.veterinaryapp.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.gr.veterinaryapp.common.VisitStatus;
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
import pl.gr.veterinaryapp.service.VisitService;

import java.time.Clock;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class VisitServiceImpl implements VisitService {

    private static final int MINIMAL_TIME_TO_VISIT = 60;

    private final VisitRepository visitRepository;
    private final VetRepository vetRepository;
    private final PetRepository petRepository;
    private final TreatmentRoomRepository treatmentRoomRepository;
    private final Clock systemClock;

    @Override
    public Visit getVisitById(User user, long id) {
        Visit visit = visitRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Wrong id."));

        if (!isUserAuthorized(user, visit.getPet().getClient())) {
            throw new ResourceNotFoundException("Wrong id.");
        }

        return visit;
    }

    @Override
    public List<Visit> getAllVisits(User user) {
        return visitRepository.findAll()
                .stream()
                .filter(visit -> isUserAuthorized(user, visit.getPet().getClient()))
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public Visit createVisit(User user, VisitRequestDto visitRequestDto) {
        var vetId = visitRequestDto.getVetId();
        var startDateTime = visitRequestDto.getStartDateTime();
        var duration = visitRequestDto.getDuration();

        Vet vet = vetRepository.findById(vetId)
                .orElseThrow(() -> new IncorrectDataException("Wrong vet id."));

        validateVisitDate(vetId, startDateTime, duration);

        Pet pet = petRepository.findById(visitRequestDto.getPetId())
                .orElseThrow(() -> new IncorrectDataException("Wrong pet id."));

        if (!isUserAuthorized(user, pet.getClient())) {
            throw new ResourceNotFoundException("Wrong id.");
        }

        var treatmentRoom = getFreeTreatmentRoom(startDateTime, duration)
                .orElseThrow(() -> new IncorrectDataException("There is no free treatment room."));

        if (!isTimeBetweenIncludingEndPoints(vet.getWorkStartTime(), vet.getWorkEndTime(), visitRequestDto.getStartDateTime())) {
            throw new IncorrectDataException("This vet doesn't work at this hour.");
        }

        var newVisit = new Visit();
        newVisit.setPet(pet);
        newVisit.setVet(vet);
        newVisit.setStartDateTime(startDateTime);
        newVisit.setDuration(duration);
        newVisit.setPrice(visitRequestDto.getPrice());
        newVisit.setVisitType(visitRequestDto.getVisitType());
        newVisit.setVisitStatus(VisitStatus.SCHEDULED);
        newVisit.setOperationType(visitRequestDto.getOperationType());
        newVisit.setTreatmentRoom(treatmentRoom);

        return visitRepository.save(newVisit);
    }

    private void validateVisitDate(long vetId, OffsetDateTime startDateTime, Duration duration) {
        var nowZoned = OffsetDateTime.now(systemClock);

        if (startDateTime.isBefore(nowZoned)) {
            throw new IncorrectDataException("Visit startDateTime need to be in future.");
        }

        if (Duration.between(nowZoned, startDateTime).toMinutes() < MINIMAL_TIME_TO_VISIT) {
            throw new IncorrectDataException("The time to your visit is too short.");
        }

        if (visitRepository.findAllOverlapping(vetId, startDateTime, startDateTime.plus(duration)).size() != 0) {
            throw new IncorrectDataException("This date is not available.");
        }
    }

    private Optional<TreatmentRoom> getFreeTreatmentRoom(OffsetDateTime startDateTime, Duration duration) {
        var occupiedRooms =
                visitRepository.findAllOverlappingInDateRange(startDateTime, startDateTime.plus(duration))
                        .stream()
                        .map(Visit::getTreatmentRoom)
                        .collect(Collectors.toSet());
        var rooms = treatmentRoomRepository.findAll();
        rooms.removeAll(occupiedRooms);

        if (rooms.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(rooms.get(0));
        }
    }

    @Transactional
    @Override
    public Visit finalizeVisit(VisitEditDto visitEditDto) {
        Visit visit = visitRepository.findById(visitEditDto.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Wrong id."));
        if (visitEditDto.getVisitStatus() == VisitStatus.FINISHED
                || visitEditDto.getVisitStatus() == VisitStatus.DID_NOT_APPEAR
                || visitEditDto.getVisitStatus() == VisitStatus.CANCELLED) {
            visit.setVisitStatus(visitEditDto.getVisitStatus());
        }
        visit.setVisitDescription(visitEditDto.getDescription());
        return visit;
    }

    @Transactional
    @Override
    public void deleteVisit(long id) {
        Visit result = visitRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Wrong id."));
        visitRepository.delete(result);
    }

    @Transactional
    @Override
    public List<AvailableVisitDto> getAvailableVisits(
            OffsetDateTime startDateTime,
            OffsetDateTime endDateTime,
            Collection<Long> vetIds) {
        var vets = getVets(vetIds);
        var vetIdsSet = vets
                .stream()
                .map(Vet::getId)
                .collect(Collectors.toSet());
        var visits = visitRepository.findAllInDateTimeRangeAndVetIdIn(startDateTime, endDateTime, vetIdsSet);

        List<AvailableVisitDto> result = new ArrayList<>();

        var visitSlotStart = startDateTime;
        while (visitSlotStart.compareTo(endDateTime) < 0) {
            var visitSlotEnd = visitSlotStart.plusMinutes(15);

            var busyVetIds = getBusyVetIds(visits, visitSlotStart, visitSlotEnd);
            var availableVetIds = getAvailableVetIds(vets, visitSlotStart, visitSlotEnd);
            availableVetIds.removeAll(busyVetIds);

            if (!availableVetIds.isEmpty()) {
                var availableVisit = new AvailableVisitDto();
                availableVisit.setVetIds(availableVetIds);
                availableVisit.setStartDateTime(visitSlotStart);
                result.add(availableVisit);
            }

            visitSlotStart = visitSlotEnd;
        }

        return result;
    }

    private List<Vet> getVets(Collection<Long> vetIds) {
        if (vetIds.isEmpty()) {
            return vetRepository.findAll();
        }
        return vetRepository.findAllById(vetIds);
    }

    private List<Long> getBusyVetIds(Collection<Visit> visits,
                                     OffsetDateTime visitSlotStart,
                                     OffsetDateTime visitSlotEnd) {
        return visits
                .stream()
                .filter(visit ->
                        visitSlotStart.compareTo(visit.getStartDateTime()) >= 0
                                && visitSlotEnd.compareTo(visit.getEndDateTime()) <= 0)
                .map(Visit::getVet)
                .map(Vet::getId)
                .collect(Collectors.toList());
    }

    private List<Long> getAvailableVetIds(Collection<Vet> vets,
                                          OffsetDateTime visitSlotStart,
                                          OffsetDateTime visitSlotEnd) {
        return vets
                .stream()
                .filter(vet ->
                        visitSlotStart.toOffsetTime().compareTo(vet.getWorkStartTime()) >= 0
                                && visitSlotEnd.toOffsetTime().compareTo(vet.getWorkEndTime()) <= 0)
                .map(Vet::getId)
                .collect(Collectors.toList());
    }

    @Scheduled(fixedDelay = 60 * 60 * 1000)
    @Transactional
    public void checkExpiredVisits() {
        var visits =
                visitRepository.findAllByEndDateAndEndTimeBeforeAndVisitStatus(
                        OffsetDateTime.now(systemClock), VisitStatus.SCHEDULED);
        for (var visit : visits) {
            visit.setVisitStatus(VisitStatus.EXPIRED);
        }
    }

    private boolean isUserAuthorized(User user, Client client) {
        boolean isClient = user.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_CLIENT"::equalsIgnoreCase);
        if (isClient) {
            if (client.getUser() == null) {
                return false;
            } else {
                return client.getUser().getUsername().equalsIgnoreCase(user.getUsername());
            }
        }
        return true;
    }

    public boolean isTimeBetweenIncludingEndPoints(OffsetTime min, OffsetTime max, OffsetDateTime date) {
        return !(date.toOffsetTime().isBefore(min) || date.toOffsetTime().isAfter(max));
    }
}
