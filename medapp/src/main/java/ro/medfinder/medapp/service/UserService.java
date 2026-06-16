package ro.medfinder.medapp.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.medfinder.medapp.dto.RegisterRequest;
import ro.medfinder.medapp.entity.Pharmacist;
import ro.medfinder.medapp.entity.PharmOwner;
import ro.medfinder.medapp.entity.User;
import ro.medfinder.medapp.entity.enums.Role;
import ro.medfinder.medapp.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void registerUser(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        Role role = Role.valueOf(request.getRole());
        User user;

        if (role == Role.PHARM_OWNER) {
            user = PharmOwner.builder()
                    .email(request.getEmail())
                    .password(passwordEncoder.encode(request.getPassword()))
                    .firstName(request.getFirstName())
                    .lastName(request.getLastName())
                    .phone(request.getPhone())
                    .role(Role.PHARM_OWNER)
                    .enabled(true)
                    .companyName(request.getCompanyName())
                    .build();
        } else if (role == Role.PHARMACIST) {
            user = Pharmacist.builder()
                    .email(request.getEmail())
                    .password(passwordEncoder.encode(request.getPassword()))
                    .firstName(request.getFirstName())
                    .lastName(request.getLastName())
                    .phone(request.getPhone())
                    .role(Role.PHARMACIST)
                    .enabled(true)
                    .build();
        } else {
            throw new IllegalArgumentException("Invalid role for registration");
        }

        userRepository.save(user);
    }
}
