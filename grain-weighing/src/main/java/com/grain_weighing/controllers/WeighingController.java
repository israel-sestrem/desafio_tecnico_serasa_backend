package com.grain_weighing.controllers;

import com.grain_weighing.dto.WeighingInsertionRequestDto;
import com.grain_weighing.dto.WeighingResponseDto;
import com.grain_weighing.services.WeighingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/weighings")
@RequiredArgsConstructor
public class WeighingController {

    private final WeighingService weighingIngestionService;

    @PostMapping("/insert")
    public ResponseEntity<?> insert(
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
