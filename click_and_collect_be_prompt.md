Salut! Te rog să acționezi ca un Senior Java/Spring Boot Backend Developer. Sarcina ta este să implementezi pe partea de Backend logica completă pentru fluxul de "Click & Collect" din aplicația MedFinder. 

Proiectul este deja configurat și entitățile de bază există. Ai la dispoziție fișierul `click_and_collect_flow.md` în root-ul proiectului care descrie exact regulile de business și rutele API pe care trebuie să le expui.

### Pașii de implementare necesari:

**1. Modificări Entități & DTOs:**
- Modifică clasa `Client` (care moștenește `User`) și adaugă câmpul `private Integer freeLongReservationsLeft = 3;`.
- Modifică clasa `Order` pentru a adăuga câmpurile: `private Integer reservationHours;` și `private LocalDateTime acceptedAt;`.
- Creează un DTO `OrderCreateRequest` pentru payload-ul de la frontend.

**2. Implementarea Prețului (Pricing Service):**
- Creează logica care să respecte formula: ≤2h e gratuit. De la 3h la 24h crește liniar de la 1 RON la 7 RON. Rezultatul trebuie să fie de tip `BigDecimal` și rotunjit la cel mai apropiat multiplu de 0.25 (banii după virgulă să fie .00, .25, .50 sau .75). Hint: `Math.round(price * 4) / 4.0`.
- Dacă utilizatorul cere între 3 și 5 ore, și are `freeLongReservationsLeft > 0`, consumă-i o gratuitate (scade 1) și setează taxa la 0. Altfel, plătește prețul calculat.

**3. Implementarea ClientController & OrderService (Logica de Creare):**
- Implementează `POST /api/client/orders`.
- **Validare zilnică:** Înainte de a salva, verifică dacă clientul mai are astăzi o comandă activă (`PENDING`, `ACCEPTED`, `READY`, `PICKED_UP`) pentru același medicament la aceeași locație. Dacă da, aruncă o excepție (ex: 400 Bad Request).
- Creează comanda cu status `PENDING`. Aici **NU scazi stocul**.

**4. Modificarea logicii de Acceptare/Refuzare (în Admin OrderService existent):**
- Când farmacistul face update la `ACCEPTED`, setează `order.setAcceptedAt(LocalDateTime.now())` și **abia acum SCADE stocul** din `MedStock`. Dacă nu e stoc, aruncă eroare.
- Dacă farmacistul dă `REJECTED` din `PENDING`, nu atinge stocul. Dacă s-a consumat o gratuitate la creare, adaug-o înapoi clientului.

**5. Anularea de către Client:**
- Implementează `PUT /api/client/orders/{id}/cancel`.
- Dacă comanda a fost `ACCEPTED`, dă stocul înapoi în `MedStock`. Dă și gratuitatea înapoi dacă a fost folosită. Setează status `CANCELLED`.

**6. Scheduled Task (Cronjob pentru Expirare):**
- Creează clasa `OrderExpirationScheduler` cu o metodă `@Scheduled(cron = "0 0/5 * * * *")`.
- Caută comenzile `ACCEPTED` sau `READY_FOR_PICKUP` unde `acceptedAt + reservationHours < NOW`.
- Setează-le pe `EXPIRED` și dă stocul înapoi în `MedStock`.

**Plan de execuție obligatoriu pentru tine:**
1. Citește documentul `click_and_collect_flow.md` pentru clarificări și citește clasele `Order`, `Client`, `OrderService` existente.
2. Formulează un plan concret de acțiune și arată-mi lista de fișiere pe care le vei modifica/crea.
3. Așteaptă confirmarea mea, iar apoi scrie codul. Ensure transactionality (`@Transactional`) for all methods modifying stock and orders.

Ești pregătit să generezi planul?
