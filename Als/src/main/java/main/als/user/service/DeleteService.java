package main.als.user.service;

import jakarta.transaction.Transactional;
import main.als.apiPayload.code.status.ErrorStatus;
import main.als.apiPayload.exception.GeneralException;
import main.als.user.repository.RefreshRepository;
import main.als.user.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class DeleteService {

    private final UserRepository userRepository;
    private final RefreshRepository refreshRepository;

    public DeleteService(UserRepository userRepository, RefreshRepository refreshRepository) {
        this.userRepository = userRepository;
        this.refreshRepository = refreshRepository;
    }

    @Transactional
    public void deleteUser(String username) {
        // 사용자가 존재하는지 확인
        if (!userRepository.existsByUsername(username)) {
            throw new GeneralException(ErrorStatus._USERNAME_NOT_FOUND);
        }

        // 사용자 삭제
        userRepository.deleteByUsername(username);
        refreshRepository.deleteByUsername(username);
    }

}
