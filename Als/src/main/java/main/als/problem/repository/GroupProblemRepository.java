package main.als.problem.repository;

import main.als.problem.entity.GroupProblem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GroupProblemRepository extends JpaRepository<GroupProblem, Long> {
    List<GroupProblem> findByGroupId(Long groupId);
    Optional<GroupProblem> findById(Long groupProblemId);
    List<GroupProblem> findAll();
    Page<GroupProblem> findByGroupId(Long groupId, Pageable pageable);


}
