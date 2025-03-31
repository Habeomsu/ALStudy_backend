package main.als.grouptest;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import main.als.group.entity.Group;
import main.als.group.repository.GroupRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@ActiveProfiles("test")
public class GroupRepositoryTest {

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private EntityManager entityManager;

    LocalDateTime now = LocalDateTime.of(2025, 4, 1, 12, 0, 0);
    BigDecimal depositAmount = BigDecimal.valueOf(1000);

    @Test
    @Transactional
    @DisplayName("그룹 저장 테스트")
    public void saveTest(){


        Group group = Group.builder()
                .name("name")
                .password("password")
                .leader("leader")
                .depositAmount(depositAmount)
                .createdAt(now)
                .deadline(now)
                .studyEndDate(now)
                .build();

        Group savedGroup = groupRepository.save(group);

        entityManager.flush();  // 실제 DB에 즉시 반영
        entityManager.clear();  // 영속성 컨텍스트 초기화

        // 다시 조회
        Group foundGroup = groupRepository.findById(savedGroup.getId())
                .orElseThrow(() -> new AssertionError("저장한 그룹을 찾을 수 없습니다."));

        // 실제로 DB에 저장된 데이터를 검증
        assertEquals(group.getName(), foundGroup.getName());
        assertEquals(group.getPassword(), foundGroup.getPassword());
        assertEquals(group.getLeader(), foundGroup.getLeader());
        assertEquals(0, depositAmount.compareTo(foundGroup.getDepositAmount()));
        assertEquals(group.getCreatedAt(), foundGroup.getCreatedAt());
        assertEquals(group.getDeadline(), foundGroup.getDeadline());
        assertEquals(group.getStudyEndDate(), foundGroup.getStudyEndDate());
    }

