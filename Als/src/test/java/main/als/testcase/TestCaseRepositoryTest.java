package main.als.testcase;

import jakarta.persistence.EntityManager;
import main.als.problem.entity.Problem;
import main.als.problem.entity.TestCase;
import main.als.problem.repository.ProblemRepository;
import main.als.problem.repository.TestCaseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@ActiveProfiles("test")
public class TestCaseRepositoryTest {

    @Autowired
    private TestCaseRepository testCaseRepository;

    @Autowired
    private ProblemRepository problemRepository;

    @Autowired
    private EntityManager entityManager;

    Long testCaseId;
    Long problemId;

    @BeforeEach
    public void setUp() {

        Problem problem1 = Problem.builder()
                .title("title1")
                .description("description1")
                .build();

        Problem savedProblem  = problemRepository.save(problem1);

        TestCase testCase1 = TestCase.builder()
                .problem(savedProblem)
                .input("input1")
                .expectedOutput("output1")
                .build();

        TestCase testCase2 = TestCase.builder()
                .problem(savedProblem)
                .input("input2")
                .expectedOutput("output2")
                .build();

        TestCase savedTestCase1 = testCaseRepository.save(testCase1);
        TestCase savedTestCase2 = testCaseRepository.save(testCase2);

        testCaseId = savedTestCase1.getId();
        problemId = savedTestCase1.getProblem().getId();
    }

    @Test
    @DisplayName("findByProblemId 테스트")
    public void findByProblemIdTest(){

        List<TestCase> testCases = testCaseRepository.findByProblemId(problemId);

        assertEquals(2,testCases.size());
        assertEquals(testCaseId,testCases.get(0).getId());
        assertEquals(problemId,testCases.get(0).getProblem().getId());
        assertEquals("input1",testCases.get(0).getInput());
        assertEquals("output1",testCases.get(0).getExpectedOutput());

    }

    @Test
    @DisplayName("findById 테스트")
    public void findByIdTest(){

        Optional<TestCase> testCase = testCaseRepository.findById(testCaseId);

        assertTrue(testCase.isPresent());
        assertEquals(testCaseId,testCase.get().getId());
        assertEquals("input1",testCase.get().getInput());
        assertEquals("output1",testCase.get().getExpectedOutput());

    }

}
