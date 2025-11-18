package com.grain_weighing.controllers;

import com.grain_weighing.dto.BranchRequestDto;
import com.grain_weighing.dto.BranchResponseDto;
import com.grain_weighing.entities.BranchEntity;
import com.grain_weighing.services.BranchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/branches")
@RequiredArgsConstructor
@Tag(name = "Branches", description = "Branch management")
public class BranchController {

    private final BranchService branchService;

    @PostMapping
    @Operation(summary = "Create a new branch")
    public ResponseEntity<BranchResponseDto> create(@Valid @RequestBody BranchRequestDto request) {
        BranchEntity branch = branchService.create(request);

        return ResponseEntity
                .created(URI.create("/api/branches/" + branch.getId()))
                .body(new BranchResponseDto(
                        branch.getId(),
                        branch.getCode(),
                        branch.getName(),
                        branch.getCity(),
                        branch.getState()
                ));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BranchResponseDto> update(
            @PathVariable UUID id,
            @Valid @RequestBody BranchRequestDto request
    ) {
        BranchEntity updated = branchService.update(id, request);
        return ResponseEntity.ok(BranchResponseDto.from(updated));
    }

    @GetMapping
    @Operation(summary = "List all branches")
    public List<BranchResponseDto> list() {
        return branchService.findAll().stream()
                .map(b -> new BranchResponseDto(
                        b.getId(),
                        b.getCode(),
                        b.getName(),
                        b.getCity(),
                        b.getState()
                ))
                .toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get branch by ID")
    public ResponseEntity<BranchResponseDto> get(@PathVariable UUID id) {
        return branchService.findById(id)
                .map(b -> ResponseEntity.ok(new BranchResponseDto(
                        b.getId(),
                        b.getCode(),
                        b.getName(),
                        b.getCity(),
                        b.getState()
                )))
                .orElse(ResponseEntity.notFound().build());
    }
}

