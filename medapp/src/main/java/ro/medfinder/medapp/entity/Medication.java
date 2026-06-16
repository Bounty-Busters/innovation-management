package ro.medfinder.medapp.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import ro.medfinder.medapp.entity.enums.MedForm;

import java.util.ArrayList;
import java.util.List;

/**
 * Medicament din nomenclator.
 * Sursa datelor: import CSV/Excel din ANMDMR (Nomenclatorul medicamentelor de uz uman).
 * Identificare: EAN (European Article Number) — UNIQUE.
 */
@Entity
@Table(name = "medications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ToString(callSuper = true, exclude = {"medStocks"})
@EqualsAndHashCode(callSuper = true, exclude = {"medStocks"})
public class Medication extends BaseEntity {

    @Column(name = "ean", nullable = false, unique = true, length = 13)
    private String ean;

    @Column(name = "cim_code", length = 20)
    private String cimCode;

    @Column(name = "atc_code", length = 10)
    private String atcCode;

    @Column(name = "name", nullable = false, length = 300)
    private String name;

    @Column(name = "active_substance", length = 300)
    private String activeSubstance;

    @Column(name = "manufacturer", length = 200)
    private String manufacturer;

    @Column(name = "dosage", length = 50)
    private String dosage;

    @Enumerated(EnumType.STRING)
    @Column(name = "form", length = 30)
    private MedForm form;

    @Column(name = "prescription_required", nullable = false)
    @Builder.Default
    private Boolean prescriptionRequired = false;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "category", length = 100)
    private String category;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    // ── Relații ──────────────────────────────────────────────────

    /** Stocurile din diverse locații (farmacii). */
    @OneToMany(mappedBy = "medication", fetch = FetchType.LAZY)
    @Builder.Default
    private List<MedStock> medStocks = new ArrayList<>();
}
