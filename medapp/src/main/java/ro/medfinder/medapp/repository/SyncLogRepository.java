package ro.medfinder.medapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ro.medfinder.medapp.entity.SyncLog;

import org.springframework.data.domain.Page;
import java.util.List;

public interface SyncLogRepository extends JpaRepository<SyncLog, Long> {

    Page<SyncLog> findByPharmacyId(Long pharmacyId, org.springframework.data.domain.Pageable pageable);

    Page<SyncLog> findByPharmacyOwnerId(Long ownerId, org.springframework.data.domain.Pageable pageable);
}
