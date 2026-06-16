package ro.medfinder.medapp.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import ro.medfinder.medapp.dto.PharmacyForm;
import ro.medfinder.medapp.entity.Pharmacy;
import ro.medfinder.medapp.entity.PharmOwner;
import ro.medfinder.medapp.entity.User;
import ro.medfinder.medapp.entity.enums.Role;
import ro.medfinder.medapp.repository.LocationRepository;
import ro.medfinder.medapp.repository.PharmacyRepository;
import ro.medfinder.medapp.repository.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PharmacyService {

    private final PharmacyRepository pharmacyRepository;
    private final UserRepository userRepository;
    private final LocationRepository locationRepository;

    public Page<Pharmacy> getPharmaciesForUser(User user, org.springframework.data.domain.Pageable pageable) {
        if (user.getRole() == Role.SUPER_USER) {
            return pharmacyRepository.findAll(pageable);
        }
        return pharmacyRepository.findByOwnerId(user.getId(), pageable);
    }

    public Pharmacy getPharmacyById(Long id) {
        return pharmacyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Pharmacy not found"));
    }

    @Transactional
    public void createPharmacy(PharmacyForm form, User currentUser) {
        User owner;
        if (currentUser.getRole() == Role.SUPER_USER && form.getOwnerId() != null) {
            owner = userRepository.findById(form.getOwnerId())
                    .orElseThrow(() -> new IllegalArgumentException("Owner not found"));
        } else {
            owner = currentUser;
        }

        Pharmacy pharmacy = Pharmacy.builder()
                .name(form.getName())
                .cui(form.getCui())
                .phone(form.getPhone())
                .email(form.getEmail())
                .website(form.getWebsite())
                .logoUrl(form.getLogoUrl())
                .syncEnabled(form.getSyncEnabled() != null && form.getSyncEnabled())
                .syncEndpointUrl(form.getSyncEndpointUrl())
                .owner(owner)
                .active(true)
                .build();

        pharmacyRepository.save(pharmacy);
    }

    @Transactional
    public void updatePharmacy(Long id, PharmacyForm form, User currentUser) {
        Pharmacy pharmacy = getPharmacyById(id);
        checkOwnership(pharmacy, currentUser);

        pharmacy.setName(form.getName());
        pharmacy.setCui(form.getCui());
        pharmacy.setPhone(form.getPhone());
        pharmacy.setEmail(form.getEmail());
        pharmacy.setWebsite(form.getWebsite());
        pharmacy.setLogoUrl(form.getLogoUrl());
        pharmacy.setSyncEnabled(form.getSyncEnabled() != null && form.getSyncEnabled());
        pharmacy.setSyncEndpointUrl(form.getSyncEndpointUrl());

        if (currentUser.getRole() == Role.SUPER_USER && form.getOwnerId() != null) {
            User owner = userRepository.findById(form.getOwnerId())
                    .orElseThrow(() -> new IllegalArgumentException("Owner not found"));
            pharmacy.setOwner(owner);
        }

        pharmacyRepository.save(pharmacy);
    }

    @Transactional
    public void toggleActive(Long id, User currentUser) {
        Pharmacy pharmacy = getPharmacyById(id);
        checkOwnership(pharmacy, currentUser);
        pharmacy.setActive(!pharmacy.getActive());
        pharmacyRepository.save(pharmacy);
    }

    public long countLocations(Long pharmacyId) {
        return locationRepository.countByPharmacyId(pharmacyId);
    }

    private void checkOwnership(Pharmacy pharmacy, User currentUser) {
        if (currentUser.getRole() != Role.SUPER_USER
                && !pharmacy.getOwner().getId().equals(currentUser.getId())) {
            throw new IllegalArgumentException("Access denied");
        }
    }
}
