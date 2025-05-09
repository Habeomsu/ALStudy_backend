package main.als.submissionTest;

import main.als.group.entity.Group;
import main.als.group.repository.GroupRepository;
import main.als.problem.entity.GroupProblem;
import main.als.problem.entity.Problem;
import main.als.problem.entity.Submission;
import main.als.problem.entity.SubmissionStatus;
import main.als.problem.repository.GroupProblemRepository;
import main.als.problem.repository.ProblemRepository;
import main.als.problem.repository.SubmissionRepository;
import main.als.user.entity.Role;
import main.als.user.entity.User;
import main.als.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
public class SubmissionRepositoryTest {

    @Autowired
    private SubmissionRepository submissionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GroupProblemRepository groupProblemRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private ProblemRepository problemRepository;

    private Submission savedSubmission1;
    private Submission savedSubmission2;
    private GroupProblem savedGroupProblem1;
    private User savedUser1;

    LocalDateTime now = LocalDateTime.now();
    BigDecimal depositAmount = BigDecimal.valueOf(1000);

    @BeforeEach
    public void setUp() {

        Group group = groupRepository.save(Group.builder()
                .name("name")
                .password("password")
                .leader("leader")
                .depositAmount(depositAmount)
                .createdAt(now)
                .deadline(now)
                .studyEndDate(now)
                .build());

        Problem problem = problemRepository.save(Problem.builder()
                .title("test1")
                .build());

        savedGroupProblem1 = groupProblemRepository.save(GroupProblem.builder()
                        .problem(problem)
                        .group(group)
                        .build());

        savedUser1 = userRepository.save(User.builder()
                        .username("test1")
                        .password("password")
                        .role(Role.ROLE_USER)
                        .customerId("test1")
                        .build());

        Submission submission1 = Submission.builder()
                .groupProblem(savedGroupProblem1)
                .user(savedUser1)
                .language("python")
                .code("test1.com")
                .status(SubmissionStatus.FAILED)
                .submissionTime(now)
                .build();

        Submission submission2 = Submission.builder()
                .groupProblem(savedGroupProblem1)
                .user(savedUser1)
                .language("python")
                .code("test2.com")
                .status(SubmissionStatus.SUCCEEDED)
                .submissionTime(now)
                .build();

        savedSubmission1 = submissionRepository.save(submission1);
        savedSubmission2 = submissionRepository.save(submission2);

    }

    @Test
    @DisplayName("findByUserUsername 테스트")
    public void findByUserUsernameTest() {

        List<Submission> submissions = submissionRepository.findByUserUsername("test1");

        assertNotNull(submissions);
        assertFalse(submissions.isEmpty());

        for (Submission submission : submissions) {
            assertEquals("test1", submission.getUser().getUsername());
        }

        assertEquals(2, submissions.size());
    }

    @Test
    @DisplayName("findByUserAndGroupProblem 테스트")
    public void findByUserAndGroupProblemTest(){

        List<Submission> submissions = submissionRepository.findByUserAndGroupProblem(savedUser1, savedGroupProblem1);

        assertNotNull(submissions);
        assertFalse(submissions.isEmpty());

        for (Submission submission : submissions) {
            assertEquals("test1", submission.getUser().getUsername());
            assertEquals(savedGroupProblem1.getId(), submission.getGroupProblem().getId());
        }

        assertEquals(2, submissions.size());

    }

    @Test
    @DisplayName("findByUserAndGroupProblemPage 테스트")
    public void findByUserAndGroupProblemPageTest(){

        for (int i = 3; i <= 5; i++) {
            submissionRepository.save(Submission.builder()
                    .groupProblem(savedGroupProblem1)
                    .user(savedUser1)
                    .language("python")
                    .code("test" + i + ".com")
                    .status(SubmissionStatus.FAILED)
                    .submissionTime(now.plusMinutes(i))
                    .build());
        }

        Pageable pageable = PageRequest.of(0, 2);
        Page<Submission> page = submissionRepository.findByUserAndGroupProblem(savedUser1, savedGroupProblem1, pageable);

        assertNotNull(page);
        assertEquals(2, page.getContent().size());
        assertEquals(5, page.getTotalElements());
        assertEquals(3, page.getTotalPages());
        assertEquals(0, page.getNumber());

        for (Submission submission : page.getContent()) {
            assertEquals(savedUser1.getId(), submission.getUser().getId());
            assertEquals(savedGroupProblem1.getId(), submission.getGroupProblem().getId());
        }

    }

    @Test
    @DisplayName("findByGroupProblemIdAndStatusPage 테스트")
    public void findByGroupProblemIdAndStatusTest(){

        for (int i = 0; i < 3; i++) {
            submissionRepository.save(Submission.builder()
                    .groupProblem(savedGroupProblem1)
                    .user(savedUser1)
                    .language("python")
                    .code("success" + i + ".com")
                    .status(SubmissionStatus.SUCCEEDED)
                    .submissionTime(now.plusMinutes(i))
                    .build());
        }

        Pageable pageable = PageRequest.of(0, 2);

        Page<Submission> page = submissionRepository.findByGroupProblemIdAndStatus(
                savedGroupProblem1.getId(),
                SubmissionStatus.SUCCEEDED,
                pageable
        );

        assertNotNull(page);
        assertEquals(2, page.getContent().size());
        assertEquals(4, page.getTotalElements());
        assertEquals(2, page.getTotalPages());
        assertEquals(0, page.getNumber());

        for (Submission submission : page.getContent()) {
            assertEquals(savedGroupProblem1.getId(), submission.getGroupProblem().getId());
            assertEquals(SubmissionStatus.SUCCEEDED, submission.getStatus());
        }

    }

    @Test
    @DisplayName("existsByUserAndGroupProblemAndStatus 테스트")
    public void existsByUserAndGroupProblemAndStatusTest(){

        boolean existsSucceeded = submissionRepository.existsByUserAndGroupProblemAndStatus(
                savedUser1,
                savedGroupProblem1,
                SubmissionStatus.SUCCEEDED
        );

        boolean existsFailed = submissionRepository.existsByUserAndGroupProblemAndStatus(
                savedUser1,
                savedGroupProblem1,
                SubmissionStatus.FAILED
        );

        boolean existsPending = submissionRepository.existsByUserAndGroupProblemAndStatus(
                savedUser1,
                savedGroupProblem1,
                SubmissionStatus.PENDING
        );

        assertTrue(existsSucceeded);
        assertTrue(existsFailed);
        assertFalse(existsPending);

    }
}
