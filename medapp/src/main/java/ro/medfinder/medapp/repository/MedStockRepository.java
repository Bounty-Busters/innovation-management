package ro.medfinder.medapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ro.medfinder.medapp.entity.MedStock;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MedStockRepository extends JpaRepository<MedStock, Long> {

    List<MedStock> findByLocationId(Long locationId);

    Optional<MedStock> findByLocationIdAndMedicationId(Long locationId, Long medicationId);

    long countByLocationId(Long locationId);

    List<MedStock> findByLocationPharmacyOwnerId(Long ownerId);

    // Phase 3: Dashboard Charts (Low stock)
    List<MedStock> findByQuantityLessThanEqual(int quantity);
    List<MedStock> findByLocationPharmacyOwnerIdAndQuantityLessThanEqual(Long ownerId, int quantity);
    List<MedStock> findByLocationIdAndQuantityLessThanEqual(Long locationId, int quantity);

    // Storefront: get all stocks for an EAN
    List<MedStock> findByMedicationEanAndQuantityGreaterThan(String ean, int quantity);

    // Storefront: approximate bounding box for nearby medications
    @Query("SELECT m FROM MedStock m WHERE m.quantity > 0 AND m.location.latitude BETWEEN :minLat AND :maxLat AND m.location.longitude BETWEEN :minLon AND :maxLon ORDER BY m.id DESC")
    List<MedStock> findNearbyInStockOrderByLastSyncedDesc(
            @Param("minLat") Double minLat,
            @Param("maxLat") Double maxLat,
            @Param("minLon") Double minLon,
            @Param("maxLon") Double maxLon
    );
}
