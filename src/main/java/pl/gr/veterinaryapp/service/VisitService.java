package pl.gr.veterinaryapp.service;

import org.springframework.security.core.userdetails.User;
import pl.gr.veterinaryapp.model.dto.AvailableVisitDto;
import pl.gr.veterinaryapp.model.dto.VisitEditDto;
import pl.gr.veterinaryapp.model.dto.VisitRequestDto;
import pl.gr.veterinaryapp.model.entity.Visit;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;

public interface VisitService {

    void deleteVisit(long id);

    List<Visit> getAllVisits(User user);

    Visit createVisit(User user, VisitRequestDto visitRequestDto);

    Visit finalizeVisit(VisitEditDto visitEditDto);

    Visit getVisitById(User user, long id);

    List<AvailableVisitDto> getAvailableVisits(
            OffsetDateTime startDateTime,
            OffsetDateTime endDateTime,
            Collection<Long> vetIds);
}
