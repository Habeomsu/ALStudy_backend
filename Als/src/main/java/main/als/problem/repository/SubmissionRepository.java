package main.als.problem.repository;

import main.als.problem.entity.GroupProblem;
import main.als.problem.entity.Submission;
import main.als.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubmissionRepository extends JpaRepository<Submission, Long> {

    List<Submission> findByUserUsername(String username);

    List<Submission> findByUserAndGroupProblem(User user, GroupProblem groupProblem);

}
