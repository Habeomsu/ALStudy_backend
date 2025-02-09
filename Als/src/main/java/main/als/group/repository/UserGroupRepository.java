package main.als.group.repository;

import main.als.group.entity.Group;
import main.als.group.entity.UserGroup;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;


public interface UserGroupRepository extends JpaRepository<UserGroup,Long> {

    List<UserGroup> findByGroupId(Long groupId);
    Page<UserGroup> findByGroupId(Long groupId, Pageable pageable);
    boolean existsByGroupIdAndUserUsername(Long groupId, String username);
    Optional<UserGroup> findByGroupIdAndUserUsername(Long groupId, String username);
    Optional<UserGroup> findById(Long userGroupId);
    List<UserGroup> findByChargedFalse();
}


