package main.als.usertest;

import main.als.apiPayload.code.status.ErrorStatus;
import main.als.apiPayload.exception.GeneralException;
import main.als.user.dto.JoinDto;
import main.als.user.entity.Role;
import main.als.user.entity.User;
import main.als.user.repository.RefreshRepository;
import main.als.user.repository.UserRepository;
import main.als.user.service.DeleteService;
import main.als.user.service.JoinService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Mock
    private RefreshRepository refreshRepository;

    @InjectMocks
    private JoinService joinService;

    @InjectMocks
    private DeleteService deleteService;

    @Test
    @DisplayName("회원가입 성공 테스트")
    public void JoinProcessSuccessTest(){

        //given
        JoinDto joinDto = new JoinDto();
        String username = "username";
        String password = "password";
        joinDto.setUsername(username);
        joinDto.setPassword(password);

        //when
        when(userRepository.existsByUsername(username)).thenReturn(false);
        when(bCryptPasswordEncoder.encode("password")).thenReturn("encryptedPassword");

        //when
        joinService.joinProcess(joinDto);

        //then
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();

        assertEquals("username", savedUser.getUsername());
        assertEquals("encryptedPassword", savedUser.getPassword());
        assertEquals(Role.ROLE_USER, savedUser.getRole());
        assertNotNull(savedUser.getCustomerId());

    }

    @DisplayName("회원가입 실패 테스트")
    @Test
    public void JoinProcessFailTest(){
        JoinDto joinDto = new JoinDto();
        String username = "username";
        String password = "password";
        joinDto.setUsername(username);
        joinDto.setPassword(password);

        when(userRepository.existsByUsername(username)).thenReturn(true);

        GeneralException exception = assertThrows(GeneralException.class,()->joinService.joinProcess(joinDto));

        assertEquals(false,exception.getErrorReasonHttpStatus().getIsSuccess());
        assertEquals("USER400_1",exception.getErrorReasonHttpStatus().getCode());
        assertEquals("아이디가 존재합니다.",exception.getErrorReasonHttpStatus().getMessage());
        assertEquals(HttpStatus.BAD_REQUEST,exception.getErrorReasonHttpStatus().getHttpStatus());

    }

    // TODO - refreshRepository
    @Test
    @DisplayName("사용자 삭제 성공 테스트")
    public void DeleteSuccessTest(){
        String username = "username";

        when(userRepository.existsByUsername(username)).thenReturn(true);

        deleteService.deleteUser(username);

        verify(userRepository, times(1)).deleteByUsername(username);
        verify(refreshRepository, times(1)).deleteByUsername(username);

    }

    @Test
    @DisplayName("사용자 삭제 실패 테스트")
    public void DeleteFailTest(){

        String username = "username";

        when(userRepository.existsByUsername(username)).thenReturn(false);

        GeneralException exception = assertThrows(GeneralException.class,()-> deleteService.deleteUser(username));

        assertEquals(false,exception.getErrorReasonHttpStatus().getIsSuccess());
        assertEquals("USER400_2",exception.getErrorReasonHttpStatus().getCode());
        assertEquals("회원가입된 아이디가 아닙니다.",exception.getErrorReasonHttpStatus().getMessage());
        assertEquals(HttpStatus.NOT_FOUND,exception.getErrorReasonHttpStatus().getHttpStatus());

    }



}
