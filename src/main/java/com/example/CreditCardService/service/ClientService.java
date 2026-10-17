package com.example.CreditCardService.service;

import com.example.CreditCardService.dao.ClientRepository;
import com.example.CreditCardService.exception.CustomException.ConflictException;
import com.example.CreditCardService.exception.CustomException.NotFoundException;
import com.example.CreditCardService.model.dto.request.CreateClientDto;
import com.example.CreditCardService.model.dto.response.ResponseClientDto;
import com.example.CreditCardService.model.entity.Client;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ClientService {
    private final ClientRepository clientRepository;
    private final ModelMapper modelMapper;

    @Autowired
    public ClientService(ClientRepository clientRepository, ModelMapper modelMapper) {
        this.clientRepository = clientRepository;
        this.modelMapper = modelMapper;
    }

    public List<ResponseClientDto> getAll() {
        List<Client> clientList = clientRepository.findAll();
        return clientList.stream()
                .map(client -> modelMapper.map(client, ResponseClientDto.class))
                .toList();
    }

    public ResponseClientDto getById(UUID id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Client not found"));
        return modelMapper.map(client, ResponseClientDto.class);
    }

    public Client getEntityById(UUID id) {
        return clientRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Client not found"));
    }

    @Transactional
    public ResponseClientDto createClient(CreateClientDto clientDto) {
        if (clientRepository.findBySeriesAndPassportNumber(
                clientDto.getSeries(),
                clientDto.getPassportNumber()
        ).isPresent()) {
            throw new ConflictException("Client with this passport already exists");
        }

        if (clientRepository.findByEmail(clientDto.getEmail())
                .isPresent()) {
            throw new ConflictException("Client with this email already exists");
        }

        Client client = modelMapper.map(clientDto, Client.class);

        return modelMapper.map(clientRepository.save(client), ResponseClientDto.class);
    }

}
