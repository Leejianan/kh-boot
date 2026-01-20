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
@RequestMapping("/admin/system/email")
@RequiredArgsConstructor
public class EmailRecordController extends BaseController {

    private final EmailRecordService emailRecordService;
    private final com.kh.boot.service.EmailService emailService;

    @Operation(summary = "Get Email Record List", description = "Retrieve email records with pagination")
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('system:email:list')")
    public Result<PageData<KhEmailRecordDTO>> list(EmailRecordQuery query) {
        return pageSuccess(emailRecordService.page(query));
    }

    @Operation(summary = "Delete Email Records", description = "Delete email records by IDs")
    @org.springframework.web.bind.annotation.DeleteMapping("/{ids}")
    @PreAuthorize("hasAuthority('system:email:remove')")
    public Result<Boolean> remove(@org.springframework.web.bind.annotation.PathVariable java.util.List<Long> ids) {
        emailRecordService.deleteEmailRecords(ids);
        return success(true);
    }

    @Operation(summary = "Send Email", description = "Send email to specified recipient")
    @org.springframework.web.bind.annotation.PostMapping("/send")
    @PreAuthorize("hasAuthority('system:email:send')")
    public Result<Boolean> send(@org.springframework.web.bind.annotation.RequestBody SendEmailRequest request) {
        emailService.sendEmail(request.getTo(), request.getSubject(), request.getContent());
        return success(true);
    }

    @lombok.Data
    public static class SendEmailRequest {
        private String to;
        private String subject;
        private String content;
    }
}
