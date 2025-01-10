package main.als.problem.repository;

import main.als.problem.entity.GroupProblem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GroupProblemRepository extends JpaRepository<GroupProblem, Long> {
    List<GroupProblem> findByGroupId(Long groupId);
    Optional<GroupProblem> findById(Long groupProblemId);

}
