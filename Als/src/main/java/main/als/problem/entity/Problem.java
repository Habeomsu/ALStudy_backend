package main.als.problem.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "problems")
public class Problem{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title; // 문제 제목
    private String difficultyLevel;

    @Enumerated(EnumType.STRING)
    private ProblemType problemType; // 문제 유형

    private LocalDateTime createdAt; // 생성 시간

    @Column(length = 1000)
    private String description; // 문제 설명
    @Column(length = 1000)
    private String inputDescription;
    @Column(length = 1000)
    private String outputDescription;
    private String exampleInput;
    private String exampleOutput;


    @OneToMany(mappedBy = "problem", cascade = CascadeType.ALL)
    @Builder.Default
    private List<TestCase> testCases = new ArrayList<>();



}
