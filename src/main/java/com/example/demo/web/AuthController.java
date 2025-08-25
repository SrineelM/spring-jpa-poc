package com.example.demo.web;

import com.example.demo.domain.User;
import com.example.demo.dto.LoginDto;
import com.example.demo.dto.SignUpDto;
import com.example.demo.event.UserRegisteredEvent;
import com.example.demo.exception.BusinessRuleViolationException;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.JwtTokenProvider;
import io.micrometer.core.annotation.Counted;
import io.micrometer.core.annotation.Timed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Enhanced authentication controller with comprehensive security, validation, and monitoring
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );
    
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
        "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$"
    );

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final ApplicationEventPublisher applicationEventPublisher;

    public AuthController(AuthenticationManager authenticationManager,
                          UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          JwtTokenProvider tokenProvider,
                          ApplicationEventPublisher applicationEventPublisher) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    /**
     * Enhanced login endpoint with comprehensive security and monitoring
     */
    @PostMapping("/login")
    @Timed(value = "auth.login.duration", description = "Time taken to authenticate user")
    @Counted(value = "auth.login.attempts", description = "Number of login attempts")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginDto loginDto) {
        
        logger.info("Login attempt for email: {}", loginDto.getEmail());
        
        try {
            // Input validation
            validateLoginInput(loginDto);
            
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginDto.getEmail(), loginDto.getPassword())
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String token = tokenProvider.generateToken(authentication);
            
            logger.info("Successful login for user: {}", loginDto.getEmail());
            
            Map<String, Object> response = new HashMap<>();
            response.put("accessToken", token);
            response.put("tokenType", "Bearer");
            response.put("expiresIn", 86400); // 24 hours in seconds
            
            return ResponseEntity.ok(response);
            
        } catch (BadCredentialsException e) {
            logger.warn("Failed login attempt for email: {} - Invalid credentials", loginDto.getEmail());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(createErrorResponse("INVALID_CREDENTIALS", "Invalid email or password"));
                    
        } catch (BusinessRuleViolationException e) {
            logger.warn("Failed login attempt for email: {} - Validation error: {}", 
                    loginDto.getEmail(), e.getMessage());
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("VALIDATION_ERROR", e.getMessage()));
                    
        } catch (Exception e) {
            logger.error("Unexpected error during login for email: {}", loginDto.getEmail(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("LOGIN_ERROR", "An unexpected error occurred"));
        }
    }

    /**
     * Enhanced registration endpoint with comprehensive validation
     */
    @PostMapping("/register")
    @Timed(value = "auth.register.duration", description = "Time taken to register user")
    @Counted(value = "auth.register.attempts", description = "Number of registration attempts")
    public ResponseEntity<?> registerUser(@RequestBody SignUpDto signUpDto) {
        
        logger.info("Registration attempt for email: {}", signUpDto.getEmail());
        
        try {
            // Comprehensive input validation
            validateRegistrationInput(signUpDto);
            
            // Check if email already exists
            if (userRepository.findByEmailWithProfile(signUpDto.getEmail()).isPresent()) {
                logger.warn("Registration failed - Email already exists: {}", signUpDto.getEmail());
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(createErrorResponse("EMAIL_EXISTS", "Email address already in use"));
            }

            // Create new user
            User user = new User();
            user.setName(signUpDto.getName().trim());
            user.setEmail(signUpDto.getEmail().toLowerCase().trim());
            user.setPassword(passwordEncoder.encode(signUpDto.getPassword()));

            User savedUser = userRepository.save(user);
            
            // Publish registration event for async processing
            applicationEventPublisher.publishEvent(new UserRegisteredEvent(this, savedUser));
            
            logger.info("User registered successfully with email: {}", savedUser.getEmail());
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "User registered successfully");
            response.put("userId", savedUser.getId());
            response.put("email", savedUser.getEmail());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (BusinessRuleViolationException e) {
            logger.warn("Registration validation failed for email: {} - {}", 
                    signUpDto.getEmail(), e.getMessage());
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("VALIDATION_ERROR", e.getMessage()));
                    
        } catch (Exception e) {
            logger.error("Unexpected error during registration for email: {}", signUpDto.getEmail(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("REGISTRATION_ERROR", "An unexpected error occurred"));
        }
    }

    /**
     * Password validation endpoint
     */
    @PostMapping("/validate-password")
    public ResponseEntity<Map<String, Object>> validatePassword(@RequestBody Map<String, String> request) {
        String password = request.get("password");
        
        Map<String, Object> validation = new HashMap<>();
        validation.put("isValid", isValidPassword(password));
        validation.put("requirements", getPasswordRequirements());
        
        return ResponseEntity.ok(validation);
    }

    private void validateLoginInput(LoginDto loginDto) {
        if (loginDto.getEmail() == null || loginDto.getEmail().trim().isEmpty()) {
            throw new BusinessRuleViolationException("Email is required", "EMAIL_REQUIRED", null);
        }
        
        if (loginDto.getPassword() == null || loginDto.getPassword().isEmpty()) {
            throw new BusinessRuleViolationException("Password is required", "PASSWORD_REQUIRED", null);
        }
        
        if (!EMAIL_PATTERN.matcher(loginDto.getEmail()).matches()) {
            throw new BusinessRuleViolationException("Invalid email format", "INVALID_EMAIL", loginDto.getEmail());
        }
    }

    private void validateRegistrationInput(SignUpDto signUpDto) {
        // Name validation
        if (signUpDto.getName() == null || signUpDto.getName().trim().length() < 2) {
            throw new BusinessRuleViolationException("Name must be at least 2 characters long", "INVALID_NAME", signUpDto.getName());
        }
        
        if (signUpDto.getName().trim().length() > 50) {
            throw new BusinessRuleViolationException("Name cannot exceed 50 characters", "NAME_TOO_LONG", signUpDto.getName());
        }
        
        // Email validation
        if (signUpDto.getEmail() == null || signUpDto.getEmail().trim().isEmpty()) {
            throw new BusinessRuleViolationException("Email is required", "EMAIL_REQUIRED", null);
        }
        
        if (!EMAIL_PATTERN.matcher(signUpDto.getEmail()).matches()) {
            throw new BusinessRuleViolationException("Invalid email format", "INVALID_EMAIL", signUpDto.getEmail());
        }
        
        // Password validation
        if (!isValidPassword(signUpDto.getPassword())) {
            throw new BusinessRuleViolationException("Password does not meet security requirements", "WEAK_PASSWORD", null);
        }
    }

    private boolean isValidPassword(String password) {
        return password != null && 
               password.length() >= 8 && 
               PASSWORD_PATTERN.matcher(password).matches();
    }

    private Map<String, String> getPasswordRequirements() {
        Map<String, String> requirements = new HashMap<>();
        requirements.put("minLength", "8 characters minimum");
        requirements.put("uppercase", "At least one uppercase letter");
        requirements.put("lowercase", "At least one lowercase letter");
        requirements.put("digit", "At least one digit");
        requirements.put("special", "At least one special character (@$!%*?&)");
        return requirements;
    }

    private Map<String, String> createErrorResponse(String errorCode, String message) {
        Map<String, String> error = new HashMap<>();
        error.put("errorCode", errorCode);
        error.put("message", message);
        error.put("timestamp", String.valueOf(System.currentTimeMillis()));
        return error;
    }
}
