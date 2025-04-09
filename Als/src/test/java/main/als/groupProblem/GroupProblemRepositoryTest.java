package main.als.groupProblem;

import jakarta.persistence.EntityManager;
import main.als.group.entity.Group;
import main.als.group.repository.GroupRepository;
import main.als.problem.entity.Deduct;
import main.als.problem.entity.GroupProblem;
import main.als.problem.entity.Problem;
import main.als.problem.entity.ProblemType;
import main.als.problem.repository.GroupProblemRepository;
import main.als.problem.repository.ProblemRepository;
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
public class GroupProblemRepositoryTest {

    @Autowired
    private GroupProblemRepository groupProblemRepository;

    @Autowired
    private ProblemRepository problemRepository;

    @Autowired
    private GroupRepository groupRepository;


    LocalDateTime now = LocalDateTime.now();

    Long groupProblemId;

    private Group savedGroup;
    private Problem savedProblem;
    private GroupProblem savedGroupProblem;

    @BeforeEach
    public void setUp() {

        Group group = Group.builder()
                .name("group")
                .password("password")
                .leader("leader")
                .depositAmount(BigDecimal.valueOf(1000))
                .createdAt(now)
                .deadline(now.plusDays(1))
                .studyEndDate(now.plusDays(5))
                .build();

        savedGroup = groupRepository.save(group);

        Problem problem = Problem.builder()
                .title("title")
                .difficultyLevel("easy")
                .problemType(ProblemType.GREEDY)
                .createdAt(now)
                .description("description")
                .inputDescription("input1")
                .outputDescription("output1")
                .exampleInput("input1")
                .exampleOutput("output1")
                .build();

        savedProblem = problemRepository.save(problem);

        GroupProblem groupProblem = GroupProblem.builder()
                .problem(problem)
                .group(group)
                .createdAt(now)
                .deadline(now.plusDays(1))
                .deductionAmount(BigDecimal.valueOf(1000))
                .deduct(Deduct.FALSE)
                .build();

        savedGroupProblem = groupProblemRepository.save(groupProblem);

        groupProblemId = savedGroupProblem.getId();

    }

    @Test
    @DisplayName("findById 테스트")
    public void findByIdTest() {

        GroupProblem groupProblem = groupProblemRepository.findById(groupProblemId).get();

        assertEquals(groupProblemId, groupProblem.getId());
        assertEquals(savedGroup, groupProblem.getGroup());
        assertEquals(savedProblem, groupProblem.getProblem());

    }

    @Test
    @DisplayName("findAll 테스트")
    public void findAllTest() {

        List<GroupProblem> groupProblems = groupProblemRepository.findAll();

        assertEquals(1, groupProblems.size());
        assertEquals(savedGroupProblem, groupProblems.get(0));

    }

    @Test
    @DisplayName("findByGroupId 테스트")
    public void findByGroupIdTest(){

        Long groupId = savedGroup.getId();

        Pageable pageable = PageRequest.of(0, 10);

        Page<GroupProblem> groupProblems = groupProblemRepository.findByGroupId(groupId, pageable);

        assertEquals(1, groupProblems.getTotalElements());
        assertEquals(groupId, groupProblems.getContent().get(0).getGroup().getId());

    }

    @Test
    @DisplayName("findByGroupIdAndDeadlineGreaterThanEqual 테스트")
    public void findByGroupIdAndDeadlineGreaterThanEqualTest(){

        Long groupId = savedGroup.getId();
        LocalDateTime now = LocalDateTime.now();
        Pageable pageable = PageRequest.of(0, 10);

        Page<GroupProblem> groupProblems = groupProblemRepository.findByGroupIdAndDeadlineGreaterThanEqual(groupId, now, pageable);

        assertEquals(1, groupProblems.getTotalElements());
        assertEquals(groupId, groupProblems.getContent().get(0).getGroup().getId());
        assertTrue(groupProblems.getContent().get(0).getDeadline().isAfter(now)
                || groupProblems.getContent().get(0).getDeadline().isEqual(now));

    }

    @Test
    @DisplayName("delete 테스트")
    public void deleteTest(){

        GroupProblem groupProblem = groupProblemRepository.findById(groupProblemId).get();

        groupProblemRepository.delete(groupProblem);

        assertFalse(groupProblemRepository.findById(groupProblemId).isPresent());

    }

}
