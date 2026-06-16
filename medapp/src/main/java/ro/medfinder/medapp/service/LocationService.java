package ro.medfinder.medapp.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.medfinder.medapp.dto.LocationForm;
import ro.medfinder.medapp.entity.*;
import ro.medfinder.medapp.entity.enums.Role;
import ro.medfinder.medapp.repository.LocationRepository;
import ro.medfinder.medapp.repository.PharmacyRepository;
import ro.medfinder.medapp.repository.WorkingHourRepository;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LocationService {

    private final LocationRepository locationRepository;
    private final PharmacyRepository pharmacyRepository;
    private final WorkingHourRepository workingHourRepository;

    public Page<Location> getLocationsForUser(User user, Pageable pageable) {
        if (user.getRole() == Role.SUPER_USER) {
            return locationRepository.findAll(pageable);
        } else if (user.getRole() == Role.PHARM_OWNER) {
            return locationRepository.findByPharmacyOwnerId(user.getId(), pageable);
        } else if (user.getRole() == Role.PHARMACIST) {
            Location loc = ((Pharmacist) user).getLocation();
            if (loc != null) {
                return new PageImpl<>(List.of(loc), pageable, 1);
            }
        }
        return Page.empty(pageable);
    }

    public Location getLocationById(Long id) {
        return locationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Location not found"));
    }

    @Transactional
    public void createLocation(LocationForm form, User currentUser) {
        Pharmacy pharmacy = pharmacyRepository.findById(form.getPharmacyId())
                .orElseThrow(() -> new IllegalArgumentException("Pharmacy not found"));

        checkPharmacyOwnership(pharmacy, currentUser);

        Location location = Location.builder()
                .name(form.getName())
                .address(form.getAddress())
                .city(form.getCity())
                .county(form.getCounty())
                .postalCode(form.getPostalCode())
                .latitude(form.getLatitude())
                .longitude(form.getLongitude())
                .phone(form.getPhone())
                .active(form.getActive() != null ? form.getActive() : true)
                .pharmacy(pharmacy)
                .build();

        locationRepository.save(location);
        saveWorkingHours(location, form.getWorkingHours());
    }

    @Transactional
    public void updateLocation(Long id, LocationForm form, User currentUser) {
        Location location = getLocationById(id);
        checkPharmacyOwnership(location.getPharmacy(), currentUser);

        location.setName(form.getName());
        location.setAddress(form.getAddress());
        location.setCity(form.getCity());
        location.setCounty(form.getCounty());
        location.setPostalCode(form.getPostalCode());
        location.setLatitude(form.getLatitude());
        location.setLongitude(form.getLongitude());
        location.setPhone(form.getPhone());
        location.setActive(form.getActive() != null ? form.getActive() : true);

        if (form.getPharmacyId() != null) {
            Pharmacy pharmacy = pharmacyRepository.findById(form.getPharmacyId())
                    .orElseThrow(() -> new IllegalArgumentException("Pharmacy not found"));
            checkPharmacyOwnership(pharmacy, currentUser);
            location.setPharmacy(pharmacy);
        }

        locationRepository.save(location);

        // Delete old working hours and re-create
        List<WorkingHour> existingHours = workingHourRepository.findByLocationIdOrderByDayOfWeek(id);
        workingHourRepository.deleteAll(existingHours);
        saveWorkingHours(location, form.getWorkingHours());
    }

    public List<WorkingHour> getWorkingHoursForLocation(Long locationId) {
        return workingHourRepository.findByLocationIdOrderByDayOfWeek(locationId);
    }

    private void saveWorkingHours(Location location, List<LocationForm.WorkingHourEntry> entries) {
        if (entries == null || entries.isEmpty()) {
            return;
        }

        for (LocationForm.WorkingHourEntry entry : entries) {
            if (entry.getDayOfWeek() == null || entry.getDayOfWeek().isBlank()) {
                continue;
            }

            boolean closed = entry.getClosed() != null && entry.getClosed();
            LocalTime openTime = null;
            LocalTime closeTime = null;

            if (!closed && entry.getOpenTime() != null && !entry.getOpenTime().isBlank()) {
                openTime = LocalTime.parse(entry.getOpenTime());
            }
            if (!closed && entry.getCloseTime() != null && !entry.getCloseTime().isBlank()) {
                closeTime = LocalTime.parse(entry.getCloseTime());
            }

            WorkingHour wh = WorkingHour.builder()
                    .location(location)
                    .dayOfWeek(DayOfWeek.valueOf(entry.getDayOfWeek()))
                    .openTime(openTime)
                    .closeTime(closeTime)
                    .closed(closed)
                    .build();

            workingHourRepository.save(wh);
        }
    }

    private void checkPharmacyOwnership(Pharmacy pharmacy, User currentUser) {
        if (currentUser.getRole() != Role.SUPER_USER
                && !pharmacy.getOwner().getId().equals(currentUser.getId())) {
            throw new IllegalArgumentException("Access denied");
        }
    }
}
