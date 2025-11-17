package com.grain_weighing.controllers;

import com.grain_weighing.dto.WeighingInsertionRequestDto;
import com.grain_weighing.dto.WeighingResponseDto;
import com.grain_weighing.services.WeighingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/weighings")
@RequiredArgsConstructor
@Tag(name = "Weighings", description = "Endpoints for ESP32 insertion and weighing operations")
public class WeighingController {

    private final WeighingService weighingIngestionService;

    @PostMapping("/insert")
    @Operation(
            summary = "Insert raw weighing from ESP32",
            description = """
                    Receives weight readings sent by the ESP32 while there is a truck on the scale.
                    Weighing only persists when the reading has stabilized according to the configured strategy.
                    """
    )
    public ResponseEntity<?> insert(
            @Parameter(description = "API token for the scale, previously registered.")
            @RequestHeader("X-Scale-Token") String token,

            @RequestBody WeighingInsertionRequestDto request
    ) {
        return weighingIngestionService.insertRawWeighing(request, token)
                .<ResponseEntity<?>>map(weighing -> ResponseEntity.status(HttpStatus.CREATED)
                        .body(WeighingResponseDto.from(weighing)))
                .orElseGet(() -> ResponseEntity.accepted().body(Map.of(
                        "status", "PENDING_STABILIZATION",
                        "message", "Weight not stable yet"
                )));
    }
}
