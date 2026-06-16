package ro.medfinder.medapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ro.medfinder.medapp.entity.Location;

import java.util.List;

public interface LocationRepository extends JpaRepository<Location, Long> {

    List<Location> findByPharmacyId(Long pharmacyId);

    List<Location> findByPharmacyOwnerId(Long ownerId);
    Page<Location> findByPharmacyOwnerId(Long ownerId, Pageable pageable);


    long countByPharmacyId(Long pharmacyId);
}
