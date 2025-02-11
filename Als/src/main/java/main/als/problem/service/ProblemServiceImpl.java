package main.als.problem.service;


import jakarta.transaction.Transactional;
import main.als.apiPayload.code.status.ErrorStatus;
import main.als.apiPayload.exception.GeneralException;
import main.als.page.PostPagingDto;
import main.als.problem.converter.ProblemConverter;
import main.als.problem.dto.ProblemRequestDto;
import main.als.problem.dto.ProblemResponseDto;
import main.als.problem.entity.Problem;
import main.als.problem.entity.ProblemType;
import main.als.problem.entity.TestCase;
import main.als.problem.repository.ProblemRepository;
import main.als.problem.repository.TestCaseRepository;
import org.hibernate.StaleObjectStateException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProblemServiceImpl implements ProblemService {
    private final ProblemRepository problemRepository;
    private final TestCaseRepository testCaseRepository;

    public ProblemServiceImpl(ProblemRepository problemRepository, TestCaseRepository testCaseRepository) {
        this.problemRepository = problemRepository;
        this.testCaseRepository = testCaseRepository;
    }

    @Override
    @Transactional
    public void createProblem(ProblemRequestDto.createProblemDto requestDto) {
        try {
            Problem problem = ProblemConverter.toProblem(requestDto);
            TestCase testCase =TestCase.builder()
                    .problem(problem)
                    .input(requestDto.getExampleInput())
                    .expectedOutput(requestDto.getExampleOutput())
                    .build();

            problem.getTestCases().add(testCase);
            problemRepository.save(problem);
        } catch (Exception e) {
            throw new GeneralException(ErrorStatus._NOT_CREATED_PROBLEM);
        }
    }

    @Override
    @Transactional
    public void updateProblem(ProblemRequestDto.createProblemDto requestDto,Long problemId) {
        try {
            Problem problem = problemRepository.findById(problemId)
                    .orElseThrow(() -> new GeneralException(ErrorStatus._NOT_FOUND_PROBLEM));

            // 문제의 속성을 업데이트합니다.
            problem.setTitle(requestDto.getTitle());
            problem.setDifficultyLevel(requestDto.getDifficultyLevel());
            problem.setProblemType(requestDto.getProblemType());
            problem.setDescription(requestDto.getDescription());
            problem.setInputDescription(requestDto.getInputDescription());
            problem.setOutputDescription(requestDto.getOutputDescription());
            problem.setExampleInput(requestDto.getExampleInput());
            problem.setExampleOutput(requestDto.getExampleOutput());

            // TestCase 업데이트
            if (!problem.getTestCases().isEmpty()) {
                // 첫 번째 TestCase를 업데이트
                TestCase testCase = problem.getTestCases().get(0); // 예시로 첫 번째 TestCase 사용
                testCase.setInput(requestDto.getExampleInput());
                testCase.setExpectedOutput(requestDto.getExampleOutput());
            } else {
                // TestCase가 없다면 새로 추가할 수 있습니다.
                TestCase newTestCase = TestCase.builder()
                        .problem(problem)
                        .input(requestDto.getExampleInput())
                        .expectedOutput(requestDto.getExampleOutput())
                        .build();
                problem.getTestCases().add(newTestCase);
            }

            // 문제 저장 (변경 사항이 자동으로 반영됨)
            problemRepository.save(problem);

        } catch (StaleObjectStateException e) {
            throw new GeneralException(ErrorStatus._BAD_REQUEST);
        } catch (Exception e) {
            throw new GeneralException(ErrorStatus._NOT_UPDATED_PROBLEM);
        }
    }

    @Override
    public ProblemResponseDto.SearchProblems getAllProblems(PostPagingDto.PagingDto pagingDto,String problemType,String search) {

        Sort sort = Sort.by(Sort.Direction.fromString(pagingDto.getSort()),"id");
        Pageable pageable = PageRequest.of(pagingDto.getPage(), pagingDto.getSize(), sort);

        Page<Problem> problems;

        if (problemType != null && !problemType.isEmpty()) {
            try {
                // 문자열을 ProblemType으로 변환
                ProblemType type = ProblemType.valueOf(problemType.toUpperCase());
                // 검색어가 주어진 경우
                if (search != null && !search.isEmpty()) {
                    problems = problemRepository.findByProblemTypeAndTitleContaining(type, search, pageable);
                } else {
                    problems = problemRepository.findByProblemType(type, pageable);
                }
            } catch (IllegalArgumentException e) {
                throw new GeneralException(ErrorStatus._INVALID_PROBLEM_TYPE); // 잘못된 문제 유형 처리
            }
        } else {
            // 문제 유형이 주어지지 않은 경우
            if (search != null && !search.isEmpty()) {
                problems = problemRepository.findByTitleContaining(search, pageable);
            } else {
                problems = problemRepository.findAll(pageable);
            }
        }


        return ProblemConverter.toSearchProblemDto(problems);
    }

    @Override
    public ProblemResponseDto.ProblemDto getProblemById(Long id) {
        Problem problem = problemRepository.findById(id)
                .orElseThrow(()->new GeneralException(ErrorStatus._NOT_FOUND_PROBLEM));
        return ProblemConverter.toProblemDto(problem);
    }

    @Override
    public void deleteProblem(Long id) {
        Problem problem = problemRepository.findById(id)
                .orElseThrow(()->new GeneralException(ErrorStatus._NOT_FOUND_PROBLEM));
        problemRepository.deleteById(id);
    }


}
