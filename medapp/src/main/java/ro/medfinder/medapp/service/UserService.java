package ro.medfinder.medapp.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.medfinder.medapp.dto.RegisterRequest;
import ro.medfinder.medapp.dto.UserForm;
import ro.medfinder.medapp.entity.*;
import ro.medfinder.medapp.entity.enums.Role;
import ro.medfinder.medapp.repository.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ClientRepository clientRepository;
    private final PharmOwnerRepository pharmOwnerRepository;
    private final PharmacistRepository pharmacistRepository;
    private final LocationRepository locationRepository;

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
        } else if (role == Role.CLIENT) {
            user = Client.builder()
                    .email(request.getEmail())
                    .password(passwordEncoder.encode(request.getPassword()))
                    .firstName(request.getFirstName())
                    .lastName(request.getLastName())
                    .phone(request.getPhone())
                    .role(Role.CLIENT)
                    .enabled(true)
                    .build();
        } else {
            throw new IllegalArgumentException("Invalid role for registration");
        }

        userRepository.save(user);
    }

    // ── Phase 2: User management methods ─────────────────────────

    public Page<Client> getAllClients(Pageable pageable) {
        return clientRepository.findAll(pageable);
    }

    public Page<PharmOwner> getAllPharmOwners(Pageable pageable) {
        return pharmOwnerRepository.findAll(pageable);
    }

    public Page<Pharmacist> getPharmacistsForUser(User user, Pageable pageable) {
        if (user.getRole() == Role.SUPER_USER) {
            return pharmacistRepository.findAll(pageable);
        }
        return pharmacistRepository.findByLocationPharmacyOwnerId(user.getId(), pageable);
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    @Transactional
    public void updateUser(Long id, UserForm form) {
        User user = getUserById(id);

        user.setFirstName(form.getFirstName());
        user.setLastName(form.getLastName());
        user.setPhone(form.getPhone());

        // Email change — check uniqueness
        if (!user.getEmail().equals(form.getEmail())) {
            if (userRepository.existsByEmail(form.getEmail())) {
                throw new IllegalArgumentException("Email already exists");
            }
            user.setEmail(form.getEmail());
        }

        // Role-specific fields
        if (user instanceof PharmOwner pharmOwner) {
            pharmOwner.setCompanyName(form.getCompanyName());
        }

        if (user instanceof Pharmacist pharmacist && form.getLocationId() != null) {
            Location location = locationRepository.findById(form.getLocationId())
                    .orElseThrow(() -> new IllegalArgumentException("Location not found"));
            pharmacist.setLocation(location);
        }

        userRepository.save(user);
    }

    @Transactional
    public void toggleEnabled(Long id) {
        User user = getUserById(id);
        user.setEnabled(!user.getEnabled());
        userRepository.save(user);
    }
}
