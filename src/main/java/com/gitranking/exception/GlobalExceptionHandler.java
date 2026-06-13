package com.gitranking.exception;

import com.gitranking.dto.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * Translates domain exceptions into safe, human-readable HTTP error responses.
 *
 * <p>Internal error details (raw GitHub API responses, stack traces) are written
 * to the log only and never included in the response body.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(GitHubRateLimitException.class)
    public ResponseEntity<ErrorResponse> handleRateLimit(GitHubRateLimitException ex) {
        log.warn("GitHub rate limit exceeded. {}", ex.getMessage());
        return error(HttpStatus.TOO_MANY_REQUESTS,
                "GitHub API rate limit exceeded. Please wait before retrying.");
    }

    @ExceptionHandler(GitHubAuthException.class)
    public ResponseEntity<ErrorResponse> handleAuth(GitHubAuthException ex) {
        log.error("GitHub authentication failure. {}", ex.getMessage());
        return error(HttpStatus.SERVICE_UNAVAILABLE,
                "Repository search is temporarily unavailable. Please try again later.");
    }

    @ExceptionHandler(GitHubUpstreamException.class)
    public ResponseEntity<ErrorResponse> handleUpstream(GitHubUpstreamException ex) {
        log.error("GitHub upstream error (status {}). {}", ex.getStatusCode(), ex.getMessage());
        return error(HttpStatus.BAD_GATEWAY,
                "Repository search is temporarily unavailable. Please try again later.");
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParam(MissingServletRequestParameterException ex) {
        return error(HttpStatus.BAD_REQUEST,
                "Required parameter '" + ex.getParameterName() + "' is missing.");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return error(HttpStatus.BAD_REQUEST,
                "Invalid value for parameter '" + ex.getName() + "'. Please check the request and try again.");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex) {
        log.error("Unexpected error processing request", ex);
        return error(HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred. Please try again later.");
    }

    private ResponseEntity<ErrorResponse> error(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(new ErrorResponse(status.value(), message));
    }
}
