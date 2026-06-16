# 🔄 Flow și Rute API: Click & Collect

Acest document descrie logica de business, fluxurile aplicației și rutele REST pentru funcționalitatea de Click & Collect din aplicația MedFinder.

## 📌 Reguli de Business (Business Rules)

1. **Calcul Preț (Holding Fee):**
   - ≤ 2 ore: **Gratuit (0 RON)**.
   - Între 3h și 24h: Prețul crește liniar de la 1 RON (pentru 3h) la 7 RON (pentru 24h).
   - *Formula:* Prețul exact calculat ca `1 + (ore - 3) * (6 / 21)`, rotunjit la cel mai apropiat multiplu de 0.25 (adică restul după virgulă să fie .00, .25, .50 sau .75). Exemplu în Java: `Math.round(rawPrice * 4) / 4.0`.
   - *Perk Client:* Fiecare client primește **3 rezervări gratuite de până la 5 ore** la crearea contului (`freeLongReservationsLeft = 3`).

2. **Gestiunea Stocului (`MedStock`):**
   - Stocul scade (este blocat fizic) **doar atunci când farmacistul acceptă comanda** (`status == ACCEPTED`).
   - *Restaurare stoc:* Dacă o comandă anterior acceptată este anulată de client (`CANCELLED`) sau expiră (`EXPIRED`), stocul trebuie returnat locației.

3. **Limită zilnică (Anti-Spam / Anti-Hoarding):**
   - Un utilizator poate rezerva același medicament la aceeași locație **o singură dată pe zi**.
   - Această limită se aplică doar dacă există deja o rezervare `PENDING`, `ACCEPTED`, `READY_FOR_PICKUP` sau `PICKED_UP`.
   - Comenzile `REJECTED`, `CANCELLED` sau `EXPIRED` nu blochează o nouă încercare.

4. **Timp de rezervare și Expirare:**
   - Timpul rezervării începe să curgă din momentul în care comanda este `ACCEPTED` (nu din momentul plasării cererii).
   - Un **Cronjob** care rulează din 5 în 5 minute verifică comenzile `ACCEPTED` sau `READY_FOR_PICKUP`. Dacă `acceptedAt + reservationHours < NOW`, comanda trece automat în `EXPIRED`, iar stocul este refăcut.

---

## 🚦 Mașina de Stări (State Machine) pentru Comenzi

* **`PENDING`** - Clientul a plasat cererea (stocul încă NU a scăzut).
* **`ACCEPTED`** - Farmacistul a acceptat cererea (stocul SCADE, timer-ul de rezervare PORNEȘTE).
* **`REJECTED`** - Farmacistul refuză cererea (lipsă fizică de stoc etc. Perk-ul gratuit se dă înapoi dacă a fost folosit).
* **`READY_FOR_PICKUP`** - Produsul a fost pus deoparte.
* **`PICKED_UP`** - Clientul a luat produsul fizic din farmacie.
* **`CANCELLED`** - Clientul s-a răzgândit (stocul este restaurat dacă a fost scăzut, perk-ul se dă înapoi).
* **`EXPIRED`** - Cronjob-ul o închide pentru că timpul de rezervare s-a scurs (stocul este restaurat).

---

## 🛣️ Rute API (Endpoints) pentru Client (Frontend)

Toate rutele de client necesită utilizator autentificat cu rolul `CLIENT`.

### 1. Creare Comandă (Rezervare)
**`POST /api/client/orders`**
- **Payload (JSON):**
  ```json
  {
    "medicationId": 123,
    "locationId": 45,
    "quantity": 1,
    "reservationHours": 4
  }
  ```
- **Proces:**
  1. Validează dacă clientul mai are voie să facă această comandă (limita de 1/zi pe EAN+Locație activă).
  2. Calculează `holdingFee` (aplică gratuitatea de 5h scăzând `freeLongReservationsLeft` dacă este cazul, altfel calculează după formulă).
  3. Calculează `totalPrice` = `(medStock.price * quantity) + holdingFee`.
  4. Salvează `Order` cu status `PENDING`. **Nu** modifică `MedStock`.

### 2. Anulare Comandă de către Client
**`PUT /api/client/orders/{orderId}/cancel`**
- **Proces:**
  1. Dacă statusul era `ACCEPTED` sau `READY_FOR_PICKUP`, returnează produsele în `MedStock`.
  2. Dacă s-a folosit o rezervare lungă gratuită (`holdingFee` a fost 0 deși `hours` > 2), incrementează la loc `freeLongReservationsLeft` al clientului.
  3. Status devine `CANCELLED`.

### 3. Listarea comenzilor proprii
**`GET /api/client/orders`**
- Returnează comenzile clientului logat, ordonate descrescător după dată.

---

## ⚙️ Cronjob (Background Task)

- **Clasa:** `OrderExpirationScheduler`
- **Timp:** `@Scheduled(cron = "0 0/5 * * * *")` (la fiecare 5 minute).
- **Proces:**
  Caută comenzile unde:
  `status` IN (`ACCEPTED`, `READY_FOR_PICKUP`)
  ȘI `acceptedAt` + `reservationHours` < NOW.
  Pentru fiecare:
  1. Status = `EXPIRED`.
  2. Returnează cantitatea înapoi în `MedStock`.
