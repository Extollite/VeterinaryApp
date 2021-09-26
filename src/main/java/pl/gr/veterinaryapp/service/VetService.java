package pl.gr.veterinaryapp.service;

import pl.gr.veterinaryapp.model.dto.VetRequestDto;
import pl.gr.veterinaryapp.model.entity.Vet;

import java.util.List;

public interface VetService {

    Vet getVetById(long id);

    List<Vet> getAllVets();

    Vet createVet(VetRequestDto vetRequestDTO);

    void deleteVet(long id);
}
