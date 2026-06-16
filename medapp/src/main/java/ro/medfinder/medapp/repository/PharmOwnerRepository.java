package ro.medfinder.medapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ro.medfinder.medapp.entity.PharmOwner;

public interface PharmOwnerRepository extends JpaRepository<PharmOwner, Long> {
    Page<PharmOwner> findAll(Pageable pageable);
}
