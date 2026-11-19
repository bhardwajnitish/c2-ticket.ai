package com.example.ticket.api;

import com.example.ticket.api.dto.CreateTicketRequest;
import com.example.ticket.api.dto.PatchTicketRequest;
import com.example.ticket.api.dto.PatchTicketStatusRequest;
import com.example.ticket.api.dto.TicketFilter;
import com.example.ticket.api.dto.TicketResponse;
import com.example.ticket.domain.StatusChangeNotAllowedException;
import com.example.ticket.domain.TicketPriority;
import com.example.ticket.domain.TicketStatus;
import com.example.ticket.service.TicketService;
import com.example.ticket.service.TicketStatusTransitionService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/tickets")
public class TicketController {

    private final TicketService ticketService;
    private final TicketStatusTransitionService statusTransitionService;

    public TicketController(
            TicketService ticketService, TicketStatusTransitionService statusTransitionService) {
        this.ticketService = ticketService;
        this.statusTransitionService = statusTransitionService;
    }

    @PostMapping
    public ResponseEntity<TicketResponse> create(@Valid @RequestBody CreateTicketRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ticketService.create(request));
    }

    @GetMapping
    public List<TicketResponse> list(
            @RequestParam(required = false) TicketStatus status,
            @RequestParam(required = false) TicketPriority priority,
            @RequestParam(required = false) String assignee,
            @RequestParam(required = false) String category) {
        return ticketService.list(new TicketFilter(status, priority, assignee, category));
    }

    @GetMapping("/{ticketId}")
    public TicketResponse get(@PathVariable String ticketId) {
        return ticketService.getByTicketId(ticketId);
    }

    @PatchMapping("/{ticketId}")
    public TicketResponse patch(
            @PathVariable String ticketId, @Valid @RequestBody PatchTicketRequest request) {
        if (request.status() != null) {
            throw new StatusChangeNotAllowedException();
        }
        if (!request.hasAnyAllowedField()) {
            throw new ValidationException("At least one field is required for PATCH.");
        }
        return ticketService.updateFields(ticketId, request);
    }

    @PatchMapping("/{ticketId}/status")
    public TicketResponse patchStatus(
            @PathVariable String ticketId, @Valid @RequestBody PatchTicketStatusRequest request) {
        return statusTransitionService.transition(ticketId, request.status());
    }
}