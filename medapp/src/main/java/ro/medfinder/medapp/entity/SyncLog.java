package ro.medfinder.medapp.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import ro.medfinder.medapp.entity.enums.SyncStatus;
import ro.medfinder.medapp.entity.enums.SyncType;

import java.time.LocalDateTime;

/**
 * Jurnalizarea fiecărui import CSV / ERP, pentru audit și debugging.
 *
 * Strategii de sync (la nivel de service, nu entitate):
 * - Manual (on init): import inițial din CSV/Excel ANMDMR
 * - Automat (cronjob): Spring @Scheduled, rulează periodic
 * - Manual (admin): endpoint REST din admin panel
 *
 * Fiecare execuție creează un rând în această tabelă.
 */
@Entity
@Table(name = "sync_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ToString(callSuper = true, exclude = {"pharmacy", "triggeredBy"})
@EqualsAndHashCode(callSuper = true, exclude = {"pharmacy", "triggeredBy"})
public class SyncLog extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "sync_type", nullable = false, length = 15)
    private SyncType syncType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private SyncStatus status;

    @Column(name = "file_name", length = 300)
    private String fileName;

    @Column(name = "total_records")
    private Integer totalRecords;

    @Column(name = "processed_records")
    private Integer processedRecords;

    @Column(name = "failed_records")
    private Integer failedRecords;

    @Column(name = "error_details", columnDefinition = "TEXT")
    private String errorDetails;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    // ── Relații ──────────────────────────────────────────────────

    /** Farmacia pentru care s-a făcut sync-ul. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pharmacy_id")
    private Pharmacy pharmacy;

    /** Utilizatorul care a declanșat sync-ul (admin/system). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "triggered_by_id")
    private User triggeredBy;
}
