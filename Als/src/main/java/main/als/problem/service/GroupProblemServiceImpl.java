package main.als.problem.service;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import main.als.apiPayload.code.status.ErrorStatus;
import main.als.apiPayload.exception.GeneralException;
import main.als.group.entity.Group;
import main.als.group.entity.UserGroup;
import main.als.group.repository.GroupRepository;
import main.als.group.repository.UserGroupRepository;
import main.als.page.PostPagingDto;
import main.als.problem.converter.GroupProblemConverter;
import main.als.problem.dto.GroupProblemRequestDto;
import main.als.problem.dto.GroupProblemResponseDto;
import main.als.problem.entity.*;
import main.als.problem.repository.GroupProblemRepository;
import main.als.problem.repository.ProblemRepository;
import main.als.problem.repository.SubmissionRepository;
import main.als.problem.util.SubmissionStatusDeterminer;
import main.als.user.entity.User;
import main.als.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class GroupProblemServiceImpl implements GroupProblemService {

    private final GroupProblemRepository groupProblemRepository;
    private final ProblemRepository problemRepository;
    private final GroupRepository groupRepository;
    private final UserGroupRepository userGroupRepository;
    private final SubmissionRepository submissionRepository;
    private final UserRepository userRepository;

    public GroupProblemServiceImpl(GroupProblemRepository groupProblemRepository,ProblemRepository problemRepository,
                                   GroupRepository groupRepository,UserGroupRepository userGroupRepository,
                                   SubmissionRepository submissionRepository,UserRepository userRepository) {
        this.groupProblemRepository = groupProblemRepository;
        this.problemRepository = problemRepository;
        this.groupRepository = groupRepository;
        this.userGroupRepository = userGroupRepository;
        this.submissionRepository = submissionRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public void createGroupProblem(GroupProblemRequestDto.GroupProblemDto groupProblemDto,String username,Long groupId) {

        Problem problem = problemRepository.findById(groupProblemDto.getProblem_id())
                .orElseThrow(()-> new GeneralException(ErrorStatus._NOT_FOUND_PROBLEM));

        Group group = groupRepository.findById(groupId)
                .orElseThrow(()-> new GeneralException(ErrorStatus._NOT_FOUND_GROUP));

        String leader = group.getLeader();

        if (!username.equals(leader)) {
            throw new GeneralException(ErrorStatus._NOT_MATCH_LEADER);
        }

        boolean isDuplicate = group.getGroupProblems().stream()
                .anyMatch(gp -> gp.getProblem().getId().equals(problem.getId()));

        if (isDuplicate) {
            throw new GeneralException(ErrorStatus._DUPLICATE_GROUP_PROBLEM); // 중복된 문제 예외 처리
        }

        //그룹 모집기간 이후에 문제 생성 가능
//        if(group.getDeadline().isAfter(LocalDateTime.now())) {
//            throw new GeneralException(ErrorStatus._DEADLINE_NOT_PASSED);
//        }

        GroupProblem groupProblem = GroupProblem.builder()
                .problem(problem)
                .group(group)
                .createdAt(LocalDateTime.now())
                .deadline(groupProblemDto.getDeadline())
                .deductionAmount(groupProblemDto.getDeductionAmount())
                .deduct(Deduct.FALSE)
                .build();


        group.getGroupProblems().add(groupProblem);

        groupProblemRepository.save(groupProblem);

    }

    @Override
    public GroupProblemResponseDto.SearchGroupProblem getGroupProblems(Long groupId, String username, PostPagingDto.PagingDto pagingDto) {

        if (!groupRepository.existsById(groupId)) {
            throw new GeneralException(ErrorStatus._NOT_FOUND_GROUP);
        }

        Sort sort = Sort.by(Sort.Direction.fromString(pagingDto.getSort()),"createdAt");
        Pageable pageable = PageRequest.of(pagingDto.getPage(), pagingDto.getSize(), sort);

        Page<GroupProblem> groupProblems = groupProblemRepository.findByGroupId(groupId,pageable);

        List<Submission> userSubmissions = submissionRepository.findByUserUsername(username);

        // submission 을 사용자별로 가지고온다음 그룹 아이디 : [상태들] -> 그룹 아이디 : 상태로 만듬
        Map<Long, SubmissionStatus> submissionStatusMap = userSubmissions.stream()
                .collect(Collectors.groupingBy(
                        submission -> submission.getGroupProblem().getId(),
                        Collectors.mapping(Submission::getStatus, Collectors.toList())
                ))
                .entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> {
                            List<SubmissionStatus> statuses = entry.getValue(); // entry의 값을 가져옴

                            if (statuses.contains(SubmissionStatus.SUCCEEDED)) {
                                return SubmissionStatus.SUCCEEDED; // 성공이 있으면 성공으로 설정
                            } else if (statuses.contains(SubmissionStatus.FAILED)) {
                                return SubmissionStatus.FAILED; // 실패가 있으면 실패로 설정
                            } else {
                                return SubmissionStatus.PENDING; // 제출이 없거나 모두 대기 상태면 대기
                            }
                        }
                ));


        return GroupProblemConverter.toSearchGroupProblemDto(groupProblems, submissionStatusMap);
    }

    @Override
    public GroupProblemResponseDto.SearchGroupProblem getTodayGroupProblems(Long groupId, String username, PostPagingDto.PagingDto pagingDto) {

        if (!groupRepository.existsById(groupId)) {
            throw new GeneralException(ErrorStatus._NOT_FOUND_GROUP);
        }

        Sort sort = Sort.by(Sort.Direction.fromString(pagingDto.getSort()),"createdAt");
        Pageable pageable = PageRequest.of(pagingDto.getPage(), pagingDto.getSize(), sort);

        // 현재 날짜와 시간
        LocalDateTime now = LocalDateTime.now();

        // 현재 시간보다 마감일이 큰 문제를 필터링합니다.
        Page<GroupProblem> groupProblems = groupProblemRepository.findByGroupIdAndDeadlineGreaterThanEqual(
                groupId,
                now, // 현재 시간
                pageable
        );

        List<Submission> userSubmissions = submissionRepository.findByUserUsername(username);

        // submission 을 사용자별로 가지고온다음 그룹 아이디 : [상태들] -> 그룹 아이디 : 상태로 만듬
        Map<Long, SubmissionStatus> submissionStatusMap = userSubmissions.stream()
                .collect(Collectors.groupingBy(
                        submission -> submission.getGroupProblem().getId(),
                        Collectors.mapping(Submission::getStatus, Collectors.toList())
                ))
                .entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> {
                            List<SubmissionStatus> statuses = entry.getValue(); // entry의 값을 가져옴

                            if (statuses.contains(SubmissionStatus.SUCCEEDED)) {
                                return SubmissionStatus.SUCCEEDED; // 성공이 있으면 성공으로 설정
                            } else if (statuses.contains(SubmissionStatus.FAILED)) {
                                return SubmissionStatus.FAILED; // 실패가 있으면 실패로 설정
                            } else {
                                return SubmissionStatus.PENDING; // 제출이 없거나 모두 대기 상태면 대기
                            }
                        }
                ));


        return GroupProblemConverter.toSearchGroupProblemDto(groupProblems, submissionStatusMap);
    }

    @Override
    public GroupProblemResponseDto.DetailGroupProblem getDetailGroupProblem(Long groupId,Long groupProblemId,String username) {
        GroupProblem groupProblem = groupProblemRepository.findById(groupProblemId)
                .orElseThrow(()->new GeneralException(ErrorStatus._NOT_FOUND_GROUPPROBLEM));

        Group group = groupProblem.getGroup();

        // URL에서 받은 groupId와 실제 그룹 ID가 일치하는지 확인
        if (!group.getId().equals(groupId)) {
            throw new GeneralException(ErrorStatus._NOT_FOUND_GROUP); // 잘못된 그룹 ID 예외 처리
        }

        boolean isMember = userGroupRepository.existsByGroupIdAndUserUsername(group.getId(), username);

        if (!isMember) {
            throw new GeneralException(ErrorStatus._NOT_IN_USERGROUP); // 권한이 없는 경우 예외 처리
        }

        User user = userRepository.findByUsername(username);

        if (user == null) {
            throw new GeneralException(ErrorStatus._USERNAME_NOT_FOUND);
        }

        List<Submission> submissions = submissionRepository.findByUserAndGroupProblem(user, groupProblem);

        SubmissionStatus finalStatus = SubmissionStatusDeterminer.determineFinalSubmissionStatus(submissions);

        return GroupProblemConverter.toDetailGroupProblem(groupProblem,finalStatus);
    }

    @Transactional
    @Override
    public void checkDeadlines() {
        LocalDateTime currentTime = LocalDateTime.now();
        List<GroupProblem> problems = groupProblemRepository.findAll();

        for (GroupProblem groupProblem : problems) {
            if (currentTime.isAfter(groupProblem.getDeadline()) && groupProblem.getDeduct() == Deduct.FALSE) {

                Map<Long, List<Submission>> submissionsByUser = groupProblem.getSubmissions().stream()
                        .collect(Collectors.groupingBy(submission -> submission.getUser().getId()));

                List<UserGroup> userGroups = userGroupRepository.findByGroupId(groupProblem.getGroup().getId());

                for (UserGroup userGroup : userGroups) {
                    List<Submission> userSubmissions = submissionsByUser.get(userGroup.getUser().getId());

                    // 제출이 없는 경우
                    if (userSubmissions == null || userSubmissions.isEmpty()) {
                        // 금액 차감 로직
                        if (userGroup.getUserDepositAmount().compareTo(groupProblem.getDeductionAmount()) < 0) {
                            // 예치금이 부족한 경우 0으로 설정
                            userGroup.setUserDepositAmount(BigDecimal.ZERO);
                        } else {
                            userGroup.setUserDepositAmount(userGroup.getUserDepositAmount().subtract(groupProblem.getDeductionAmount())); // 금액 차감
                        }
                        userGroupRepository.save(userGroup); // 상태 업데이트
                        log.info("User {} has been deducted amount {} due to no submission for group problem ID {}.",
                                userGroup.getUser().getUsername(),
                                groupProblem.getDeductionAmount(),
                                groupProblem.getId());
                    } else {
                        // 사용자 제출 중 성공적인 제출이 있는지 확인
                        boolean hasSuccessfulSubmission = userSubmissions.stream()
                                .anyMatch(submission -> SubmissionStatus.SUCCEEDED.equals(submission.getStatus()));

                        // 성공적인 제출이 없는 경우에만 금액 차감
                        if (!hasSuccessfulSubmission) {
                            if (userGroup.getUserDepositAmount().compareTo(groupProblem.getDeductionAmount()) < 0) {
                                // 예치금이 부족한 경우 0으로 설정
                                userGroup.setUserDepositAmount(BigDecimal.ZERO);
                            } else {
                                userGroup.setUserDepositAmount(userGroup.getUserDepositAmount().subtract(groupProblem.getDeductionAmount())); // 금액 차감
                            }
                            userGroupRepository.save(userGroup); // 상태 업데이트
                            log.info("User {} has been deducted amount {} due to no successful submission for group problem ID {}.",
                                    userGroup.getUser().getUsername(),
                                    groupProblem.getDeductionAmount(),
                                    groupProblem.getId());
                        }
                    }
                }
                // 금액 차감 완료 후 상태 업데이트
                groupProblem.setDeduct(Deduct.TRUE);  // 금액 차감 여부 업데이트
                groupProblemRepository.save(groupProblem); // 상태 업데이트
            }
        }
    }


    @Override
    public void deleteGroupProblem(Long groupId, Long groupProblemId, String username) {

        GroupProblem groupProblem = groupProblemRepository.findById(groupProblemId)
                .orElseThrow(()->new GeneralException(ErrorStatus._NOT_FOUND_GROUPPROBLEM));

        Group group = groupProblem.getGroup();

        if (!group.getId().equals(groupId)) {
            throw new GeneralException(ErrorStatus._NOT_MATCH_GROUP);
        }

        if(!group.getLeader().equals(username)){
            throw new GeneralException(ErrorStatus._NOT_MATCH_LEADER);
        }

        groupProblemRepository.delete(groupProblem);

    }

    @Override
    public void updateGroupProblem(GroupProblemRequestDto.UpdateGroupProblemDto updateGroupProblemDto, Long groupId, Long groupProblemId, String username) {

        GroupProblem groupProblem = groupProblemRepository.findById(groupProblemId)
                .orElseThrow(()->new GeneralException(ErrorStatus._NOT_FOUND_GROUPPROBLEM));

        Group group = groupProblem.getGroup();

        //그룹 일치 검사
        if (!group.getId().equals(groupId)) {
            throw new GeneralException(ErrorStatus._NOT_MATCH_GROUP);
        }

        //리더 검사
        if(!group.getLeader().equals(username)){
            throw new GeneralException(ErrorStatus._NOT_MATCH_LEADER);
        }

        // 마감일 검사
        if (groupProblem.getDeadline().isBefore(LocalDateTime.now())) {
            throw new GeneralException(ErrorStatus._DEADLINE_EXPIRED);
        }

        groupProblem.setDeadline(updateGroupProblemDto.getDeadline());
        groupProblem.setDeductionAmount(updateGroupProblemDto.getDeductionAmount());

        groupProblemRepository.save(groupProblem);


    }



}
