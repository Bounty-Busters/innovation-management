# 🔄 Flow și Rute API: Click & Collect (Actualizat)

Acest document descrie logica de business, fluxurile aplicației și rutele REST pentru funcționalitatea de Click & Collect din aplicația MedFinder, incluzând regulile pentru stocuri, limite per utilizator și expirare.

---

## 📌 Reguli de Business (Business Rules)

### 1. Limite și Restricții pentru Clienți
- **Limită zilnică pe produs:** Un utilizator poate rezerva același medicament la aceeași locație **o singură dată pe zi** (dacă are deja o comandă `PENDING`, `ACCEPTED`, `READY_FOR_PICKUP`, `PICKED_UP` sau `EXPIRED`). Asta înseamnă că dacă nu ridică o comandă și expiră, nu mai poate rezerva același produs acolo în aceeași zi, pentru a preveni abuzurile.
- **Limită maximă comenzi PENDING:** Un utilizator poate avea **maximum 10 comenzi în stadiul `PENDING`** simultan pe tot contul său. La încercarea de a plasa a 11-a comandă, sistemul va arunca eroare și va cere anularea uneia existente.

### 2. Calcul Preț (Holding Fee) & Avantaje (Perks)
- **Primele 2 ore:** Gratuit (0 RON).
- **Între 3h și 24h:** Prețul crește liniar de la 1 RON (pentru 3h) la 7 RON (pentru 24h). Formula: `1 + (ore - 3) * (6 / 21)`, rotunjit la cel mai apropiat multiplu de 0.25 (ex: 1.25, 1.50).
- **Perk-ul Gratuității (Max 3 ore):** Fiecare client primește **3 rezervări gratuite de până la 3 ore** la crearea contului (`freeLongReservationsLeft = 3`). Dacă rezervarea este de max 3 ore și mai are perk-uri, taxa devine 0 RON și se scade un perk din cont. Dacă alege peste 3 ore, se va calcula taxa direct.

### 3. Gestiunea Stocului (`MedStock`)
- **Faza PENDING:** Stocul **NU scade**. Aplicația doar verifică vizual dacă există stoc disponibil. *(Atenție: Se poate suprascrie/rezerva același produs de mai mulți clienți până când farmacistul aprobă)*.
- **Faza ACCEPTED:** Stocul **SCADE fizic** în sistem.
- **Restaurare stoc (Undo):** Dacă o comandă anterior acceptată este anulată (indiferent dacă o anulează clientul sau farmacistul din panou) sau dacă expiră (`EXPIRED`), stocul este returnat.

### 4. Timpi de Răspuns și Expirare (Cronjobs)
- **Timp PENDING (Auto-Reject):** O comandă lăsată pe `PENDING` de farmacist mai mult de **30 de minute** este respinsă automat (Trece în `REJECTED`). Motivul salvat: "Pharmacy did not respond within 30 minutes". Perk-ul (dacă a fost folosit) este returnat clientului.
- **Timp ACCEPTED (Expirare Ridicare):** Timpul de rezervare (ex: 2 ore) începe să curgă din momentul în care comanda devine `ACCEPTED`. Un Cronjob verifică comenzile `ACCEPTED/READY_FOR_PICKUP`. Dacă `acceptedAt + reservationHours < NOW`, comanda trece automat în `EXPIRED` (stocul se întoarce).

---

## 🚦 Mașina de Stări (State Machine) pentru Comenzi

Flow-ul de viață al unei rezervări (Order):

1. **`PENDING`** - Clientul a plasat cererea.
   - *Stoc:* Neschimbat. 
   - *Tranziții posibile:*
     - 👉 `ACCEPTED` (de către farmacist, dacă are stoc fizic).
     - 👉 `REJECTED` (de către farmacist SAU automat de cronjob la 30 min. Perk returnat).
     - 👉 `CANCELLED` (de către client. Perk returnat).

2. **`ACCEPTED`** - Farmacistul a aprobat cererea.
   - *Stoc:* Scade. 
   - *Timp:* Pornește cronometrul rezervării (ex: are 3 ore să o ridice).
   - *Tranziții posibile:*
     - 👉 `READY_FOR_PICKUP` (marcată de farmacist).
     - 👉 `EXPIRED` (cronjob dacă timpul a trecut. Stoc returnat).
     - 👉 `CANCELLED` (anulată de client SAU de farmacist ca 'undo'. Stoc returnat. Perk returnat).

3. **`READY_FOR_PICKUP`** - Punga este pregătită (stadiu opțional).
   - *Stoc:* Rămâne scăzut.
   - *Tranziții posibile:*
     - 👉 `PICKED_UP` (Clientul a ridicat-o).
     - 👉 `EXPIRED` (Timpul a trecut. Stoc returnat).
     - 👉 `CANCELLED` (Stoc returnat. Perk returnat).

4. **Stări Finale (Terminale):**
   - **`PICKED_UP`**: Clientul a plătit și luat medicamentul fizic din farmacie.
   - **`REJECTED`**: Cererea inițială a fost refuzată.
   - **`CANCELLED`**: Comanda a fost anulată (undo).
   - **`EXPIRED`**: Comanda nu a fost ridicată în timpul alocat de rezervare.

---

## 🛣️ Rute API (Endpoints) Backend

### 1. Creare Comandă (Rezervare)
**`POST /api/client/orders`**
- Validează limita de 1/zi pe același medicament + limita globală de 3 comenzi `PENDING`.
- Calculează taxa / aplică perk-ul pentru 3h.
- Salvează ca `PENDING`.

### 2. Anulare Comandă de către Client
**`PUT /api/client/orders/{orderId}/cancel`**
- Dacă statusul era `ACCEPTED` / `READY_FOR_PICKUP`, returnează produsele în stoc.
- Returnează perk-ul. Status devine `CANCELLED`.

### 3. Modificare Status de către Farmacist (Admin)
**`POST /admin/orders/{id}/status`**
- Farmacistul dă Accept, Reject, sau Cancel.
- *La Cancel pe o comandă ACCEPTED*: Backend-ul este obligat să apeleze returnarea de stoc și de perk-uri (undo).

### 4. Background Tasks (Scheduler)
- **`OrderExpirationScheduler` (rulare la fiecare 5 minute):**
  - Găsește comenzile `ACCEPTED` sau `READY_FOR_PICKUP` unde timpul de ridicare a fost depășit și le trece în `EXPIRED` (cu returnarea stocului).
  - Găsește comenzile `PENDING` mai vechi de 30 minute și le trece automat în `REJECTED` (cu returnarea perk-ului).
