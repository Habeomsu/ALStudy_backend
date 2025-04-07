package main.als.problem.repository;

import jakarta.transaction.Transactional;
import main.als.problem.entity.GroupProblem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface GroupProblemRepository extends JpaRepository<GroupProblem, Long> {
    List<GroupProblem> findByGroupId(Long groupId);
    Optional<GroupProblem> findById(Long groupProblemId);
    List<GroupProblem> findAll();
    Page<GroupProblem> findByGroupId(Long groupId, Pageable pageable);

    @Query("SELECT gp FROM GroupProblem gp WHERE gp.group.id = :groupId AND gp.deadline >= :now")
    Page<GroupProblem> findByGroupIdAndDeadlineGreaterThanEqual(@Param("groupId") Long groupId, @Param("now") LocalDateTime now, Pageable pageable);

    @Transactional
    void delete(GroupProblem groupProblem);

}
