package com.kh.boot.controller;

import com.kh.boot.common.PageData;
import com.kh.boot.common.Result;
import com.kh.boot.controller.base.BaseController;
import com.kh.boot.dto.KhEmailRecordDTO;
import com.kh.boot.query.EmailRecordQuery;
import com.kh.boot.service.EmailRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * Email Record Controller
 * </p>
 *
 * @author harlan
 * @since 2024-01-15
 */
@Tag(name = "Email Log", description = "Email sending record management")
@RestController
@RequestMapping("/email/records")
@RequiredArgsConstructor
public class EmailRecordController extends BaseController {

    private final EmailRecordService emailRecordService;

    @Operation(summary = "Get Email Record List", description = "Retrieve email records with pagination")
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('system:email:list')")
    public Result<PageData<KhEmailRecordDTO>> list(EmailRecordQuery query) {
        return pageSuccess(emailRecordService.page(query));
    }
}
