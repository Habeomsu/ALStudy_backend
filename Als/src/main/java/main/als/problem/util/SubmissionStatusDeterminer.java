package main.als.problem.util;

import main.als.problem.entity.Submission;
import main.als.problem.entity.SubmissionStatus;

import java.util.List;

public class SubmissionStatusDeterminer {

    public static SubmissionStatus determineFinalSubmissionStatus(List<Submission> submissions) {

        boolean hasSucceeded = false;
        boolean hasFailed = false;

        // 제출 목록을 순회하면서 상태를 확인
        for (Submission submission : submissions) {
            if (submission.getStatus() == SubmissionStatus.SUCCEEDED) {
                hasSucceeded = true;
                break; // 성공한 경우 더 이상 확인할 필요 없음
            } else if (submission.getStatus() == SubmissionStatus.FAILED) {
                hasFailed = true;
            }
        }

        // 최종 상태 결정
        if (hasSucceeded) {
            return SubmissionStatus.SUCCEEDED; // 하나라도 성공하면 성공으로 설정
        } else if (hasFailed) {
            return SubmissionStatus.FAILED; // 실패가 있으면 실패로 설정
        } else {
            return SubmissionStatus.PENDING; // 제출이 없거나 모두 대기 상태면 대기
        }
    }

}
