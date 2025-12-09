package com.openclassrooms.starterjwt.security.services;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

class UserDetailsImplTest {

    @Test
    void equals_shouldReturnTrue_whenIdsAreEqual() {
        // Arrange
        UserDetailsImpl user1 = UserDetailsImpl.builder()
                .id(1L)
                .username("user1@test.com")
                .firstName("John")
                .lastName("Doe")
                .password("pwd")
                .admin(true)
                .build();

        UserDetailsImpl user2 = UserDetailsImpl.builder()
                .id(1L)
                .username("user2@test.com") // peu importe
                .firstName("Jane")
                .lastName("Smith")
                .password("pwd2")
                .admin(false)
                .build();

        // Act
        boolean equals = user1.equals(user2);

        // Assert
        assertThat(equals).isTrue();
    }

    @Test
    void equals_shouldReturnFalse_whenIdsAreDifferent() {
        // Arrange
        UserDetailsImpl user1 = UserDetailsImpl.builder()
                .id(1L)
                .username("user1@test.com")
                .build();

        UserDetailsImpl user2 = UserDetailsImpl.builder()
                .id(2L)
                .username("user2@test.com")
                .build();

        // Act
        boolean equals = user1.equals(user2);

        // Assert
        assertThat(equals).isFalse();
    }

    @Test
    void equals_shouldReturnFalse_whenOtherIsNullOrDifferentClass() {
        // Arrange
        UserDetailsImpl user = UserDetailsImpl.builder()
                .id(1L)
                .username("user@test.com")
                .build();

        // Act & Assert
        assertThat(user)
                .isNotEqualTo(null)
                .isNotEqualTo("some string");
    }

    @Test
    void accountFlags_shouldAllBeTrue() {
        // Arrange
        UserDetailsImpl user = UserDetailsImpl.builder()
                .id(1L)
                .username("user@test.com")
                .password("pwd")
                .admin(false)
                .build();

        // Act
        boolean nonExpired = user.isAccountNonExpired();
        boolean nonLocked = user.isAccountNonLocked();
        boolean credentialsNonExpired = user.isCredentialsNonExpired();
        boolean enabled = user.isEnabled();

        // Assert
        assertThat(nonExpired).isTrue();
        assertThat(nonLocked).isTrue();
        assertThat(credentialsNonExpired).isTrue();
        assertThat(enabled).isTrue();
    }

    @Test
    void getAuthorities_shouldReturnEmptyCollection() {
        // Arrange
        UserDetailsImpl user = UserDetailsImpl.builder()
                .id(1L)
                .username("user@test.com")
                .password("pwd")
                .admin(false)
                .build();

        // Act
        Collection<? extends GrantedAuthority> authorities = user.getAuthorities();

        // Assert
        assertThat(authorities)
                .isNotNull()
                .isEmpty();
    }

    @Test
    void getters_shouldReturnValuesProvidedToBuilder() {
        // Arrange
        Long id = 1L;
        String username = "user@test.com";
        String firstName = "John";
        String lastName = "Doe";
        String password = "pwd";
        Boolean admin = true;

        UserDetailsImpl user = UserDetailsImpl.builder()
                .id(id)
                .username(username)
                .firstName(firstName)
                .lastName(lastName)
                .password(password)
                .admin(admin)
                .build();

        // Act
        Long resultId = user.getId();
        String resultUsername = user.getUsername();
        String resultFirstName = user.getFirstName();
        String resultLastName = user.getLastName();
        String resultPassword = user.getPassword();
        Boolean resultAdmin = user.getAdmin();

        // Assert
        assertThat(resultId).isEqualTo(id);
        assertThat(resultUsername).isEqualTo(username);
        assertThat(resultFirstName).isEqualTo(firstName);
        assertThat(resultLastName).isEqualTo(lastName);
        assertThat(resultPassword).isEqualTo(password);
        assertThat(resultAdmin).isEqualTo(admin);
    }
}
