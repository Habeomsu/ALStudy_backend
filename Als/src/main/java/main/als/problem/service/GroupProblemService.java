package main.als.problemTest.service;

import main.als.page.PostPagingDto;
import main.als.problemTest.dto.GroupProblemRequestDto;
import main.als.problemTest.dto.GroupProblemResponseDto;

public interface GroupProblemService {
    void createGroupProblem(GroupProblemRequestDto.GroupProblemDto groupProblemDto,String username,Long groupId);
    GroupProblemResponseDto.SearchGroupProblem getGroupProblems(Long groupId, String username, PostPagingDto.PagingDto pagingDto);
    GroupProblemResponseDto.SearchGroupProblem getTodayGroupProblems(Long groupId, String username, PostPagingDto.PagingDto pagingDto);
    GroupProblemResponseDto.DetailGroupProblem getDetailGroupProblem(Long groupId,Long groupProblemId,String username);
    void checkDeadlines();
    void deleteGroupProblem(Long groupId,Long groupProblemId, String username);
    void updateGroupProblem(GroupProblemRequestDto.UpdateGroupProblemDto updateGroupProblemDto,Long groupId,Long groupProblemId, String username);
}
