package ro.medfinder.medapp.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.medfinder.medapp.dto.MedicationForm;
import ro.medfinder.medapp.entity.Medication;
import ro.medfinder.medapp.repository.MedicationRepository;

@Service
@RequiredArgsConstructor
public class MedicationService {

    private final MedicationRepository medicationRepository;

    public Page<Medication> getAllMedications(String search, Pageable pageable) {
        if (search != null && !search.trim().isEmpty()) {
            return medicationRepository.findByNameContainingIgnoreCase(search.trim(), pageable);
        }
        return medicationRepository.findAll(pageable);
    }

    public Medication getMedicationById(Long id) {
        return medicationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Medication not found"));
    }

    @Transactional
    public void createMedication(MedicationForm form) {
        if (medicationRepository.existsByEan(form.getEan())) {
            throw new IllegalArgumentException("EAN already exists");
        }

        Medication medication = Medication.builder()
                .ean(form.getEan())
                .name(form.getName())
                .activeSubstance(form.getActiveSubstance())
                .form(form.getForm())
                .dosage(form.getDosage())
                .category(form.getCategory())
                .prescriptionRequired(form.getPrescriptionRequired() != null && form.getPrescriptionRequired())
                .description(form.getDescription())
                .build();

        medicationRepository.save(medication);
    }

    @Transactional
    public void updateMedication(Long id, MedicationForm form) {
        Medication medication = getMedicationById(id);

        if (!medication.getEan().equals(form.getEan()) && medicationRepository.existsByEan(form.getEan())) {
            throw new IllegalArgumentException("EAN already exists");
        }

        medication.setEan(form.getEan());
        medication.setName(form.getName());
        medication.setActiveSubstance(form.getActiveSubstance());
        medication.setForm(form.getForm());
        medication.setDosage(form.getDosage());
        medication.setCategory(form.getCategory());
        medication.setPrescriptionRequired(form.getPrescriptionRequired() != null && form.getPrescriptionRequired());
        medication.setDescription(form.getDescription());

        medicationRepository.save(medication);
    }

    @Transactional
    public void deleteMedication(Long id) {
        medicationRepository.deleteById(id);
    }
}
