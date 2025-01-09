package main.als.problem.repository;

import main.als.problem.entity.GroupProblem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GroupProblemRepository extends JpaRepository<GroupProblem, Long> {
    List<GroupProblem> findByGroupId(Long groupId);


}
