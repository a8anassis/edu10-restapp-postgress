package gr.aueb.cf.eduapp.security;

import tools.jackson.databind.ObjectMapper;
import gr.aueb.cf.eduapp.dto.ErrorResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Authentication Filter. Returns 401.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException e) throws IOException {

        Object jwtErrorCode = request.getAttribute("auth_error_code");
        Object jwtErrorMessage = request.getAttribute("auth_error");

        String errorCode;
        String message;

        // αν το προηγούμενο φίλτρο έφτασε στο try/catch
        if (jwtErrorCode != null) {
            errorCode = (String) jwtErrorCode;
            message = (String) jwtErrorMessage;
        // αν το προηγούμενο φίλτρο ήταν στην περίπτωση  if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
        // δεν υπάρχει καθόλου authorization header και τα attributes εδώ είναι null
        } else {
            errorCode = "UNAUTHORIZED";
            message = "Authentication required";
        }

        log.warn("Unauthenticated request: uri={}, code={}, cause={}",
                request.getRequestURI(), errorCode, e.getMessage());

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json; charset=UTF-8");
        response.getWriter().write(
                objectMapper.writeValueAsString(
                        new ErrorResponseDTO(errorCode, message)
                )
        );
    }
}

// Set the response status to 401 Unauthorized
//        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//        response.setContentType("application/json; charset=UTF-8");
//
//        String json = "{\"code\": \"UserNotAuthenticated\", \"description\": \"User needs to authenticate in order to access this route\"}";
//        response.getWriter().write(json);
