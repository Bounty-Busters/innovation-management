package ro.medfinder.medapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ro.medfinder.medapp.entity.Pharmacy;

import java.util.List;

public interface PharmacyRepository extends JpaRepository<Pharmacy, Long> {

    List<Pharmacy> findByOwnerId(Long ownerId);

    Page<Pharmacy> findByOwnerId(Long ownerId, Pageable pageable);

    long countByActiveTrue();
}
