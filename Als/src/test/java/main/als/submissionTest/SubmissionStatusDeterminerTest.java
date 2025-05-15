package main.als.submissionTest;

import main.als.problem.entity.Submission;
import main.als.problem.entity.SubmissionStatus;
import main.als.problem.util.SubmissionStatusDeterminer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SubmissionStatusDeterminerTest {

    private Submission createSubmission(SubmissionStatus status) {
        return Submission.builder()
                .status(status)
                .build();
    }

    @Test
    @DisplayName("determineFinalSubmissionStatus 테스트 - ( 성공 테스트 )")
    void determineFinalSubmissionStatusTest1() {
        List<Submission> submissions = List.of(
                createSubmission(SubmissionStatus.FAILED),
                createSubmission(SubmissionStatus.SUCCEEDED),
                createSubmission(SubmissionStatus.PENDING)
        );

        SubmissionStatus result = SubmissionStatusDeterminer.determineFinalSubmissionStatus(submissions);

        assertEquals(SubmissionStatus.SUCCEEDED, result);
    }

    @Test
    @DisplayName("determineFinalSubmissionStatus 테스트 - ( 실패 테스트 )")
    void determineFinalSubmissionStatusTest2() {
        List<Submission> submissions = List.of(
                createSubmission(SubmissionStatus.FAILED),
                createSubmission(SubmissionStatus.FAILED)
        );

        SubmissionStatus result = SubmissionStatusDeterminer.determineFinalSubmissionStatus(submissions);

        assertEquals(SubmissionStatus.FAILED, result);
    }

    @Test
    @DisplayName("determineFinalSubmissionStatus 테스트 - ( 팬딩 테스트 )")
    void determineFinalSubmissionStatusTest3() {
        List<Submission> submissions = List.of();

        SubmissionStatus result = SubmissionStatusDeterminer.determineFinalSubmissionStatus(submissions);

        assertEquals(SubmissionStatus.PENDING, result);
    }

}
