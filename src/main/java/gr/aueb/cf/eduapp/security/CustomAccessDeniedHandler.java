package gr.aueb.cf.eduapp.security;

import gr.aueb.cf.eduapp.dto.ErrorResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

/**
 * Authorization Filter. Returns 403.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;  // Spring Boot gets this from Jackson

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException)
            throws IOException {

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        log.warn("Access denied: user={}, uri={}", username, request.getRequestURI()); // Set the response status and content type
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json; charset=UTF-8");

        response.getWriter().write(
                objectMapper.writeValueAsString(
                        new ErrorResponseDTO("ACCESS_DENIED", "User is not allowed to access this route.")
                )
        );
    }
}

// Write a custom JSON response with the collected information
//        String jsonResponse = "{\"code\": \"UserNotAuthorized\", \"description\": \"User is not allowed to access this route.\"}";
//        response.getWriter().write(jsonResponse);
