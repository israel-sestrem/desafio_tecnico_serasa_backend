package com.grain_weighing.controllers;

import com.grain_weighing.dto.WeighingInsertionRequestDto;
import com.grain_weighing.dto.WeighingResponseDto;
import com.grain_weighing.services.WeighingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/weighings")
@RequiredArgsConstructor
public class WeighingController {

    private final WeighingService weighingIngestionService;

    @PostMapping("/insert")
    public ResponseEntity<?> insert(@RequestBody WeighingInsertionRequestDto request) {
        try {
            return weighingIngestionService.insertRawWeighing(request)
                    .<ResponseEntity<?>>map(weighing ->
                            ResponseEntity.status(HttpStatus.CREATED)
                                    .body(WeighingResponseDto.from(weighing)))
                    .orElseGet(() -> ResponseEntity.accepted().body("Weight still not stable"));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
