package main.als.usertest;

import jakarta.transaction.Transactional;
import main.als.group.entity.UserGroup;
import main.als.user.entity.Role;
import main.als.user.entity.User;
import main.als.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    @Transactional
    @DisplayName("사용자 존재 테스트")
    public void testExistsByUsername() {
        User user = User.builder()
                .username("username")
                .password("password")
                .role(Role.ROLE_USER)
                .customerId("customerId")
                .build();
        userRepository.save(user);

        boolean exists = userRepository.existsByUsername("username");
        assertTrue(exists);
    }

    @Test
    @Transactional
    @DisplayName("사용자 이름을 이용해서 사용자 찾기 테스트")
    public void FindByUsernameTest() {
        User user = User.builder()
                .username("username")
                .password("password")
                .role(Role.ROLE_USER)
                .customerId("customerId")
                .build();
        userRepository.save(user);
        User user1 = userRepository.findByUsername("username");
        assertNotNull(user1);
        assertEquals("username", user1.getUsername());
        assertEquals("password", user1.getPassword());
        assertEquals(Role.ROLE_USER, user1.getRole());
    }

    @Test
    @Transactional
    @DisplayName("사용자 이름을 이용해서 삭제하기")
    public void DeleteByUsernameTest(){
        User user = User.builder()
                .username("username")
                .password("password")
                .role(Role.ROLE_USER)
                .customerId("customerId")
                .build();
        userRepository.save(user);

        userRepository.deleteByUsername("username");
        assertFalse(userRepository.existsByUsername("username"));
    }


    @Test
    @Transactional
    @DisplayName("사용자 이름으로 유저그룹 찾기")
    public void FindUserGroupByUsernameTest(){

        // TODO - 그룹, 유저그룹이 필요함 추후에 하기



    }
}
