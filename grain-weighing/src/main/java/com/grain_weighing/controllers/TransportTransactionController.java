package com.grain_weighing.controllers;

import com.grain_weighing.dto.TransportTransactionRequestDto;
import com.grain_weighing.dto.TransportTransactionResponseDto;
import com.grain_weighing.entities.TransportTransactionEntity;
import com.grain_weighing.services.TransportTransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/transport-transactions")
@RequiredArgsConstructor
@Tag(name = "Transport Transactions", description = "Transport transaction management")
public class TransportTransactionController {

    private final TransportTransactionService transportTransactionService;

    @PostMapping("/open")
    @Operation(summary = "Open a new transport transaction")
    public ResponseEntity<?> open(@Valid @RequestBody TransportTransactionRequestDto request) {
        TransportTransactionEntity tt = transportTransactionService.open(request);

        return ResponseEntity
                .created(URI.create("/api/transport-transactions/" + tt.getId()))
                .body(toResponse(tt));
    }

    @PostMapping("/{id}/close")
    @Operation(summary = "Close a transport transaction")
    public ResponseEntity<?> close(@PathVariable UUID id) {
        TransportTransactionEntity tt = transportTransactionService.close(id);

        return ResponseEntity.ok(toResponse(tt));
    }

    private TransportTransactionResponseDto toResponse(TransportTransactionEntity tt) {
        return new TransportTransactionResponseDto(
                tt.getId(),
                tt.getTruck().getId(),
                tt.getBranch().getId(),
                tt.getGrainType().getId(),
                tt.getStartTimestamp(),
                tt.getEndTimestamp(),
                tt.getAppliedMargin(),
                tt.getPurchasePricePerTon(),
                tt.getSalePricePerTon(),
                tt.getTotalNetWeightKg(),
                tt.getTotalLoadCost(),
                tt.getTotalEstimatedRevenue(),
                tt.getEstimatedProfit()
        );
    }
}
