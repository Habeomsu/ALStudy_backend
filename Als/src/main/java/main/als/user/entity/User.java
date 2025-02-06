package main.als.user.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import main.als.group.entity.UserGroup;
import main.als.problem.entity.Submission;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Builder
@Table(name = "users")
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    @NotNull(message = "User must not be null")
    private String username;

    @Column(nullable = false)
    @NotNull(message = "password must not be null")
    private String password;

    @Enumerated(EnumType.STRING) // Enum 값을 문자열로 저장
    @Column(nullable = false)
    private Role role;

    @Column(nullable = false)
    private String customerId;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    @Builder.Default //builder사용시 null값 초기화 방지
    private List<UserGroup> userGroups = new ArrayList<>(); // 그룹과의 관계

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Submission> submissions = new ArrayList<>(); // 자신이 푼 문제 목록

}
