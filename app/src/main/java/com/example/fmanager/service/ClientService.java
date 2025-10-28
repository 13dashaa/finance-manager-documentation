package com.example.fmanager.service;

import static com.example.fmanager.exception.NotFoundMessages.CLIENT_NOT_FOUND_MESSAGE;

import com.example.fmanager.dto.ClientCreateDto;
import com.example.fmanager.dto.ClientGetDto;
import com.example.fmanager.dto.ClientUpdateDto;
import com.example.fmanager.exception.NotFoundException;
import com.example.fmanager.models.Client;
import com.example.fmanager.repository.ClientRepository;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class ClientService {
    private ClientRepository clientRepository;

    public ClientService(ClientRepository userRepository) {
        this.clientRepository = userRepository;
    }

    public List<ClientGetDto> findAll() {
        List<Client> clients = clientRepository.findAll();
        List<ClientGetDto> clientDtos = new ArrayList<>();
        for (Client client : clients) {
            clientDtos.add(ClientGetDto.convertToDto(client));
        }
        return clientDtos;
    }

    public Optional<ClientGetDto> findById(int id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(CLIENT_NOT_FOUND_MESSAGE));
        return Optional.of(ClientGetDto.convertToDto(client));
    }

    public Client createUser(ClientCreateDto userCreateDto) {
        Client client = new Client();
        client.setUsername(userCreateDto.getUsername());
        client.setPassword(userCreateDto.getPassword());
        client.setEmail(userCreateDto.getEmail());
        return clientRepository.save(client);


    }

    @Transactional
    public ClientGetDto updateUser(int id, ClientUpdateDto userDetails) {
        Client user = clientRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(CLIENT_NOT_FOUND_MESSAGE));
        user.setUsername(userDetails.getUsername());
        return ClientGetDto.convertToDto(clientRepository.save(user));
    }

    @Transactional
    public void deleteUser(int id) {
        Client user = clientRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(CLIENT_NOT_FOUND_MESSAGE));
        clientRepository.delete(user);
    }

    public List<Client> findAllClients() {
        return clientRepository.findAll(); // Return the entities
    }

    public Optional<Client> findClientById(int id) {
        return clientRepository.findById(id);  //Return Optional<Client>
    }
}
