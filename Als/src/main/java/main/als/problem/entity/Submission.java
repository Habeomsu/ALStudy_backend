package main.als.problem.entity;

import jakarta.persistence.*;
import lombok.*;
import main.als.user.entity.User;


import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "submissions")
public class Submission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_problem_id", nullable = false) // GroupProblem과의 관계
    private GroupProblem groupProblem; // 그룹 문제와의 관계

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false) // 사용자 이름을 저장하기 위한 필드
    private User user;

    private String language;
    private String code;

    @Enumerated(EnumType.STRING)
    private SubmissionStatus status;

    private LocalDateTime submissionTime;

}
