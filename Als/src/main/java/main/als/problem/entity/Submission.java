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

    @JoinColumn(name = "problem_id", nullable = false)
    @ManyToOne
    private Problem problem;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false) // 사용자 이름을 저장하기 위한 필드
    private User user;

    private String language;
    private String code;
    private String status;
    private LocalDateTime submissionTime;

}
