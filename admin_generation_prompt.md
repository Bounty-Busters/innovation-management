Salut! Te rog să acționezi ca un Senior Full-Stack Java/Spring Boot Developer. Sarcina ta este să implementezi **Admin Panel-ul** complet pentru proiectul „MedFinder" — un agregator de farmacii cu funcționalitate Click & Collect.

**Contextul proiectului:**
- **Tech Stack:** Spring Boot 4.x, Java 17, Gradle, Hibernate/JPA, H2 (dev), Thymeleaf, Bootstrap 5, Chart.js, Spring Security
- **Pachetul de bază:** `ro.medfinder.medapp`
- **Entitățile JPA sunt deja implementate** în `ro.medfinder.medapp.entity` — NU le modifica.

**Ce trebuie să folosești ca sursă de adevăr:**
1. Citește fișierul `admin_panel_structure.md` aflat în rădăcina proiectului. Acolo vei găsi structura FINALĂ cu toate paginile, rutele, componentele UI, matricea de permisiuni per rol, structura template-urilor Thymeleaf, lista de repositories/services/controllers/DTOs, și planul de implementare pe faze. **RESPECTĂ-L STRICT!**
2. Citește fișierul `entity_structure_proposal.md` pentru a înțelege relațiile dintre entități (cine vede ce, cum se navighează de la User → Pharmacy → Location → MedStock etc.)
3. Citește fișierul `AGENTS.md` pentru contextul de business.

**Cerințe specifice de implementare:**

1. **Dependențe Gradle:** Adaugă în `build.gradle`:
   - `spring-boot-starter-thymeleaf`
   - `spring-boot-starter-security`
   - `nz.net.ultraq.thymeleaf:thymeleaf-layout-dialect`
   - `spring-boot-starter-validation` (pentru Jakarta Bean Validation)

2. **Spring Security:**
   - Form-based login pe `/auth/login`
   - `CustomUserDetailsService` care caută user după email în DB
   - Password encoding cu `BCryptPasswordEncoder`
   - Role-based access control conform matricei din `admin_panel_structure.md`
   - După login → redirect la `/admin/dashboard`
   - CSRF activat (Thymeleaf gestionează automat `th:action`)
   - Sesiuni HTTP standard (fără JWT)

3. **Layout Thymeleaf (Thymeleaf Layout Dialect):**
   - Un layout principal `layout/admin.html` cu:
     - Sidebar (navigare diferită per rol — folosește `sec:authorize` pentru a afișa/ascunde meniuri)
     - Header (navbar cu nume user + buton logout)
     - `layout:fragment="content"` — zona unde fiecare pagină inserează conținutul propriu
     - Footer
   - Paginile de auth (`login.html`, `register.html`) NU folosesc layout-ul admin (sunt standalone, card centrat)

4. **Bootstrap 5 + Bootstrap Icons via CDN:**
   - Include CSS și JS din CDN-ul oficial Bootstrap 5.3
   - Include Bootstrap Icons CDN
   - Un fișier custom `static/css/admin.css` pentru override-uri și styling sidebar
   - Un fișier `static/js/admin.js` pentru logică minimală (toggle sidebar, confirmări delete, etc.)

5. **Controllers Spring MVC:**
   - Fiecare controller returnează un view name Thymeleaf (NU JSON)
   - Folosește `@ModelAttribute` pentru binding formulare
   - Folosește `RedirectAttributes.addFlashAttribute()` pentru mesaje succes/eroare
   - Injectează `@AuthenticationPrincipal` sau `SecurityContextHolder` pentru a obține user-ul curent
   - Filtrează datele per rol (SUPER_USER vede tot, PHARM_OWNER vede doar ale lui, PHARMACIST vede doar locația lui)

6. **Repositories Spring Data JPA:**
   - Extends `JpaRepository<Entity, Long>`
   - Custom query methods prin method naming convention (ex: `findByOwnerId()`)
   - `@Query` JPQL doar unde e necesar (ex: count queries pentru dashboard)

7. **Services:**
   - Fiecare service este `@Service` cu `@Transactional` unde e necesar
   - Business logic + filtrare per rol
   - Validare date (email unic, etc.)

8. **DataLoader (Seed Data):**
   - Implementează un `@Component` cu `CommandLineRunner` care, la pornirea aplicației, verifică dacă DB-ul e gol și, dacă da, inserează date demo:
     - 1 SUPER_USER (admin@medfinder.ro / admin123)
     - 2 PHARM_OWNER cu câte 1-2 farmacii fiecare
     - 3-4 Locații distribuite pe farmacii
     - 5-6 Pharmacists asignați la locații
     - 2-3 Clienți
     - 15-20 Medicamente (realiste — Paracetamol, Ibuprofen, Amoxicilină, etc.)
     - Stocuri (MedStock) pentru locații
     - 5-10 Comenzi în diferite statusuri
   - Parolele trebuie encodate cu `BCryptPasswordEncoder`

9. **Grafice Dashboard (Chart.js):**
   - Datele pentru grafice se trimit ca `th:inline="javascript"` din controller
   - Sau ca atribute `data-*` pe elementele HTML și se citesc din JS
   - Chart.js se include via CDN

10. **Paginare:**
    - Folosește `Pageable` + `Page<T>` din Spring Data
    - Fragment Thymeleaf reutilizabil pentru controalele de paginare

11. **Nomenclatură și convenții:**
    - Controllers: `@RequestMapping("/admin/...")` sau `@RequestMapping("/auth/...")`
    - Template paths: corespund cu ruta (ex: `/admin/medications` → `templates/admin/medications/list.html`)
    - Mesaje flash: cheia `successMessage` sau `errorMessage`
    - DTOs: suffix `Form` pentru formulare, `Stats` pentru statistici

**Plan de execuție:**
1. Citește cele trei fișiere sursă (`admin_panel_structure.md`, `entity_structure_proposal.md`, `AGENTS.md`).
2. Scrie un `implementation_plan.md` cu fișierele pe care urmează să le creezi, grupate pe faze, și așteaptă aprobarea mea.
3. Implementează **Faza 1 (MVP)** — config, security, layout, auth pages, dashboard, medications CRUD, seed data.
4. Verifică compilarea și pornirea aplicației.
5. După validare, continuă cu fazele următoare.

**IMPORTANT:**
- NU modifica fișierele din `ro.medfinder.medapp.entity` — entitățile sunt deja implementate și validate.
- Folosește **Lombok** (`@RequiredArgsConstructor` pe services/controllers pentru dependency injection prin constructor).
- Toate resursele CSS/JS externe (Bootstrap, Chart.js, Bootstrap Icons) se includ via CDN — fără npm/webpack.
- Paginile de auth trebuie să funcționeze corect ÎNAINTE de a trece la alte pagini.

Te rog să îmi confirmi când ai înțeles cerințele și ești gata să începi.
