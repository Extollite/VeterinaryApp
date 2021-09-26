package pl.gr.veterinaryapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.gr.veterinaryapp.model.entity.Client;

public interface ClientRepository extends JpaRepository<Client, Long> {

}
