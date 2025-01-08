package main.als.user.service;

import main.als.apiPayload.code.status.ErrorStatus;
import main.als.apiPayload.exception.GeneralException;
import main.als.group.entity.UserGroup;
import main.als.user.entity.User;
import main.als.user.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FindUserGroupService {

    private final UserRepository userRepository;
    public FindUserGroupService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<UserGroup> userGroups(String username){

        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new GeneralException(ErrorStatus._USERNAME_NOT_FOUND);
        }

        return user.getUserGroups();

    }

}
