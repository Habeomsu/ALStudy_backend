package main.als.group.repository;

import main.als.group.entity.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface GroupRepository extends JpaRepository<Group,Long> {

    Group save(Group group);
    List<Group> findAll();
    void deleteById(Long id);

    @Query("SELECT g FROM Group g WHERE g.studyEndDate < :currentDate")
    List<Group> findExpiredGroups(@Param("currentDate") LocalDateTime currentDate);
}
