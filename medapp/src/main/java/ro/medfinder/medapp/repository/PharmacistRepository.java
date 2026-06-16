package ro.medfinder.medapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ro.medfinder.medapp.entity.Pharmacist;

import java.util.List;

public interface PharmacistRepository extends JpaRepository<Pharmacist, Long> {

    List<Pharmacist> findByLocationId(Long locationId);

    List<Pharmacist> findByLocationPharmacyOwnerId(Long ownerId);
    
    Page<Pharmacist> findByLocationPharmacyOwnerId(Long ownerId, Pageable pageable);
}
