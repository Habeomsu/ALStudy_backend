package main.als.problem.repository;

import main.als.problem.entity.Problem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProblemRepository extends JpaRepository<Problem, Long> {

    List<Problem> findAll();

}
