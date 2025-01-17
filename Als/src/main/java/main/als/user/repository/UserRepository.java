package main.als.user.repository;

import main.als.group.entity.UserGroup;
import main.als.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByUsername(String username);
    User findByUsername(String username);
    void deleteByUsername(String username);

    @Query("SELECT ug FROM UserGroup ug WHERE ug.user.username = :username")
    Page<UserGroup> findUserGroupsByUsername(@Param("username") String username, Pageable pageable);
}
