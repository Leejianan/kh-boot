package com.kh.boot.controller;

import com.kh.boot.common.Result;
import com.kh.boot.entity.KhSerialNumber;
import com.kh.boot.service.SerialNumberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Serial Number Management", description = "APIs for managing business code generation rules")
@RestController
@RequestMapping("/system/serial-number")
@RequiredArgsConstructor
public class SerialNumberController {

    private final SerialNumberService serialNumberService;

    @Operation(summary = "Get All Rules", description = "List all registered serial number rules and their current states")
    @GetMapping("/list")
    public Result<List<KhSerialNumber>> list() {
        return Result.success(serialNumberService.list());
    }

    @Operation(summary = "Save or Update Rule", description = "Configure dynamic rules for a business key")
    @PostMapping("/save")
    public Result<Void> save(@RequestBody KhSerialNumber serialNumber) {
        serialNumberService.saveRule(serialNumber);
        return Result.success("Rule saved successfully", null);
    }

    @Operation(summary = "Reset Sequence", description = "Manually reset the sequence value for a business key")
    @PutMapping("/reset")
    public Result<Void> reset(@RequestParam String businessKey, @RequestParam Long nextValue) {
        serialNumberService.reset(businessKey, nextValue);
        return Result.success("Sequence reset successfully", null);
    }
}
