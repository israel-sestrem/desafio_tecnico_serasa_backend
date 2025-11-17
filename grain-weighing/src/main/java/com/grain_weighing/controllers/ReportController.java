package com.grain_weighing.controllers;

import com.grain_weighing.dto.WeighingResponseDto;
import com.grain_weighing.dto.WeighingSummaryResponseDto;
import com.grain_weighing.repositories.WeighingRepository;
import com.grain_weighing.services.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final WeighingRepository weighingRepository;
    private final ReportService reportService;

    @GetMapping("/weighings")
    public List<WeighingResponseDto> listWeighings(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            @RequestParam(required = false) UUID branchId,
            @RequestParam(required = false) UUID truckId,
            @RequestParam(required = false) UUID grainTypeId
    ) {
        return weighingRepository.findByWeighingTimestampBetween(start, end).stream()
                .filter(w -> branchId == null || w.getScale().getBranch().getId().equals(branchId))
                .filter(w -> truckId == null || (w.getTruck() != null && w.getTruck().getId().equals(truckId)))
                .filter(w -> grainTypeId == null || w.getGrainType().getId().equals(grainTypeId))
                .map(WeighingResponseDto::from)
                .toList();
    }

    @GetMapping("/summary")
    public ResponseEntity<WeighingSummaryResponseDto> summary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            @RequestParam(required = false) UUID branchId,
            @RequestParam(required = false) UUID truckId,
            @RequestParam(required = false) UUID grainTypeId
    ) {
        WeighingSummaryResponseDto summary = reportService.summarize(start, end, branchId, truckId, grainTypeId);
        return ResponseEntity.ok(summary);
    }
}
