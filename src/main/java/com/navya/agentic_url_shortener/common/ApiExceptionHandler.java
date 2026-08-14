package com.navya.agentic_url_shortener.common;

import com.navya.agentic_url_shortener.url.exception.InvalidUrlException;
import com.navya.agentic_url_shortener.url.exception.ShortCodeGenerationException;
import com.navya.agentic_url_shortener.url.exception.ShortUrlNotFoundException;
import com.navya.agentic_url_shortener.url.exception.ShortUrlUnavailableException;
import com.navya.agentic_url_shortener.idempotency.exception.IdempotencyConflictException;
import com.navya.agentic_url_shortener.idempotency.exception.IdempotencyInProgressException;
import com.navya.agentic_url_shortener.idempotency.exception.InvalidIdempotencyKeyException;
import com.navya.agentic_url_shortener.orchestration.exception.InvalidWorkflowGraphException;
import com.navya.agentic_url_shortener.orchestration.exception.WorkflowNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(InvalidUrlException.class)
    public ProblemDetail handleInvalidUrl(
            InvalidUrlException exception,
            HttpServletRequest request
    ) {
        return createProblem(
                HttpStatus.BAD_REQUEST,
                "Invalid URL",
                exception.getMessage(),
                "invalid-url",
                request
        );
    }

    @ExceptionHandler(ShortUrlNotFoundException.class)
    public ProblemDetail handleNotFound(
            ShortUrlNotFoundException exception,
            HttpServletRequest request
    ) {
        return createProblem(
                HttpStatus.NOT_FOUND,
                "Short URL not found",
                exception.getMessage(),
                "short-url-not-found",
                request
        );
    }

    @ExceptionHandler(ShortUrlUnavailableException.class)
    public ProblemDetail handleUnavailable(
            ShortUrlUnavailableException exception,
            HttpServletRequest request
    ) {
        return createProblem(
                HttpStatus.GONE,
                "Short URL unavailable",
                exception.getMessage(),
                "short-url-unavailable",
                request
        );
    }

    @ExceptionHandler(ShortCodeGenerationException.class)
    public ProblemDetail handleGenerationFailure(
            ShortCodeGenerationException exception,
            HttpServletRequest request
    ) {
        return createProblem(
                HttpStatus.SERVICE_UNAVAILABLE,
                "Short-code allocation failed",
                exception.getMessage(),
                "short-code-generation-failed",
                request
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgument(
            IllegalArgumentException exception,
            HttpServletRequest request
    ) {
        return createProblem(
                HttpStatus.BAD_REQUEST,
                "Invalid request",
                exception.getMessage(),
                "invalid-request",
                request
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        ProblemDetail problem = createProblem(
                HttpStatus.BAD_REQUEST,
                "Request validation failed",
                "One or more request fields are invalid",
                "validation-failed",
                request
        );

        Map<String, String> errors = new LinkedHashMap<>();

        exception.getBindingResult()
                .getFieldErrors()
                .forEach(error ->
                        errors.putIfAbsent(
                                error.getField(),
                                error.getDefaultMessage()
                        )
                );

        problem.setProperty("errors", errors);

        return problem;
    }

    @ExceptionHandler(WorkflowNotFoundException.class)
    public ProblemDetail handleWorkflowNotFound(
            WorkflowNotFoundException exception,
            HttpServletRequest request
    ) {
        return createProblem(
                HttpStatus.NOT_FOUND,
                "Engineering workflow not found",
                exception.getMessage(),
                "workflow-not-found",
                request
        );
    }

    @ExceptionHandler(InvalidWorkflowGraphException.class)
    public ProblemDetail handleInvalidWorkflowGraph(
            InvalidWorkflowGraphException exception,
            HttpServletRequest request
    ) {
        return createProblem(
                HttpStatus.UNPROCESSABLE_ENTITY,
                "Invalid workflow graph",
                exception.getMessage(),
                "invalid-workflow-graph",
                request
        );
    }

    private ProblemDetail createProblem(
            HttpStatus status,
            String title,
            String detail,
            String type,
            HttpServletRequest request
    ) {
        ProblemDetail problem =
                ProblemDetail.forStatusAndDetail(
                        status,
                        detail
                );

        problem.setTitle(title);
        problem.setType(
                URI.create(
                        "https://agentic-url-shortener.dev/problems/"
                                + type
                )
        );
        problem.setInstance(
                URI.create(request.getRequestURI())
        );
        problem.setProperty(
                "timestamp",
                Instant.now()
        );

        return problem;
    }

    @ExceptionHandler(InvalidIdempotencyKeyException.class)
    public ProblemDetail handleInvalidIdempotencyKey(
            InvalidIdempotencyKeyException exception,
            HttpServletRequest request
    ) {
        return createProblem(
                HttpStatus.BAD_REQUEST,
                "Invalid idempotency key",
                exception.getMessage(),
                "invalid-idempotency-key",
                request
        );
    }

    @ExceptionHandler(IdempotencyConflictException.class)
    public ProblemDetail handleIdempotencyConflict(
            IdempotencyConflictException exception,
            HttpServletRequest request
    ) {
        return createProblem(
                HttpStatus.CONFLICT,
                "Idempotency conflict",
                exception.getMessage(),
                "idempotency-conflict",
                request
        );
    }

    @ExceptionHandler(IdempotencyInProgressException.class)
    public ProblemDetail handleIdempotencyInProgress(
            IdempotencyInProgressException exception,
            HttpServletRequest request
    ) {
        ProblemDetail problem = createProblem(
                HttpStatus.CONFLICT,
                "Request already in progress",
                exception.getMessage(),
                "idempotency-in-progress",
                request
        );

        problem.setProperty("retryable", true);

        return problem;
    }
}