package main.als.problem.converter;

import main.als.problem.dto.TestCaseResponseDto;
import main.als.problem.entity.TestCase;

import java.util.List;
import java.util.stream.Collectors;

public class TestCaseConverter {

    public static TestCaseResponseDto.TestCaseDto toTestCase(TestCase testCase){
        return TestCaseResponseDto.TestCaseDto.builder()
                .id(testCase.getId())
                .problemId(testCase.getProblem().getId())
                .input(testCase.getInput())
                .expectedOutput(testCase.getExpectedOutput())
                .build();
    }

    public static List<TestCaseResponseDto.TestCaseDto> toTestCase(List<TestCase> testCases){
        return testCases
                .stream()
                .map(TestCaseConverter::toTestCase)
                .collect(Collectors.toList());
    }

}
