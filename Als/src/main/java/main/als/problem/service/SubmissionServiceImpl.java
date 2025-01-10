package main.als.problem.service;

import main.als.apiPayload.code.status.ErrorStatus;
import main.als.apiPayload.exception.GeneralException;
import main.als.group.repository.UserGroupRepository;
import main.als.problem.dto.SubmissionRequestDto;
import main.als.problem.entity.GroupProblem;
import main.als.problem.entity.Submission;
import main.als.problem.entity.SubmissionStatus;
import main.als.problem.repository.GroupProblemRepository;
import main.als.problem.repository.SubmissionRepository;
import main.als.user.entity.User;
import main.als.user.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class SubmissionServiceImpl implements SubmissionService {

    private final SubmissionRepository repository;
    private final UserRepository userRepository;
    private final GroupProblemRepository groupProblemRepository;
    private final SubmissionRepository submissionRepository;
    private final UserGroupRepository userGroupRepository;

    public SubmissionServiceImpl(SubmissionRepository repository, UserRepository userRepository,
                                 GroupProblemRepository groupProblemRepository,
                                 SubmissionRepository submissionRepository,
                                 UserGroupRepository userGroupRepository) {
        this.repository = repository;
        this.userRepository = userRepository;
        this.groupProblemRepository = groupProblemRepository;
        this.submissionRepository = submissionRepository;
        this.userGroupRepository = userGroupRepository;
    }

    @Override
    public void submit(SubmissionRequestDto.SubmissionDto submissionDto,Long groupProblemId,String username) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new GeneralException(ErrorStatus._USERNAME_NOT_FOUND);
        }
        GroupProblem groupProblem = groupProblemRepository.findById(groupProblemId)
                .orElseThrow(()->new GeneralException(ErrorStatus._NOT_FOUND_GROUPPROBLEM));

        boolean isMember = userGroupRepository.existsByGroupIdAndUserUsername(groupProblem.getGroup().getId(), user.getUsername());

        if (!isMember) {
            throw new GeneralException(ErrorStatus._NOT_IN_USERGROUP); // 권한이 없는 경우 예외 처리
        }

        Submission submission = Submission.builder()
                .groupProblem(groupProblem)
                .user(user)
                .language(submissionDto.getLanguage())
                .code(submissionDto.getCode())
                .status(SubmissionStatus.FAILED)
                .submissionTime(LocalDateTime.now())
                .build();

        user.getSubmissions().add(submission);
        groupProblem.getSubmissions().add(submission);

        submissionRepository.save(submission);

    }
}
