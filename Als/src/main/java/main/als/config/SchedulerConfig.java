package main.als.config;

import main.als.group.service.UserGroupService;
import main.als.problem.entity.GroupProblem;
import main.als.problem.service.GroupProblemService;
import main.als.problem.service.ProblemService;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableScheduling
public class SchedulerConfig {

    private final GroupProblemService groupProblemService;
    private final UserGroupService userGroupService;

    public SchedulerConfig(GroupProblemService groupProblemService, UserGroupService userGroupService) {
        this.groupProblemService = groupProblemService;
        this.userGroupService = userGroupService;
    }

    @Scheduled(fixedRate = 60000) // 60초마다 실행 (60000ms)
    public void checkProblems() {
        groupProblemService.checkDeadlines();
        userGroupService.checkCharged();
    }

}
