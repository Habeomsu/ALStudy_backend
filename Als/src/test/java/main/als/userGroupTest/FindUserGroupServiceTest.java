package main.als.userGroupTest;

import main.als.apiPayload.code.status.ErrorStatus;
import main.als.apiPayload.exception.GeneralException;
import main.als.group.entity.UserGroup;
import main.als.group.service.UserGroupServiceImpl;
import main.als.page.PostPagingDto;
import main.als.user.entity.User;
import main.als.user.repository.UserRepository;
import main.als.user.service.FindUserGroupService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
public class FindUserGroupServiceTest {

    @InjectMocks
    private FindUserGroupService findUserGroupService;

    @Mock
    private UserRepository userRepository;

    @Test
    @DisplayName("userGroups 테스트 - ( 성공 )")
    public void userGroupsSuccessTes(){

        String username = "test";

        User user = User.builder().username(username).build();

        PostPagingDto.PagingDto pagingDto = PostPagingDto.PagingDto.builder()
                .page(0)
                .size(10)
                .sort("ASC")
                .build();

        Sort sort = Sort.by(Sort.Direction.fromString(pagingDto.getSort()),"id");
        Pageable pageable = PageRequest.of(pagingDto.getPage(), pagingDto.getSize(), sort);

        UserGroup userGroup = UserGroup.builder()
                .user(user)
                .build();

        Page<UserGroup> page = new PageImpl<>(List.of(userGroup), pageable, 1);

        when(userRepository.findByUsername(username)).thenReturn(user);
        when(userRepository.findUserGroupsByUsername(username, pageable)).thenReturn(page);

        Page<UserGroup> result = findUserGroupService.userGroups(username, pagingDto);
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(userGroup, result.getContent().get(0));

    }

    @Test
    @DisplayName("userGroups 테스트 - ( 실패 - 유저 없음 )")
    public void userGroupsFailTest(){

        String username = "test";

        PostPagingDto.PagingDto pagingDto = PostPagingDto.PagingDto.builder()
                .page(0)
                .size(10)
                .sort("ASC")
                .build();

        when(userRepository.findByUsername("test")).thenReturn(null);

        GeneralException exception = assertThrows(GeneralException.class,()->findUserGroupService.userGroups(username,pagingDto));

        assertFalse(exception.getErrorReasonHttpStatus().getIsSuccess());
        assertEquals(ErrorStatus._USERNAME_NOT_FOUND.getCode(), exception.getErrorReasonHttpStatus().getCode());
        assertEquals(ErrorStatus._USERNAME_NOT_FOUND.getMessage(), exception.getErrorReasonHttpStatus().getMessage());
        assertEquals(ErrorStatus._USERNAME_NOT_FOUND.getHttpStatus(), exception.getErrorReasonHttpStatus().getHttpStatus());

    }

}
