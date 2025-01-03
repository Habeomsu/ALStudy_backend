package main.als.usertest;

import jakarta.transaction.Transactional;
import main.als.user.entity.Role;
import main.als.user.entity.User;
import main.als.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    @Transactional
    public void testExistsByUsername() {
        User user = User.builder()
                .username("username")
                .password("password")
                .role(Role.ROLE_USER)
                .build();
        userRepository.save(user);

        boolean exists = userRepository.existsByUsername("username");
        assertTrue(exists);
    }

    @Test
    @Transactional
    public void FindByUsernameTest() {
        User user = User.builder()
                .username("username")
                .password("password")
                .role(Role.ROLE_USER)
                .build();
        userRepository.save(user);
        User user1 = userRepository.findByUsername("username");
        assertNotNull(user1);
        assertEquals("username", user1.getUsername());
        assertEquals("password", user1.getPassword());
        assertEquals(Role.ROLE_USER, user1.getRole());
    }
}
