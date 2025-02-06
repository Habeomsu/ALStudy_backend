package main.als.user.service;

import main.als.apiPayload.code.status.ErrorStatus;
import main.als.apiPayload.exception.GeneralException;
import main.als.user.dto.JoinDto;
import main.als.user.entity.Role;
import main.als.user.entity.User;
import main.als.user.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class JoinService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public JoinService(UserRepository userRepository,BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    public void joinProcess(JoinDto joinDto) {

        String username=joinDto.getUsername();
        String password=joinDto.getPassword();

        boolean isexist = userRepository.existsByUsername(username);
        if (isexist) {
            throw new GeneralException(ErrorStatus._EXIST_USERNAME);
        }

        String customerId = UUID.randomUUID().toString();

        User data = User.builder()
                    .username(username)
                    .password(bCryptPasswordEncoder.encode(password))
                    .role(Role.ROLE_USER)
                    .customerId(customerId)
                    .build();

        userRepository.save(data);

    }


}
