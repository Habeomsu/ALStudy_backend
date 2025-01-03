package main.als.group.entity;


import jakarta.persistence.*;
import lombok.*;
import main.als.problem.entity.GroupProblem;
import main.als.user.entity.User;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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

    @ManyToOne
    @JoinColumn(name = "leader_id")
    private User leader;

    @Column(name = "deposit_amount", precision = 10, scale = 2)
    private BigDecimal depositAmount;


    private LocalDateTime createdAt;
    private LocalDateTime deadline; // 모집 마감일

    @Column(name = "study_end_date", nullable = false)
    private LocalDateTime studyEndDate; // 스터디 종료일

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL)
    private List<UserGroup> userGroups;

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL)
    private List<GroupProblem> groupProblems;
}
