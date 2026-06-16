package ro.medfinder.medapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ro.medfinder.medapp.entity.MedStock;

import java.util.List;
import java.util.Optional;

public interface MedStockRepository extends JpaRepository<MedStock, Long> {

    List<MedStock> findByLocationId(Long locationId);

    Optional<MedStock> findByLocationIdAndMedicationId(Long locationId, Long medicationId);
}
