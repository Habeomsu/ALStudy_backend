package main.als.user.service;

import main.als.user.dto.CustomUserDetails;
import main.als.user.entity.User;
import main.als.user.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        User userdata=userRepository.findByUsername(username);

        if (userdata == null)
        {
            throw new UsernameNotFoundException("User not found with username: " + username);
        }

        return new CustomUserDetails(userdata);
    }
}
