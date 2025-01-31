package main.als.user.repository;

import jakarta.transaction.Transactional;
import main.als.user.entity.Refresh;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;

public interface RefreshRepository extends JpaRepository<Refresh, Integer> {
    Boolean existsByRefresh(String refresh);

    @Transactional
    void deleteByRefresh(String refresh);

    @Transactional
    void deleteByUsername(String username);

}
