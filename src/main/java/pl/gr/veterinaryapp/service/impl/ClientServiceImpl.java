package pl.gr.veterinaryapp.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.gr.veterinaryapp.exception.IncorrectDataException;
import pl.gr.veterinaryapp.exception.ResourceNotFoundException;
import pl.gr.veterinaryapp.mapper.ClientMapper;
import pl.gr.veterinaryapp.model.dto.ClientRequestDto;
import pl.gr.veterinaryapp.model.entity.Client;
import pl.gr.veterinaryapp.model.entity.VetAppUser;
import pl.gr.veterinaryapp.repository.ClientRepository;
import pl.gr.veterinaryapp.repository.UserRepository;
import pl.gr.veterinaryapp.service.ClientService;

import java.util.List;

@RequiredArgsConstructor
@Service
public class ClientServiceImpl implements ClientService {

    private final ClientRepository clientRepository;
    private final ClientMapper mapper;
    private final UserRepository userRepository;

    @Override
    public Client getClientById(long id) {
        return clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Wrong id."));
    }

    @Override
    public List<Client> getAllClients() {
        return clientRepository.findAll();
    }

    @Transactional
    @Override
    public Client createClient(ClientRequestDto clientRequestDTO) {
        if (clientRequestDTO.getSurname() == null || clientRequestDTO.getName() == null) {
            throw new IncorrectDataException("Name and Surname should not be null.");
        }

        VetAppUser user = userRepository.findByUsername(clientRequestDTO.getUsername())
                .orElse(null);

        Client client = mapper.map(clientRequestDTO);
        client.setUser(user);

        return clientRepository.save(client);
    }

    @Transactional
    @Override
    public void deleteClient(long id) {
        Client result = clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Wrong id."));
        clientRepository.delete(result);
    }
}
