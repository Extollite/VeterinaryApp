package pl.gr.veterinaryapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pl.gr.veterinaryapp.common.VisitStatus;
import pl.gr.veterinaryapp.model.entity.Visit;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;

@Repository
public interface VisitRepository extends JpaRepository<Visit, Long> {

    @Query("select v " +
            "from Visit v " +
            "where :vetId = v.vet.id " +
            "and v.startDateTime  <= :endDateTime " +
            "and (v.startDateTime + v.duration) >= :startDateTime")
    List<Visit> findAllOverlapping(long vetId, OffsetDateTime startDateTime, OffsetDateTime endDateTime);

    @Query("select v from Visit v where " +
            "(v.startDateTime + v.duration) <= :now " +
            "and v.visitStatus = :visitStatus")
    List<Visit> findAllByEndDateAndEndTimeBeforeAndVisitStatus(OffsetDateTime now, VisitStatus visitStatus);

    @Query("select v from Visit v where " +
            "v.startDateTime >= :startDateTime " +
            "and (v.startDateTime + v.duration) <= :endDateTime " +
            "and v.vet.id in :vetIds")
    List<Visit> findAllInDateTimeRangeAndVetIdIn(
            OffsetDateTime startDateTime,
            OffsetDateTime endDateTime,
            Collection<Long> vetIds);

    @Query("select v from Visit v where " +
            "v.startDateTime  <= :endDateTime " +
            "and (v.startDateTime + v.duration) >= :startDateTime")
    List<Visit> findAllOverlappingInDateRange(
            OffsetDateTime startDateTime,
            OffsetDateTime endDateTime);
}
