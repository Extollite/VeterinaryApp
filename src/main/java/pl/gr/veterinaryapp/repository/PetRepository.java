package pl.gr.veterinaryapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.gr.veterinaryapp.model.entity.Pet;

public interface PetRepository extends JpaRepository<Pet, Long> {

}
