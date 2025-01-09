package main.als.problem.repository;

import main.als.problem.entity.Problem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProblemRepository extends JpaRepository<Problem, Long> {

    List<Problem> findAll();
    Optional<Problem> findById(Long id);

}
