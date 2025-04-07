package main.als.problemTest;

import jakarta.persistence.EntityManager;
import main.als.problem.entity.Problem;
import main.als.problem.entity.ProblemType;
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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@ActiveProfiles("test")
public class ProblemRepositoryTest {

    @Autowired
    private ProblemRepository problemRepository;

    @Autowired
    private EntityManager entityManager;

    LocalDateTime now = LocalDateTime.now();

    private Long savedProblemId;

    @BeforeEach
    public void setUp(){

        Problem problem1 = Problem.builder()
                .title("title1")
                .difficultyLevel("easy")
                .description("description1")
                .problemType(ProblemType.GREEDY)
                .createdAt(now)
                .inputDescription("inputDescription1")
                .outputDescription("outputDescription1")
                .exampleInput("exampleInput1")
                .exampleOutput("exampleOutput1")
                .testCases(new ArrayList<>())
                .build();

        Problem problem2 = Problem.builder()
                .title("title2")
                .difficultyLevel("hard")
                .description("description2")
                .problemType(ProblemType.DYNAMIC_PROGRAMMING)
                .createdAt(now)
                .inputDescription("inputDescription2")
                .outputDescription("outputDescription2")
                .exampleInput("exampleInput2")
                .exampleOutput("exampleOutput2")
                .testCases(new ArrayList<>())
                .build();

        Problem savedProblem1 = problemRepository.save(problem1);
        Problem savedProblem2 = problemRepository.save(problem2);

        savedProblemId = savedProblem1.getId();

    }

    @Test
    @DisplayName("find All 테스트 - (Return List)")
    public void findAllTest(){

        List<Problem> problems = problemRepository.findAll();

        // 첫번째 요소
        assertEquals(2,problems.size());
        assertEquals("title1", problems.get(0).getTitle());
        assertEquals("easy", problems.get(0).getDifficultyLevel());
        assertEquals("description1", problems.get(0).getDescription());
        assertEquals("inputDescription1", problems.get(0).getInputDescription());
        assertEquals("outputDescription1", problems.get(0).getOutputDescription());
        assertEquals("exampleInput1", problems.get(0).getExampleInput());
        assertEquals("exampleOutput1", problems.get(0).getExampleOutput());

        // 두번째 요소
        assertEquals("title2", problems.get(1).getTitle());

    }

    @Test
    @DisplayName("find All 테스트 - (Return Page)")
    public void findAllPageTest(){

        Pageable pageable = PageRequest.of(0, 10);

        Page<Problem> problems = problemRepository.findAll(pageable);

        assertEquals(2,problems.getTotalElements());
        assertEquals("title1", problems.getContent().get(0).getTitle());
        assertEquals("title2", problems.getContent().get(1).getTitle());

    }

    @Test
    @DisplayName("findById 테스트")
    public void findByIdTest(){

        Long id = savedProblemId;
        Optional<Problem> problem = problemRepository.findById(id);

        assertEquals(id,problem.get().getId());
        assertEquals("title1", problem.get().getTitle());
        assertEquals("easy", problem.get().getDifficultyLevel());
        assertEquals("description1", problem.get().getDescription());
        assertEquals("inputDescription1", problem.get().getInputDescription());
        assertEquals("outputDescription1", problem.get().getOutputDescription());
        assertEquals("exampleInput1", problem.get().getExampleInput());
        assertEquals("exampleOutput1", problem.get().getExampleOutput());

    }

    @Test
    @DisplayName("deleteById 테스트")
    public void deleteByIdTest(){

        Long id = savedProblemId;

        problemRepository.deleteById(id);

        entityManager.flush();
        entityManager.clear();

        Optional<Problem> problem = problemRepository.findById(id);
        assertTrue(problem.isEmpty());

    }

    @Test
    @DisplayName("findByProblemType 테스트")
    public void findByProblemTypeTest(){

        ProblemType problemType = ProblemType.GREEDY;
        Pageable pageable = PageRequest.of(0, 10);

        Page<Problem> problems = problemRepository.findByProblemType(problemType, pageable);

        assertEquals(1,problems.getTotalElements());
        assertEquals("title1", problems.getContent().get(0).getTitle());
        assertEquals(ProblemType.GREEDY, problems.getContent().get(0).getProblemType());

    }

    @Test
    @DisplayName("findByProblemTypeAndTitleContaining 테스트")
    public void findByProblemTypeAndTitleContainingTest(){

        ProblemType problemType = ProblemType.GREEDY;
        String title = "title1";

        Pageable pageable = PageRequest.of(0, 10);

        Page<Problem> problems = problemRepository.findByProblemTypeAndTitleContaining(problemType, title, pageable);

        assertEquals(1,problems.getTotalElements());
        assertEquals("title1", problems.getContent().get(0).getTitle());
        assertEquals(ProblemType.GREEDY, problems.getContent().get(0).getProblemType());

    }

    @Test
    @DisplayName("findByTitleContaining 테스트")
    public void findByTitleContainingTest(){

        String title = "title1";
        Pageable pageable = PageRequest.of(0, 10);

        Page<Problem> problems = problemRepository.findByTitleContaining(title, pageable);
        assertEquals(1,problems.getTotalElements());
        assertEquals("title1", problems.getContent().get(0).getTitle());

    }

}
