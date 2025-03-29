package main.als.refreshtest;

import jakarta.transaction.Transactional;
import main.als.user.entity.Refresh;
import main.als.user.repository.RefreshRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class RefreshRepositoryTest {

    @Autowired
    private RefreshRepository refreshRepository;

    @Test
    @Transactional
    @DisplayName("리프레쉬 존재 테스트")
    public void existByRefreshTest(){

        Refresh refresh = Refresh.builder()
                .username("test1")
                .refresh("refresh1")
                .expiration("expiration1")
                .build();

        refreshRepository.save(refresh);

        Boolean exists = refreshRepository.existsByRefresh(refresh.getRefresh());
        assertTrue(exists);

    }

    @Test
    @Transactional
    @DisplayName("리프레쉬 삭제 테스트")
    public void deleteByUsernameTest(){

        Refresh refresh = Refresh.builder()
                .username("test1")
                .refresh("refresh1")
                .expiration("expiration1")
                .build();

        refreshRepository.save(refresh);

        refreshRepository.deleteByUsername("test1");

        assertFalse(refreshRepository.existsByRefresh(refresh.getRefresh()));


    }



}
