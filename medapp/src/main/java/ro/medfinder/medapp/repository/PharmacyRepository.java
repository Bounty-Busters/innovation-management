package ro.medfinder.medapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ro.medfinder.medapp.entity.Pharmacy;

import java.util.List;

public interface PharmacyRepository extends JpaRepository<Pharmacy, Long> {

    List<Pharmacy> findByOwnerId(Long ownerId);

    long countByActiveTrue();
}
