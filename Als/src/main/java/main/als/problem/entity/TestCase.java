package main.als.problem.entity;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "test_cases")
public class TestCase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_id", nullable = false)
    private Problem problem; // 해당 테스트 케이스가 속하는 문제

    @Column(columnDefinition = "TEXT")
    private String input; // 테스트 입력 데이터

    private String expectedOutput; // 예상 출력 데이터
}
