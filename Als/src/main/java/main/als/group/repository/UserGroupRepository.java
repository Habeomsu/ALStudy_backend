package main.als.group.repository;

import main.als.group.entity.Group;
import main.als.group.entity.UserGroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserGroupRepository extends JpaRepository<UserGroup,Long> {

    List<UserGroup> findByGroupId(Long groupId);
}

