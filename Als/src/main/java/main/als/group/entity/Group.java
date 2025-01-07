package main.als.group.entity;


import jakarta.persistence.*;
import lombok.*;
import main.als.problem.entity.GroupProblem;
import main.als.user.entity.User;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name="study_groups")
public class Group {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String leader;

    @Column(name = "deposit_amount", precision = 10, scale = 2)
    private BigDecimal depositAmount;


    private LocalDateTime createdAt;
    private LocalDateTime deadline; // 모집 마감일

    @Column(name = "study_end_date", nullable = false)
    private LocalDateTime studyEndDate; // 스터디 종료일

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL)
    @Builder.Default
    private List<UserGroup> userGroups = new ArrayList<>();

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL)
    @Builder.Default
    private List<GroupProblem> groupProblems = new ArrayList<>();
}
