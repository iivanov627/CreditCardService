package com.example.CreditCardService.controller;


import com.example.CreditCardService.model.dto.request.CreateClientDto;
import com.example.CreditCardService.model.dto.response.ResponseClientDto;
import com.example.CreditCardService.service.ClientService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
public class ClientController {
    private final ClientService clientService;

    @Autowired
    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }

    @PostMapping("/clients")
    public ResponseEntity<ResponseClientDto> createClient(
            @Valid @RequestBody CreateClientDto clientDto
    ) {
        ResponseClientDto client = clientService.createClient(clientDto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(client);
    }

    @GetMapping("/clients")
    public ResponseEntity<List<ResponseClientDto>> getAllClients() {
        return ResponseEntity.ok(clientService.getAll());
    }

    @GetMapping("/clients/{id}")
    public ResponseEntity<ResponseClientDto> getClient(
            @PathVariable UUID id
    ) {
        return ResponseEntity
                .ok(clientService.getById(id));
    }
}
