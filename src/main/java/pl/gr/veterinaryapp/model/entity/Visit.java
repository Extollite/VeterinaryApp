package pl.gr.veterinaryapp.model.entity;

import com.vladmihalcea.hibernate.type.interval.PostgreSQLIntervalType;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import org.hibernate.annotations.TypeDef;
import pl.gr.veterinaryapp.common.OperationType;
import pl.gr.veterinaryapp.common.VisitStatus;
import pl.gr.veterinaryapp.common.VisitType;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.OffsetDateTime;

@Data
@Entity
@Table(name = "visits")
@TypeDef(
        typeClass = PostgreSQLIntervalType.class,
        defaultForType = Duration.class
)
public class Visit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "vet_id", referencedColumnName = "id", nullable = false)
    private Vet vet;
    @ManyToOne
    @JoinColumn(name = "pet_id", referencedColumnName = "id", nullable = false)
    private Pet pet;
    @ManyToOne
    @JoinColumn(name = "treatment_room_id", referencedColumnName = "id", nullable = false)
    private TreatmentRoom treatmentRoom;
    @NotNull
    private OffsetDateTime startDateTime;
    @Column(columnDefinition = "interval")
    @NotNull
    private Duration duration;
    @NotNull
    private BigDecimal price;
    @NotNull
    private VisitType visitType;
    private String visitDescription;
    @NotNull
    private VisitStatus visitStatus;
    @NotNull
    private OperationType operationType;

    @Transient
    @Setter(AccessLevel.PRIVATE)
    private OffsetDateTime endDateTime;

    public OffsetDateTime getEndDateTime() {
        if (endDateTime == null) {
            endDateTime = startDateTime.plus(duration);
        }
        return endDateTime;
    }
}
