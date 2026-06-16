package ro.medfinder.medapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ro.medfinder.medapp.entity.Location;

import java.util.List;

public interface LocationRepository extends JpaRepository<Location, Long> {

    List<Location> findByPharmacyId(Long pharmacyId);

    List<Location> findByPharmacyOwnerId(Long ownerId);
}
