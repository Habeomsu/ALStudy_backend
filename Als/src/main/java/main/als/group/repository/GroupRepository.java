package main.als.group.repository;

import main.als.group.entity.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface GroupRepository extends JpaRepository<Group,Long> {

    Group save(Group group);
    List<Group> findAll();
    void deleteById(Long id);
    Optional<Group> findById(Long id);
    // 만료된 그룹 삭제
    @Query("SELECT g FROM Group g WHERE g.studyEndDate < :currentDate")
    List<Group> findExpiredGroups(@Param("currentDate") LocalDateTime currentDate);
}
