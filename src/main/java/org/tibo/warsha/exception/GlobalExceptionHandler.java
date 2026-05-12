package org.tibo.warsha.exception;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@ControllerAdvice
public class GlobalExceptionHandler {

    // ── 404 ───────────────────────────────────────────────────────────────────

    @ExceptionHandler({NoHandlerFoundException.class, NoResourceFoundException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handle404(Exception ex, Model model) {
        model.addAttribute("status", 404);
        model.addAttribute("message", "The page you were looking for doesn't exist.");
        return "error/404";
    }

    // ── 403 ───────────────────────────────────────────────────────────────────

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String handle403(AccessDeniedException ex, Model model) {
        model.addAttribute("status", 403);
        model.addAttribute("message", "You don't have permission to access this page.");
        return "error/403";
    }

    // ── IllegalArgumentException (bad input / not found in services) ──────────

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleIllegalArg(IllegalArgumentException ex, Model model) {
        model.addAttribute("status", 400);
        model.addAttribute("message", ex.getMessage());
        return "error/404";   // reuse the generic error page
    }

    // ── Catch-all 500 ─────────────────────────────────────────────────────────

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handle500(Exception ex, Model model) {
        model.addAttribute("status", 500);
        model.addAttribute("message", "Something went wrong on our end. Please try again.");
        return "error/404";   // reuse the generic error page
    }
}