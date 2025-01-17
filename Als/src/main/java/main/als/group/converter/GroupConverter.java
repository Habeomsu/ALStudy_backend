package main.als.group.converter;

import main.als.group.dto.GroupRequestDto;
import main.als.group.dto.GroupResponseDto;
import main.als.group.entity.Group;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.stream.Collectors;

public class GroupConverter {

    public static GroupResponseDto.AllGroupDto toAllGroupDto(Group group) {
        return GroupResponseDto.AllGroupDto.builder()
                .id(group.getId())
                .groupname(group.getName())
                .username(group.getLeader())
                .depositAmount(group.getDepositAmount())
                .createdAt(group.getCreatedAt())
                .deadline(group.getDeadline())
                .stutyEndDate(group.getStudyEndDate())
                .build();
    }

    public static List<GroupResponseDto.AllGroupDto> toAllGroupDto(List<Group> groups) {
        return groups.stream()
                .map(GroupConverter::toAllGroupDto)
                .collect(Collectors.toList());
    }

    public static GroupResponseDto.SearchGroups toSearchGroupDto(Page<Group> groups) {
        return GroupResponseDto.SearchGroups.builder()
                .groupResDtos(toAllGroupDto(groups.getContent()))
                .isFirst(groups.isFirst())
                .isLast(groups.isLast())
                .listSize(groups.getTotalPages())
                .totalElements(groups.getTotalElements())
                .build();
    }


}
