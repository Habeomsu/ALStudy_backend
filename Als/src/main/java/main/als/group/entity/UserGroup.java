package main.als.group.entity;

import jakarta.persistence.*;
import lombok.*;
import main.als.user.entity.User;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name="user_groups")
public class UserGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "group_id")
    private Group group;

    @Column(name = "user_deposit_amount", precision = 10, scale = 2)
    private BigDecimal userDepositAmount;

}
