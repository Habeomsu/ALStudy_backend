package main.als.auth;

import main.als.apiPayload.code.status.ErrorStatus;
import main.als.apiPayload.exception.GeneralException;
import main.als.problem.service.SubmissionServiceImpl;
import main.als.user.dto.CustomUserDetails;
import main.als.user.entity.Role;
import main.als.user.entity.User;
import main.als.user.repository.UserRepository;
import main.als.user.service.CustomUserDetailsService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CustomUserDetailsServiceTest {

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    @Mock
    private UserRepository userRepository;

    @Test
    @DisplayName("loadUserByUsername 테스트 - ( 성공 )")
    public void loadUserByUsernameSuccessTest(){

        String username = "testuser";
        User user = User.builder()
                .id(1L)
                .username(username)
                .password("encoded-password")
                .role(Role.ROLE_USER)
                .build();

        when(userRepository.findByUsername(username)).thenReturn(user);

        // when
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);

        assertNotNull(userDetails);
        assertTrue(userDetails instanceof CustomUserDetails);
        assertEquals(username, userDetails.getUsername());
        assertEquals("encoded-password", userDetails.getPassword());

        verify(userRepository).findByUsername(username);
    }

    @Test
    @DisplayName("loadUserByUsername 테스트 - ( 실패 - 사용자 없음 )")
    public void loadUserByUsernameFailTest(){

        String username = "testuser";

        when(userRepository.findByUsername(username)).thenReturn(null);

        Exception ex = assertThrows(UsernameNotFoundException.class, () ->
                customUserDetailsService.loadUserByUsername(username));

        assertEquals("User not found with username: " + username, ex.getMessage());

    }

}
