Salut! Te rog să acționezi ca un Senior Java/Spring Boot Backend Developer. Sarcina ta este să generezi clasele entităților JPA (@Entity) pentru proiectul nostru, "MedFinder" (un agregator de farmacii cu funcționalitate de Click & Collect).

**Contextul proiectului:**
- **Tech Stack:** Spring Boot 3.x, Java 17, Gradle, Hibernate/JPA, Bază de date relațională.
- **Pachetul de bază:** `ro.medfinder.medapp.entity`

**Ce trebuie să folosești ca sursă de adevăr:**
1. Citește fișierul `entity_structure_proposal.md` aflat în rădăcina proiectului. Acolo vei găsi structura FINALĂ și validată a tuturor celor 10 entități, cele 7 enum-uri, tipurile de date exacte, numele coloanelor și relațiile dintre ele. RESPECTĂ-L STRICT!
2. Citește fișierul `AGENTS.md` pentru a înțelege contextul de business (în special partea de EAN/CIM și strategia de sincronizare via Cronjob/Scheduled).

**Cerințe specifice de implementare:**
1. **Lombok:** Te rog să folosești adnotările Lombok (`@Getter`, `@Setter`, `@NoArgsConstructor`, `@AllArgsConstructor`, `@Builder`) pentru a păstra codul curat. Adaugă și `@EqualsAndHashCode(callSuper = true)` unde este cazul, dar ai grijă la stack overflow în relațiile bidirecționale (exclude câmpurile de relație din `EqualsAndHashCode` și `ToString`).
2. **BaseEntity:** Creează clasa `BaseEntity` cu `@MappedSuperclass`, care să conțină `id` (Long, auto-generat), `createdAt` (`@CreationTimestamp`) și `updatedAt` (`@UpdateTimestamp`). Toate entitățile trebuie să extindă această clasă.
3. **Moștenire User:** Implementează clasa `User` folosind strategia `@Inheritance(strategy = InheritanceType.SINGLE_TABLE)`. Creează subclasele `Client`, `PharmOwner` și `Pharmacist` exact cum e descris în document. Atenție: `SuperUser` nu are subclasă, este reprezentat doar prin `Role.SUPER_USER`.
4. **Enum-uri:** Creează fișiere separate pentru fiecare Enum (`Role`, `OrderStatus`, `MedForm`, `NotificationType`, `NotificationStatus`, `SyncType`, `SyncStatus`) în pachetul `ro.medfinder.medapp.entity.enums`.
5. **Relații JPA:** Mapează corect relațiile (`@OneToMany`, `@ManyToOne`, etc.) folosind `mappedBy` unde e cazul pentru relații bidirecționale. Pentru `MedStock` respectă constrângerea compusă `UNIQUE(location_id, medication_id)`.
6. **Nomenclatură:** Numele claselor trebuie să fie la singular (ex: `User`, nu `Users`). Numele coloanelor în baza de date (`@Column(name = "...")`) trebuie să fie în format `snake_case`, iar atributele clasei în `camelCase`.

**Plan de execuție:**
1. Analizează cele două fișiere (`entity_structure_proposal.md` și `AGENTS.md`).
2. Scrie un scurt `implementation_plan.md` în care să listezi fișierele Java pe care urmează să le creezi și așteaptă aprobarea mea.
3. După aprobare, generează codul și plasează-l în structura de foldere corespunzătoare (`src/main/java/ro/medfinder/medapp/entity/...`).

Te rog să îmi confirmi când ai înțeles cerințele și ești gata să începi.
