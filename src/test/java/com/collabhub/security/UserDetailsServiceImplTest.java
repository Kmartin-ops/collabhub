package com.collabhub.security;

import com.collabhub.domain.User;
import com.collabhub.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserDetailsServiceImpl")
class UserDetailsServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    @Test
    @DisplayName("loadUserByUsername should map user to Spring UserDetails")
    void shouldLoadUserDetails() {
        User user = new User("Alice", "alice@collabhub.com", "MANAGER","password123!");
        user.setPasswordHash("hashed-password");

        when(userRepository.findByEmail("alice@collabhub.com")).thenReturn(Optional.of(user));

        UserDetails details = userDetailsService.loadUserByUsername("alice@collabhub.com");

        assertThat(details.getUsername()).isEqualTo("alice@collabhub.com");
        assertThat(details.getPassword()).isEqualTo("hashed-password");
        assertThat(details.getAuthorities()).extracting("authority").containsExactly("ROLE_MANAGER");
    }

    @Test
    @DisplayName("loadUserByUsername should throw when user is missing")
    void shouldThrowWhenMissing() {
        when(userRepository.findByEmail("unknown@collabhub.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userDetailsService.loadUserByUsername("unknown@collabhub.com"))
                .isInstanceOf(UsernameNotFoundException.class).hasMessageContaining("unknown@collabhub.com");
    }
}
