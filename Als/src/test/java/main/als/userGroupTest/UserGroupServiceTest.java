package main.als.userGroupTest;

import main.als.apiPayload.exception.GeneralException;
import main.als.group.entity.Group;
import main.als.group.entity.UserGroup;
import main.als.group.repository.GroupRepository;
import main.als.group.repository.UserGroupRepository;
import main.als.group.service.UserGroupService;
import main.als.group.service.UserGroupServiceImpl;
import main.als.page.PostPagingDto;
import main.als.user.converter.UserConverter;
import main.als.user.dto.UserDto;
import main.als.user.entity.Role;
import main.als.user.entity.User;
import main.als.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserGroupServiceTest {

    @InjectMocks
    private UserGroupServiceImpl userGroupService;

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private  BCryptPasswordEncoder bCryptPasswordEncoder;

    @Mock
    private  UserGroupRepository userGroupRepository;

    BigDecimal amount  = BigDecimal.valueOf(1000);
    LocalDateTime now = LocalDateTime.now();

    @Test
    @DisplayName("유저그룹 가입 테스트 - (성공)")
    public void joinUserGroupSuccessTest(){

        Long groupId = 1L;
        String password = "password";
        String username = "username";

        Group group = Group.builder()
                .name("name")
                .password("password")
                .depositAmount(amount)
                .leader("leader")
                .createdAt(now)
                .deadline(now.plusDays(1))
                .studyEndDate(now.plusDays(2))
                .build();

        User user = User.builder()
                .username("username")
                .password("password")
                .role(Role.ROLE_USER)
                .customerId("customerId")
                .build();

        when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));
        when(userRepository.findByUsername(username)).thenReturn(user);
        when(bCryptPasswordEncoder.matches(password, group.getPassword())).thenReturn(true);

        userGroupService.joinUserGroup(groupId,password,username);

        verify(userGroupRepository,times(1)).save(any(UserGroup.class));
        assertEquals(1, user.getUserGroups().size());
        assertEquals(1, group.getUserGroups().size());

    }

    @Test
    @DisplayName("유저그룹 가입 테스트 - (실패 그룹 없음)")
    public void joinUserGroupFailTest1(){

        Long groupId = 1L;
        String password = "password";
        String username = "username";

        when(groupRepository.findById(groupId)).thenReturn(Optional.empty());

        GeneralException exception = assertThrows(GeneralException.class,()->userGroupService.joinUserGroup(groupId,password,username));

        assertEquals(false,exception.getErrorReasonHttpStatus().getIsSuccess());
        assertEquals("GROUP400_2",exception.getErrorReasonHttpStatus().getCode());
        assertEquals("그룹이 존재하지 않습니다.",exception.getErrorReasonHttpStatus().getMessage());
        assertEquals(HttpStatus.NOT_FOUND,exception.getErrorReasonHttpStatus().getHttpStatus());

    }

    @Test
    @DisplayName("유저그룹 가입 테스트 - (실패 사용자 없음)")
    public void joinUserGroupFailTest2(){

        Long groupId = 1L;
        String password = "password";
        String username = "username";

        Group group = Group.builder()
                .name("name")
                .password("password")
                .depositAmount(amount)
                .leader("leader")
                .createdAt(now)
                .deadline(now.plusDays(1))
                .studyEndDate(now.plusDays(2))
                .build();

        when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));
        when(userRepository.findByUsername(username)).thenReturn(null);

        GeneralException exception = assertThrows(GeneralException.class,()->userGroupService.joinUserGroup(groupId,password,username));

        assertEquals(false,exception.getErrorReasonHttpStatus().getIsSuccess());
        assertEquals("USER400_2",exception.getErrorReasonHttpStatus().getCode());
        assertEquals("회원가입된 아이디가 아닙니다.",exception.getErrorReasonHttpStatus().getMessage());
        assertEquals(HttpStatus.NOT_FOUND,exception.getErrorReasonHttpStatus().getHttpStatus());

    }

    @Test
    @DisplayName("유저그룹 가입 테스트 - (실패 비밀번호 불일치)")
    public void joinUserGroupFailTest3(){

        Long groupId = 1L;
        String password = "password";
        String username = "username";

        Group group = Group.builder()
                .name("name")
                .password("password")
                .depositAmount(amount)
                .leader("leader")
                .createdAt(now)
                .deadline(now.plusDays(1))
                .studyEndDate(now.plusDays(2))
                .build();

        User user = User.builder()
                .username("username")
                .password("password")
                .role(Role.ROLE_USER)
                .customerId("customerId")
                .build();

        when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));
        when(userRepository.findByUsername(username)).thenReturn(user);
        when(bCryptPasswordEncoder.matches(password, group.getPassword())).thenReturn(false);

        GeneralException exception = assertThrows(GeneralException.class,()->userGroupService.joinUserGroup(groupId,password,username));

        assertEquals(false,exception.getErrorReasonHttpStatus().getIsSuccess());
        assertEquals("GROUP400_3",exception.getErrorReasonHttpStatus().getCode());
        assertEquals("그룹 비밀번호가 일치하지 않습니다.",exception.getErrorReasonHttpStatus().getMessage());
        assertEquals(HttpStatus.BAD_REQUEST,exception.getErrorReasonHttpStatus().getHttpStatus());

    }

    @Test
    @DisplayName("유저그룹 가입 테스트 - (실패 마감일 초과)")
    public void joinUserGroupFailTest4(){

        Long groupId = 1L;
        String password = "password";
        String username = "username";

        Group group = Group.builder()
                .name("name")
                .password("password")
                .depositAmount(amount)
                .leader("leader")
                .createdAt(now.minusDays(3))
                .deadline(now.minusDays(2))
                .studyEndDate(now.minusDays(1))
                .build();

        User user = User.builder()
                .username("username")
                .password("password")
                .role(Role.ROLE_USER)
                .customerId("customerId")
                .build();

        when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));
        when(userRepository.findByUsername(username)).thenReturn(user);
        when(bCryptPasswordEncoder.matches(password, group.getPassword())).thenReturn(true);

        GeneralException exception = assertThrows(GeneralException.class,()->userGroupService.joinUserGroup(groupId,password,username));

        assertEquals(false,exception.getErrorReasonHttpStatus().getIsSuccess());
        assertEquals("USERGROUP400_1",exception.getErrorReasonHttpStatus().getCode());
        assertEquals("모집기간이 지났습니다.",exception.getErrorReasonHttpStatus().getMessage());
        assertEquals(HttpStatus.BAD_REQUEST,exception.getErrorReasonHttpStatus().getHttpStatus());

    }

    @Test
    @DisplayName("유저그룹 가입 테스트 - (실패 이미 그룹 존재)")
    public void joinUserGroupFailTest5(){

        Long groupId = 1L;
        String password = "password";
        String username = "username";

        Group group = Group.builder()
                .id(groupId)
                .name("name")
                .password("password")
                .depositAmount(amount)
                .leader("leader")
                .createdAt(now)
                .deadline(now.plusDays(2))
                .studyEndDate(now.plusDays(3))
                .build();

        User user = User.builder()
                .username("username")
                .password("password")
                .role(Role.ROLE_USER)
                .customerId("customerId")
                .build();

        UserGroup userGroup = UserGroup.builder()
                .user(user)
                .group(group)
                .userDepositAmount(BigDecimal.ZERO)
                .refunded(false)
                .charged(false)
                .paymentKey(null)
                .build();

        user.getUserGroups().add(userGroup);

        when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));
        when(userRepository.findByUsername(username)).thenReturn(user);
        when(bCryptPasswordEncoder.matches(password, group.getPassword())).thenReturn(true);

        GeneralException exception = assertThrows(GeneralException.class,()->userGroupService.joinUserGroup(groupId,password,username));

        assertEquals(false,exception.getErrorReasonHttpStatus().getIsSuccess());
        assertEquals("USERGROUP400_2",exception.getErrorReasonHttpStatus().getCode());
        assertEquals("이미 그룹에 포함된 사용자입니다.",exception.getErrorReasonHttpStatus().getMessage());
        assertEquals(HttpStatus.BAD_REQUEST,exception.getErrorReasonHttpStatus().getHttpStatus());

    }

    @Test
    @DisplayName("그룹아이디로 사용자 가지고 오기 - (성공)")
    public void getUsersByGroupIdSuccessTest(){

        Long groupId = 1L;
        int page = 1;
        int size = 10;
        String sort = "ASC";

        PostPagingDto.PagingDto paingDto = PostPagingDto.PagingDto.builder()
                .page(page)
                .size(size)
                .sort(sort)
                .build();

        Sort sorts = Sort.by(Sort.Direction.fromString(paingDto.getSort()),"userDepositAmount");
        Pageable pageable = PageRequest.of(paingDto.getPage(), paingDto.getSize(), sorts);

        List<UserGroup> userGroups = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            userGroups.add(UserGroup.builder()
                    .user(User.builder().username("user" + i).build())
                    .userDepositAmount(amount)
                    .build());
        }

        Page<UserGroup> userGroupPage = new PageImpl<>(userGroups);

        when(userGroupRepository.findByGroupId(eq(groupId), any(Pageable.class)))
                .thenReturn(userGroupPage);

        UserDto.SearchUsers expectedResult = UserDto.SearchUsers.builder()
                .usernameDtos(List.of())
                .isFirst(true)
                .isLast(false)
                .listSize(1)
                .totalElements(3)
                .build();

        try (MockedStatic<UserConverter> mock = Mockito.mockStatic(UserConverter.class)) {
            mock.when(() -> UserConverter.toUserDtos(userGroupPage)).thenReturn(expectedResult);

            UserDto.SearchUsers result = userGroupService.getUsersByGroupId(groupId, paingDto);

            assertNotNull(result);
            assertTrue(result.isFirst());
            assertFalse(result.isLast());
            assertEquals(1, result.getListSize());
            assertEquals(3, result.getTotalElements());
            mock.verify(() -> UserConverter.toUserDtos(userGroupPage), times(1));
        }

    }

    @Test
    @DisplayName("그룹아이디로 사용자 가지고 오기 - (실패 그룹이 존재하지 않음)")
    public void getUsersByGroupIdFailTest(){

        Long groupId = 1L;
        int page = 1;
        int size = 10;
        String sort = "ASC";

        PostPagingDto.PagingDto paingDto = PostPagingDto.PagingDto.builder()
                .page(page)
                .size(size)
                .sort(sort)
                .build();

        Sort sorts = Sort.by(Sort.Direction.fromString(paingDto.getSort()),"userDepositAmount");
        Pageable pageable = PageRequest.of(paingDto.getPage(), paingDto.getSize(), sorts);

        when(userGroupRepository.findByGroupId(eq(groupId), any(Pageable.class))).thenReturn(null);

        GeneralException exception = assertThrows(GeneralException.class,()->userGroupService.getUsersByGroupId(groupId, paingDto));

        assertEquals(false,exception.getErrorReasonHttpStatus().getIsSuccess());
        assertEquals("GROUP400_2",exception.getErrorReasonHttpStatus().getCode());
        assertEquals("그룹이 존재하지 않습니다.",exception.getErrorReasonHttpStatus().getMessage());
        assertEquals(HttpStatus.NOT_FOUND,exception.getErrorReasonHttpStatus().getHttpStatus());

    }

    @Test
    @DisplayName("그룹 탈퇴 테스트 - (성공)")
    public void resignGroupSuccessTest(){

        Long groupId = 1L;
        String username = "username";

        Group group = Group.builder()
                .name("name")
                .password("password")
                .depositAmount(amount)
                .leader("leader")
                .createdAt(now)
                .deadline(now.plusDays(1))
                .studyEndDate(now.plusDays(2))
                .build();

        User user = User.builder()
                .username("username")
                .password("password")
                .role(Role.ROLE_USER)
                .customerId("customerId")
                .build();

        UserGroup userGroup = UserGroup.builder()
                .user(user)
                .group(group)
                .userDepositAmount(amount)
                .charged(true)
                .refunded(true)
                .paymentKey("paymentKey")
                .build();

        when(userGroupRepository.findByGroupIdAndUserUsername(groupId, username)).thenReturn(Optional.of(userGroup));

        userGroupService.resignGroup(groupId, username);

        verify(userGroupRepository,times(1)).delete(userGroup);

    }

    @Test
    @DisplayName("그룹 탈퇴 테스트 - (실패) 유저 그룹이 존재 x")
    public void resignGroupFailTest(){
        Long groupId = 1L;
        String username = "username";

        when(userGroupRepository.findByGroupIdAndUserUsername(groupId,username)).thenReturn(Optional.empty());

        GeneralException exception = assertThrows(GeneralException.class,()->userGroupService.resignGroup(groupId, username));

        assertEquals(false,exception.getErrorReasonHttpStatus().getIsSuccess());
        assertEquals("USERGROUP400_4",exception.getErrorReasonHttpStatus().getCode());
        assertEquals("그룹이 존재하지 않습니다.",exception.getErrorReasonHttpStatus().getMessage());
        assertEquals(HttpStatus.BAD_REQUEST,exception.getErrorReasonHttpStatus().getHttpStatus());

    }

    @Test
    public void checkChargedSuccessTest(){

        UserGroup userGroup1 = UserGroup.builder()
                .charged(false)
                .user(User.builder().username("username").build())
                .group(Group.builder().deadline(now.minusDays(1)).build())
                .build();

        UserGroup userGroup2 = UserGroup.builder()
                .charged(false)
                .user(User.builder().username("username").build())
                .group(Group.builder().deadline(now.minusDays(1)).build())
                .build();

        UserGroup userGroup3 = UserGroup.builder()
                .charged(false)
                .user(User.builder().username("username").build())
                .group(Group.builder().deadline(now.minusDays(1)).build())
                .build();

        UserGroup userGroup4 = UserGroup.builder()
                .charged(false)
                .user(User.builder().username("username").build())
                .group(Group.builder().deadline(now.plusDays(1)).build())
                .build();



        List<UserGroup> userGroups = Arrays.asList(userGroup1, userGroup2, userGroup3, userGroup4);

        when(userGroupRepository.findByChargedFalse()).thenReturn(userGroups);

        userGroupService.checkCharged();

        verify(userGroupRepository,times(3)).delete(any(UserGroup.class));
        verify(userGroupRepository).delete(userGroup1);
        verify(userGroupRepository).delete(userGroup2);
        verify(userGroupRepository).delete(userGroup3);
        verify(userGroupRepository,never()).delete(userGroup4);




    }






}
