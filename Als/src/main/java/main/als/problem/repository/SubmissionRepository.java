package main.als.problem.repository;

import main.als.problem.entity.GroupProblem;
import main.als.problem.entity.Submission;
import main.als.problem.entity.SubmissionStatus;
import main.als.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SubmissionRepository extends JpaRepository<Submission, Long> {

    List<Submission> findByUserUsername(String username);

    List<Submission> findByUserAndGroupProblem(User user, GroupProblem groupProblem);

    Page<Submission> findByUserAndGroupProblem(User user, GroupProblem groupProblem, Pageable pageable);

    Optional<Submission> findById(long id);

    List<Submission> findByGroupProblemIdAndStatus(long groupProblemId,SubmissionStatus status );

    Page<Submission> findByGroupProblemIdAndStatus(long groupProblemId,SubmissionStatus status,Pageable pageable);

    // 사용자, 그룹 문제, 제출 상태를 기반으로 제출 존재 여부 확인
    boolean existsByUserAndGroupProblemAndStatus(User user, GroupProblem groupProblem, SubmissionStatus status);
}
