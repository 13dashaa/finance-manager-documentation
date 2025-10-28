package com.example.fmanager.service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import com.example.fmanager.dto.ClientCreateDto;
import com.example.fmanager.dto.ClientGetDto;
import com.example.fmanager.dto.ClientUpdateDto;
import com.example.fmanager.exception.NotFoundException;
import com.example.fmanager.models.Client;
import com.example.fmanager.repository.ClientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClientServiceTest {

    @Mock
    private ClientRepository clientRepository;

    @InjectMocks
    private ClientService clientService;

    private Client client1;
    private Client client2;

    @BeforeEach
    void setUp() {
        client1 = new Client();
        client1.setId(1);
        client1.setUsername("testuser1");
        client1.setPassword("password1");
        client1.setEmail("test1@example.com");

        client2 = new Client();
        client2.setId(2);
        client2.setUsername("testuser2");
        client2.setPassword("password2");
        client2.setEmail("test2@example.com");
    }

    @Test
    void findAll_Success() {
        when(clientRepository.findAll()).thenReturn(Arrays.asList(client1, client2));
        List<ClientGetDto> result = clientService.findAll();
        assertEquals(2, result.size());
        assertEquals("testuser1", result.get(0).getUsername());
    }

    @Test
    void findById_Success() {
        when(clientRepository.findById(1)).thenReturn(Optional.of(client1));
        Optional<ClientGetDto> result = clientService.findById(1);
        assertTrue(result.isPresent());
        assertEquals("testuser1", result.get().getUsername());
    }

    @Test
    void findById_NotFound() {
        when(clientRepository.findById(1)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> clientService.findById(1));
    }

    @Test
    void createUser_ShouldSaveAndReturnClient() {
        ClientCreateDto dto = new ClientCreateDto("newuser", "newpass", "new@example.com");
        Client newClient = new Client();
        newClient.setUsername(dto.getUsername());
        newClient.setPassword(dto.getPassword());
        newClient.setEmail(dto.getEmail());
        when(clientRepository.save(any(Client.class))).thenReturn(newClient);
        Client result = clientService.createUser(dto);
        assertEquals("newuser", result.getUsername());
    }

    @Test
    void updateUser_ShouldUpdateAndReturnClient() {
        ClientUpdateDto dto = new ClientUpdateDto("updatedUser");
        when(clientRepository.findById(1)).thenReturn(Optional.of(client1));
        when(clientRepository.save(any(Client.class))).thenReturn(client1);
        ClientGetDto result = clientService.updateUser(1, dto);
        assertEquals("updatedUser", result.getUsername());
    }

    @Test
    void deleteUser_Success() {
        when(clientRepository.findById(1)).thenReturn(Optional.of(client1));
        doNothing().when(clientRepository).delete(client1);
        assertDoesNotThrow(() -> clientService.deleteUser(1));
        verify(clientRepository, times(1)).delete(client1);
    }

    @Test
    void deleteUser_NotFound() {
        when(clientRepository.findById(1)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> clientService.deleteUser(1));
    }
}