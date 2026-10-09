package com.Singla.Finance_Manager.service;

import com.Singla.Finance_Manager.dto.auth.LoginRequest;
import com.Singla.Finance_Manager.dto.auth.MessageResponse;
import com.Singla.Finance_Manager.dto.auth.RegisterRequest;
import com.Singla.Finance_Manager.dto.auth.RegisterResponse;
import com.Singla.Finance_Manager.entity.User;
import com.Singla.Finance_Manager.exception.DuplicateResourceException;
import com.Singla.Finance_Manager.exception.UnauthorizedException;
import com.Singla.Finance_Manager.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private HttpServletRequest httpRequest;

    @Mock
    private HttpServletResponse httpResponse;

    @Mock
    private HttpSession httpSession;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void testRegisterSuccess() {
        RegisterRequest request = new RegisterRequest("test@example.com", "pass123", "Test User", "+1234567890");

        when(userRepository.existsByUsername(request.getUsername())).thenReturn(false);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("hashedPassword");

        User savedUser = new User("test@example.com", "hashedPassword", "Test User", "+1234567890");
        savedUser.setId(1L);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        RegisterResponse response = userService.register(request);

        assertNotNull(response);
        assertEquals(1L, response.getUserId());
        assertEquals("User registered successfully", response.getMessage());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testRegisterDuplicateUsernameThrowsException() {
        RegisterRequest request = new RegisterRequest("duplicate@example.com", "pass123", "Test User", "+1234567890");
        when(userRepository.existsByUsername(request.getUsername())).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> userService.register(request));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testLoginSuccess() {
        LoginRequest request = new LoginRequest("user@example.com", "pass123");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);

        MessageResponse response = userService.login(request, httpRequest, httpResponse);

        assertNotNull(response);
        assertEquals("Login successful", response.getMessage());
    }

    @Test
    void testLoginInvalidCredentialsThrowsException() {
        LoginRequest request = new LoginRequest("user@example.com", "wrongpass");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThrows(UnauthorizedException.class, () -> userService.login(request, httpRequest, httpResponse));
    }

    @Test
    void testLogoutSuccess() {
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn("user@example.com");
        SecurityContextHolder.setContext(securityContext);

        when(httpRequest.getSession(false)).thenReturn(httpSession);

        MessageResponse response = userService.logout(httpRequest, httpResponse);

        assertEquals("Logout successful", response.getMessage());
        verify(httpSession).invalidate();
    }

    @Test
    void testGetCurrentAuthenticatedUserSuccess() {
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn("user@example.com");
        when(authentication.getName()).thenReturn("user@example.com");
        SecurityContextHolder.setContext(securityContext);

        User user = new User("user@example.com", "pass", "User", "+123");
        user.setId(10L);
        when(userRepository.findByUsername("user@example.com")).thenReturn(Optional.of(user));

        User current = userService.getCurrentAuthenticatedUser();
        assertNotNull(current);
        assertEquals(10L, current.getId());
    }

    @Test
    void testGetCurrentAuthenticatedUserUnauthenticatedThrowsException() {
        assertThrows(UnauthorizedException.class, () -> userService.getCurrentAuthenticatedUser());
    }
}
