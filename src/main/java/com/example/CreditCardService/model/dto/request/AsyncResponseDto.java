package com.example.CreditCardService.model.dto.request;

import com.example.CreditCardService.model.enums.AsyncStatus;
import lombok.*;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class AsyncResponseDto<T> {
    private UUID id;
    private AsyncStatus status;
    private T body;
}
