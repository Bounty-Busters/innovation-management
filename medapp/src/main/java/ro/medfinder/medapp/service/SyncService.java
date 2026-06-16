package ro.medfinder.medapp.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ro.medfinder.medapp.dto.SyncRequestForm;
import ro.medfinder.medapp.entity.*;
import ro.medfinder.medapp.entity.enums.Role;
import ro.medfinder.medapp.entity.enums.SyncStatus;
import ro.medfinder.medapp.entity.enums.SyncType;
import ro.medfinder.medapp.repository.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SyncService {

    private final SyncLogRepository syncLogRepository;
    private final PharmacyRepository pharmacyRepository;
    private final LocationRepository locationRepository;
    private final MedicationRepository medicationRepository;
    private final MedStockRepository medStockRepository;

    public Page<SyncLog> getLogsForUser(User user, Pageable pageable) {
        if (user.getRole() == Role.SUPER_USER) {
            return syncLogRepository.findAll(pageable);
        } else if (user.getRole() == Role.PHARM_OWNER) {
            return syncLogRepository.findByPharmacyOwnerId(user.getId(), pageable);
        } else {
            // Pharmacist usually only sees their own location, but SyncLog is mapped to Pharmacy.
            Long pharmId = ((Pharmacist) user).getLocation().getPharmacy().getId();
            return syncLogRepository.findByPharmacyId(pharmId, pageable);
        }
    }

    @Transactional
    public void triggerSync(SyncRequestForm form, User user) {
        Pharmacy pharmacy = pharmacyRepository.findById(form.getPharmacyId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid pharmacy ID"));

        SyncLog syncLog = SyncLog.builder()
                .pharmacy(pharmacy)
                .syncType(SyncType.CSV)
                .status(SyncStatus.IN_PROGRESS)
                .fileName(form.getFile() != null ? form.getFile().getOriginalFilename() : "N/A")
                .triggeredBy(user)
                .startedAt(LocalDateTime.now())
                .totalRecords(0)
                .processedRecords(0)
                .failedRecords(0)
                .build();

        syncLog = syncLogRepository.save(syncLog);

        if (form.getFile() == null || form.getFile().isEmpty()) {
            failSync(syncLog, "No file uploaded");
            return;
        }

        // Determine target location. For simplicity, we update stock in the first active location of the pharmacy.
        Location targetLocation = locationRepository.findByPharmacyId(pharmacy.getId()).stream()
                .filter(l -> Boolean.TRUE.equals(l.getActive()))
                .findFirst()
                .orElse(null);

        if (targetLocation == null) {
            failSync(syncLog, "Pharmacy has no active locations to update stock");
            return;
        }

        int total = 0;
        int processed = 0;
        int failed = 0;
        StringBuilder errorDetails = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(form.getFile().getInputStream()))) {
            String line;
            boolean isFirstLine = true;
            
            while ((line = reader.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue; // Skip header
                }
                
                total++;
                String[] columns = line.split(",");
                if (columns.length < 3) {
                    failed++;
                    errorDetails.append("Row ").append(total).append(": Invalid columns count; ");
                    continue;
                }
                
                String ean = columns[0].trim();
                String qtyStr = columns[1].trim();
                String priceStr = columns[2].trim();
                
                try {
                    int quantity = Integer.parseInt(qtyStr);
                    BigDecimal price = new BigDecimal(priceStr);
                    
                    Optional<Medication> medOpt = medicationRepository.findByEan(ean);
                    if (medOpt.isEmpty()) {
                        failed++;
                        errorDetails.append("Row ").append(total).append(": EAN ").append(ean).append(" not found; ");
                        continue;
                    }
                    
                    Medication medication = medOpt.get();
                    
                    // Update or create stock
                    Optional<MedStock> existingStock = medStockRepository.findByLocationIdAndMedicationId(targetLocation.getId(), medication.getId());
                    if (existingStock.isPresent()) {
                        MedStock stock = existingStock.get();
                        stock.setQuantity(quantity);
                        stock.setPrice(price);
                        medStockRepository.save(stock);
                    } else {
                        MedStock newStock = MedStock.builder()
                                .location(targetLocation)
                                .medication(medication)
                                .quantity(quantity)
                                .price(price)
                                .build();
                        medStockRepository.save(newStock);
                    }
                    processed++;
                    
                } catch (Exception e) {
                    failed++;
                    errorDetails.append("Row ").append(total).append(": ").append(e.getMessage()).append("; ");
                }
            }
            
            syncLog.setTotalRecords(total);
            syncLog.setProcessedRecords(processed);
            syncLog.setFailedRecords(failed);
            
            if (failed == 0 && processed > 0) {
                syncLog.setStatus(SyncStatus.SUCCESS);
            } else if (processed > 0 && failed > 0) {
                syncLog.setStatus(SyncStatus.PARTIAL_FAILURE);
                syncLog.setErrorDetails(errorDetails.toString());
            } else {
                syncLog.setStatus(SyncStatus.FAILED);
                syncLog.setErrorDetails(errorDetails.toString());
            }
            
        } catch (Exception e) {
            log.error("Failed to parse CSV file", e);
            failSync(syncLog, "File processing error: " + e.getMessage());
            return;
        }
        
        syncLog.setCompletedAt(LocalDateTime.now());
        syncLogRepository.save(syncLog);
    }
    
    private void failSync(SyncLog log, String reason) {
        log.setStatus(SyncStatus.FAILED);
        log.setErrorDetails(reason);
        log.setCompletedAt(LocalDateTime.now());
        syncLogRepository.save(log);
    }
}
