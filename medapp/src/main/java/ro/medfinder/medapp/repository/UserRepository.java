package ro.medfinder.medapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ro.medfinder.medapp.entity.User;
import ro.medfinder.medapp.entity.enums.Role;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    List<User> findByRole(Role role);

    long countByRole(Role role);
}
