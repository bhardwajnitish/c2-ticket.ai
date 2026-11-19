package com.example.ticket.api;

import com.example.ticket.api.dto.CommentResponse;
import com.example.ticket.api.dto.CreateCommentRequest;
import com.example.ticket.service.CommentService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/tickets/{ticketId}/comments")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping
    public ResponseEntity<CommentResponse> create(
            @PathVariable String ticketId, @Valid @RequestBody CreateCommentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(commentService.addComment(ticketId, request));
    }

    @GetMapping
    public List<CommentResponse> list(@PathVariable String ticketId) {
        return commentService.listComments(ticketId);
    }
}