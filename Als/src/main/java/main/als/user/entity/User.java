package main.als.user.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import main.als.group.entity.UserGroup;
import main.als.problem.entity.Submission;

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

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<UserGroup> userGroups; // 그룹과의 관계

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Submission> submissions; // 자신이 푼 문제 목록

}
