# Project Overview & Requirements

## Technical Stack

- **IDE:** Antigravity
- **Backend:**
  - Java with Spring / Spring Boot
  - JDBC / Hibernate
  - REST API
  - Relational Database (SQL)
- **Frontend:**
  - JavaScript / TypeScript
  - React / Angular / Ember.js (Templating Language) or other options

## Reference Links

- [Dr. Max - Enterogermina Forte](https://www.drmax.ro/enterogermina-forte-suspensie-orala-10-flacoane-sanofi)
- [Compari.ro - ASUS ROG Strix OLED](https://www.compari.ro/monitoare-c3126/asus/rog-strix-oled-xg27aqwmg-p1272818941/)

  <img width="1189" height="420" alt="Screenshot 2026-06-15 at 20 10 43" src="https://github.com/user-attachments/assets/ba9e8eb0-1654-4abd-a91f-516b0b6bb7c5" />

- [OpenFreeMap](https://openfreemap.org/)
  [https://wiki.openstreetmap.org/wiki/GeoJSON]

---

## Frontend Requirements

### Storefront

1. **Listing:** Pharmacies (Farmacii)
2. **Map:** Pharmacies (Harta Farmacii)
3. **Listing:** Medications (Medicamente)
4. **Search & Filter**
5. **Categories:** Pharmacies and Medications

#### User Flows & Business Model

- **Home v1 (Switch between Medications / Pharmacies):**
  - **If Pharmacies:**
    - Search/Filter/List Pharmacies $\rightarrow$ Pharmacy PDP $\rightarrow$ List Meds of that Pharmacy $\rightarrow$ Selected Medication PDP
  - **If Medications (Home v2):**
    - Search/Filter/List Medications $\rightarrow$ Selected Medication PDP $\rightarrow$ Medication Stock $\rightarrow$ Click & Collect $\rightarrow$ Payment System (Price per time reserved) $\rightarrow$ Map with Pharmacies $\rightarrow$ Pharmacy Listing $\rightarrow$ Pharmacy Filters (Default: Price, Location)
- **User Account:**
  - Login / Create Account

### Admin Panel

- **Account Creation:** Create Account for Pharmacists
- **Login & Roles:**
  - **Super User (CRUD):**
    - Admin Users (Pharmacists)
    - Pharmacies
    - Medications
    - Clients (Frontend)
    - Orders (Requests for Meds) + Graphs/Statistics + Manual DB Sync for Pharmacy - note: nah but the sync shouldn't be only manual from DB, according to `notes/perete-wapp.md`
  - **Business User:**
    - Pharmacy / Pharmacies management
    - Medications management
    - Clients + Orders (Accept / Reject / Undo) + Graphs/Statistics + Manual DB Sync for Pharmacy - note: nah but the sync shouldn't be only manual from DB, according to `notes/perete-wapp.md`
  - **Regular User**:
    - Can place orders

---

## Backend Requirements

- **Relational Database**
- **REST API**
- **Database Entities / Classes:**
  - `Users` (4 Roles: Client, Super User, Owner, Pharmacist $\rightarrow$ Linked to Location)
  - `Pharmacies`
  - `Locations`
  - `LM_Stock` (Many-to-Many relationship between Pharmacy and Medication)
  - `Medications`
  - `Orders` (Requests for Medications)
- **Sync & Import:** Sync/Import CSV / DB / ERP data of a pharmacy (via GET)
- **Click & Collect (Reservation):**
  - Triggers actions and states in Admin panel
  - Actions: Accept / Reject / Undo
- **Authentication & Authorization**
- **Account Creation**
- **Notifications:** Admin / Email / Phone
