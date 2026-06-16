package ro.medfinder.medapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ro.medfinder.medapp.entity.MedStock;

import java.util.List;
import java.util.Optional;

public interface MedStockRepository extends JpaRepository<MedStock, Long> {

    List<MedStock> findByLocationId(Long locationId);

    Optional<MedStock> findByLocationIdAndMedicationId(Long locationId, Long medicationId);

    long countByLocationId(Long locationId);

    List<MedStock> findByLocationPharmacyOwnerId(Long ownerId);

    // Phase 3: Dashboard Charts (Low stock)
    List<MedStock> findByQuantityLessThanEqual(int quantity);
    List<MedStock> findByLocationPharmacyOwnerIdAndQuantityLessThanEqual(Long ownerId, int quantity);
    List<MedStock> findByLocationIdAndQuantityLessThanEqual(Long locationId, int quantity);
}
