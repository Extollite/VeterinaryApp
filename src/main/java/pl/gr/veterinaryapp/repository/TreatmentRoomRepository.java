package pl.gr.veterinaryapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.gr.veterinaryapp.model.entity.TreatmentRoom;

@Repository
public interface TreatmentRoomRepository extends JpaRepository<TreatmentRoom, Long> {

}
