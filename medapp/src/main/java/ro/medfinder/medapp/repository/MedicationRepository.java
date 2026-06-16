package ro.medfinder.medapp.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ro.medfinder.medapp.entity.Medication;

import java.util.Optional;

public interface MedicationRepository extends JpaRepository<Medication, Long> {

    Page<Medication> findByNameContainingIgnoreCase(String name, Pageable pageable);

    Optional<Medication> findByEan(String ean);

    boolean existsByEan(String ean);
}
