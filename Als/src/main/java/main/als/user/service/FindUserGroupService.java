package main.als.user.service;

import main.als.apiPayload.code.status.ErrorStatus;
import main.als.apiPayload.exception.GeneralException;
import main.als.group.entity.UserGroup;
import main.als.page.PostPagingDto;
import main.als.user.entity.User;
import main.als.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Sort;
import java.util.List;

@Service
public class FindUserGroupService {

    private final UserRepository userRepository;
    public FindUserGroupService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Page<UserGroup> userGroups(String username, PostPagingDto.PagingDto pagingDto) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new GeneralException(ErrorStatus._USERNAME_NOT_FOUND);
        }
        Sort sort = Sort.by(Sort.Direction.fromString(pagingDto.getSort()),"id");
        Pageable pageable = PageRequest.of(pagingDto.getPage(), pagingDto.getSize(), sort);
        return userRepository.findUserGroupsByUsername(username, pageable);
    }

}
