package main.als.problem.repository;

import main.als.problem.entity.Problem;
import main.als.problem.entity.ProblemType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProblemRepository extends JpaRepository<Problem, Long> {

    List<Problem> findAll();

    Page<Problem> findAll(Pageable pageable);

    Optional<Problem> findById(Long id);

    void deleteById(Long id);

    Page<Problem> findByProblemType(ProblemType problemType, Pageable pageable);

    Page<Problem> findByProblemTypeAndTitleContaining(ProblemType problemType, String title, Pageable pageable);

    Page<Problem> findByTitleContaining(String title, Pageable pageable);
}