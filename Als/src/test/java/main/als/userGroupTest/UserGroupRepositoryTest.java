package main.als.userGroupTest;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import main.als.group.entity.Group;
import main.als.group.entity.UserGroup;
import main.als.group.repository.GroupRepository;
import main.als.group.repository.UserGroupRepository;
import main.als.user.entity.Role;
import main.als.user.entity.User;
import main.als.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@ActiveProfiles("test")
public class UserGroupRepositoryTest {

    @Autowired
    private UserGroupRepository userGroupRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GroupRepository groupRepository;

    private User savedUser;
    private Group savedGroup;

    @BeforeEach
    void setUp() {
        savedUser = userRepository.save(User.builder()
                .username("testUser")
                .password("password")
                .role(Role.ROLE_USER)
                .customerId("customerId")
                .build());

        savedGroup = groupRepository.save(Group.builder()
                .name("testGroup")
                .password("groupPwd")
                .leader("testUser")
                .depositAmount(BigDecimal.valueOf(1000))
                .createdAt(LocalDateTime.now())
                .deadline(LocalDateTime.now().plusDays(1))
                .studyEndDate(LocalDateTime.now().plusDays(3))
                .build());
    }

    @Test
    @DisplayName("그룹아이디로 유저그룹 찾기")
    @Transactional
    public void findByGroupIdTest(){

        UserGroup userGroup = UserGroup.builder()
                .user(savedUser)
                .group(savedGroup)
                .userDepositAmount(BigDecimal.valueOf(1000))
                .refunded(false)
                .charged(true)
                .paymentKey("paymentKey")
                .build();

        UserGroup ug = userGroupRepository.save(userGroup);

        entityManager.flush();
        entityManager.clear();


        List<UserGroup> result = userGroupRepository.findByGroupId(savedGroup.getId());
        assertEquals(1, result.size());

    }

    @Test
    @DisplayName("그룹아이디로 유저그룹 찾기-(페이징 처리)")
    @Transactional
    public void findByGroupIdByPageTest(){

        userGroupRepository.save(UserGroup.builder()
                .user(savedUser)
                .group(savedGroup)
                .charged(true)
                .refunded(false)
                .paymentKey("paymentKey")
                .build());

        entityManager.flush();
        entityManager.clear();

        Page<UserGroup> page = userGroupRepository.findByGroupId(savedGroup.getId(), PageRequest.of(0, 10));

        assertEquals(1, page.getTotalElements());

    }

    @Test
    @DisplayName("그룹아이디와 사용자 이름으로 존재여부 파악하기 테스트")
    @Transactional
    public void existsByGroupIdAndUserUsernameTest(){

        UserGroup userGroup = UserGroup.builder()
                .user(savedUser)
                .group(savedGroup)
                .charged(true)
                .refunded(false)
                .paymentKey("paymentKey")
                .build();

        userGroupRepository.save(userGroup);

        entityManager.flush();
        entityManager.clear();

        assertTrue(userGroupRepository.existsByGroupIdAndUserUsername(savedGroup.getId(),savedUser.getUsername()));
    }

    @Test
    @DisplayName("그룹아이디와 사용자 이름으로 존재여부 파악하기 테스트 - (Optional)")
    @Transactional
    public void existsByGroupIdAndUserUsernameByOptionalTest(){

        UserGroup userGroup = UserGroup.builder()
                .user(savedUser)
                .group(savedGroup)
                .charged(true)
                .refunded(false)
                .paymentKey("paymentKey")
                .build();

        userGroupRepository.save(userGroup);

        entityManager.flush();
        entityManager.clear();

        Optional<UserGroup> result = userGroupRepository.findByGroupIdAndUserUsername(savedGroup.getId(), savedUser.getUsername());
        assertTrue(result.isPresent());
        assertEquals(userGroup.getId(), result.get().getId());
    }

    @Test
    @DisplayName("충전안한 유저그룹 찾기 테스트")
    @Transactional
    public void findByChargedFalseTest(){

        UserGroup userGroup1 = UserGroup.builder()
                .user(savedUser)
                .group(savedGroup)
                .charged(false)
                .refunded(false)
                .paymentKey("paymentKey")
                .build();

        UserGroup userGroup2 = UserGroup.builder()
                .user(savedUser)
                .group(savedGroup)
                .charged(false)
                .refunded(false)
                .paymentKey("paymentKey")
                .build();

        userGroupRepository.save(userGroup1);
        userGroupRepository.save(userGroup2);

        entityManager.flush();
        entityManager.clear();

        List<UserGroup> result = userGroupRepository.findByChargedFalse();

        assertEquals(2, result.size());

    }


}
