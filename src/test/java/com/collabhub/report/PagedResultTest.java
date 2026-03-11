package com.collabhub.report;

import com.collabhub.domain.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("PagedResult")
class PagedResultTest {

    @Test
    @DisplayName("should compute page metadata correctly")
    void shouldComputePageMetadata() {
        User alice = new User("Alice", "alice@collabhub.com", "MANAGER","password123!");
        User bob = new User("Bob", "bob@collabhub.com", "DEVELOPER","password123!");

        PagedResult<User> firstPage = new PagedResult<>(List.of(alice, bob), 0, 2, 5);

        assertThat(firstPage.getTotalPages()).isEqualTo(3);
        assertThat(firstPage.hasNext()).isTrue();
        assertThat(firstPage.hasPrevious()).isFalse();
        assertThat(firstPage.getContent()).hasSize(2);
        assertThat(firstPage.getPage()).isZero();
        assertThat(firstPage.getPageSize()).isEqualTo(2);
        assertThat(firstPage.getTotalElements()).isEqualTo(5);
    }

    @Test
    @DisplayName("should expose previous/next boundaries and toString payload")
    void shouldHandleMiddlePage() {
        User carol = new User("Carol", "carol@collabhub.com", "DEVELOPER","password123!");
        PagedResult<User> middlePage = new PagedResult<>(List.of(carol), 1, 2, 5);

        assertThat(middlePage.hasNext()).isTrue();
        assertThat(middlePage.hasPrevious()).isTrue();
        assertThat(middlePage.toString()).contains("page=1/2").contains("size=1").contains("total=5");
    }
}
