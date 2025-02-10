package main.als.group.repository;

import main.als.group.entity.Group;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    // 모집 기간이 지나지 않은 그룹을 가져오는 쿼리
    @Query("SELECT g FROM Group g WHERE g.deadline > :now")
    Page<Group> findAllByDeadlineAfter(@Param("now") LocalDateTime now, Pageable pageable);

    @Query("SELECT g FROM Group g WHERE g.name LIKE %:search% AND g.deadline > :now")
    Page<Group> findByNameContainingAndDeadlineAfter(@Param("search") String search, @Param("now") LocalDateTime now, Pageable pageable);

}