    @Test
    @Transactional
    @DisplayName("findAll 테스트")
    public void findAllTest(){


        Group group1 = Group.builder()
                .name("name1")
                .password("password1")
                .leader("leader1")
                .depositAmount(depositAmount)
                .createdAt(now)
                .deadline(now)
                .studyEndDate(now)
                .build();

        Group group2 = Group.builder()
                .name("name2")
                .password("password2")
                .leader("leader2")
                .depositAmount(depositAmount)
                .createdAt(now)
                .deadline(now)
                .studyEndDate(now)
                .build();

        Group savedGroup = groupRepository.save(group1);
        Group savedGroup2 = groupRepository.save(group2);

        entityManager.flush();
        entityManager.clear();

        List<Group> groups = groupRepository.findAll();
        assertEquals(2, groups.size(), "저장한 그룹 개수와 일치해야 합니다.");

        Group foundGroup1 = groups.stream()
                .filter(g -> "name1".equals(g.getName()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("group1이 존재하지 않습니다."));

        Group foundGroup2 = groups.stream()
                .filter(g -> "name2".equals(g.getName()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("group2가 존재하지 않습니다."));

        assertEquals("password1", foundGroup1.getPassword());
        assertEquals("leader1", foundGroup1.getLeader());
        assertEquals(0, depositAmount.compareTo(foundGroup1.getDepositAmount()));

        assertEquals("password2", foundGroup2.getPassword());
        assertEquals("leader2", foundGroup2.getLeader());
        assertEquals(0, depositAmount.compareTo(foundGroup2.getDepositAmount()));

    }

    @Test
    @Transactional
    @DisplayName("deleteById 테스트")
    public void deleteByIdTest(){

        Group group1 = Group.builder()
                .name("name1")
                .password("password1")
                .leader("leader1")
                .depositAmount(depositAmount)
                .createdAt(now)
                .deadline(now)
                .studyEndDate(now)
                .build();

        Group savedGroup = groupRepository.save(group1);

        entityManager.flush();
        entityManager.clear();

        groupRepository.deleteById(savedGroup.getId());

        entityManager.flush();
        entityManager.clear();


        Optional<Group> deletedGroup = groupRepository.findById(savedGroup.getId());
        assertTrue(deletedGroup.isEmpty());

    }

    @Test
    @Transactional
    @DisplayName("findById 테스트")
    public void findByIdTest(){

        Group group1 = Group.builder()
                .name("name1")
                .password("password1")
                .leader("leader1")
                .depositAmount(depositAmount)
                .createdAt(now)
                .deadline(now)
                .studyEndDate(now)
                .build();

        Group savedGroup = groupRepository.save(group1);

        entityManager.flush();
        entityManager.clear();

        Optional<Group> foundGroup = groupRepository.findById(savedGroup.getId());
        assertTrue(foundGroup.isPresent());
        assertEquals(group1.getName(), foundGroup.get().getName());
        assertEquals(group1.getPassword(), foundGroup.get().getPassword());
    }

    @Test
    @Transactional
    @DisplayName("findExpiredGroups 테스트")
    public void findExpiredGroupsTest(){

        Group group1 = Group.builder()
                .name("name1")
                .password("password1")
                .leader("leader1")
                .depositAmount(depositAmount)
                .createdAt(now)
                .deadline(now)
                .studyEndDate(now)
                .build();

        Group group2 = Group.builder()
                .name("name2")
                .password("password2")
                .leader("leader2")
                .depositAmount(depositAmount)
                .createdAt(now)
                .deadline(now)
                .studyEndDate(now.plusDays(3))
                .build();

        Group savedGroup = groupRepository.save(group1);
        Group savedGroup2 = groupRepository.save(group2);

        entityManager.flush();
        entityManager.clear();

        LocalDateTime next = now.plusDays(1);
        List<Group> expiredGroups = groupRepository.findExpiredGroups(next);
        assertEquals(1, expiredGroups.size());
        assertEquals(savedGroup.getName(), expiredGroups.get(0).getName());
        assertEquals(savedGroup.getPassword(), expiredGroups.get(0).getPassword());

    }

    @Test
    @Transactional
    @DisplayName("findAllByDeadlineAfter 테스트")
    public void findAllByDeadlineAfterTest(){

        Group group1 = Group.builder()
                .name("name1")
                .password("password1")
                .leader("leader1")
                .depositAmount(depositAmount)
                .createdAt(now)
                .deadline(now)
                .studyEndDate(now)
                .build();

        Group group2 = Group.builder()
                .name("name2")
                .password("password2")
                .leader("leader2")
                .depositAmount(depositAmount)
                .createdAt(now)
                .deadline(now.plusDays(3))
                .studyEndDate(now)
                .build();

        Group savedGroup = groupRepository.save(group1);
        Group savedGroup2 = groupRepository.save(group2);

        entityManager.flush();
        entityManager.clear();

        LocalDateTime next = now.plusDays(1);

        Pageable pageable = PageRequest.of(0, 10);
        Page<Group> result = groupRepository.findAllByDeadlineAfter(now, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(savedGroup2.getName(), result.getContent().get(0).getName());
        assertEquals(savedGroup2.getPassword(), result.getContent().get(0).getPassword());

    }

    @Test
    @Transactional
    @DisplayName("findByNameContainingAndDeadlineAfter 테스트(이름으로 검색)")
    public void findByNameContainingAndDeadlineAfterTest(){

        Group group1 = Group.builder()
                .name("name1")
                .password("password1")
                .leader("leader1")
                .depositAmount(depositAmount)
                .createdAt(now)
                .deadline(now)
                .studyEndDate(now)
                .build();

        Group group2 = Group.builder()
                .name("name2")
                .password("password2")
                .leader("leader2")
                .depositAmount(depositAmount)
                .createdAt(now)
                .deadline(now.plusDays(3))
                .studyEndDate(now)
                .build();

        Group savedGroup = groupRepository.save(group1);
        Group savedGroup2 = groupRepository.save(group2);

        entityManager.flush();
        entityManager.clear();

        Pageable pageable = PageRequest.of(0, 10);
        Page<Group> groups = groupRepository.findByNameContainingAndDeadlineAfter("na", now, pageable);

        assertEquals(1, groups.getTotalElements());
        assertEquals(savedGroup2.getName(), groups.getContent().get(0).getName());
        assertEquals(savedGroup2.getPassword(), groups.getContent().get(0).getPassword());

    }

}
