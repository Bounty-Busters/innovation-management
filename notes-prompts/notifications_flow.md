# Arhitectură și Flow: Sistemul de Notificări

Acest document descrie arhitectura propusă pentru implementarea sistemului de notificări (Admin In-App și Email) în aplicația MedFinder, folosind Spring Events.

## 1. Arhitectura Propusă (Event-Driven)

Pentru a menține decuplarea și a nu încetini salvarea comenzilor, sistemul va folosi **Spring Application Events**.

- Când o comandă își schimbă statusul în `ClientOrderService` sau `OrderService`, serviciul va "publica" un eveniment (ex: `OrderStatusChangedEvent`).
- Un `NotificationEventListener` va asculta aceste evenimente (asincron, `@TransactionalEventListener`) și va decide ce notificări să creeze și să trimită.

### A. Notificări Email (Client)

- **Când se trimit:**
  - Când comanda este **ACCEPTED** (Confirmare rezervare).
  - Când comanda este **READY_FOR_PICKUP** (Vino să o ridici).
  - Când comanda este **REJECTED / CANCELLED** (Informare anulare).
- **Tehnologie:** `spring-boot-starter-mail` + Șabloane HTML folosind **Thymeleaf** (`/resources/templates/mail/`).

### B. Notificări In-App / Admin Panel (Farmacist)

- **Când se trimit:**
  - Când se creează o comandă nouă (**PENDING**). Farmacistul trebuie să afle imediat pentru a răspunde în 30 de minute.
  - Când clientul anulează o comandă (**CANCELLED**).
- **Tehnologie:** **WebSockets (STOMP)** pentru real-time.
- **UX Propus:**
  - Pe Frontend, va exista un script care se conectează la topicul locației farmaciei. La primirea unui mesaj nou, va apărea un "Toast" (pop-up) în colțul ecranului.
  - **Clopoțel de Notificări:** În meniul de sus al adminului va exista o iconiță tip clopoțel care va prelua din baza de date notificările necitite (`NotificationStatus.UNREAD`), asigurând faptul că dacă farmacistul a fost offline, nu va pierde alertele (Fallback UI).
  - **Concurență:** Dacă 2 farmaciști din aceeași locație dau click pe aceeași notificare, al doilea va primi o eroare prietenoasă ("Comanda a fost deja procesată de un coleg").

---

## 📝 To Be Done (Îmbunătățiri pentru viitor)

Următoarele aspecte sunt notate pentru dezvoltări ulterioare, nefiind prioritare pentru MVP-ul curent:

1. **Retry Mechanism la Mailuri:** În prezent, dacă serverul SMTP pică, notificarea este doar marcată ca `FAILED` în baza de date și se trece mai departe. Pe viitor se dorește un mecanism de retry (ex: un job care rulează din oră în oră și reîncearcă trimiterea).
2. **Limitatoare de Rată (Rate Limiting) pentru SMTP:** La auto-respingerea în masă a comenzilor vechi (peste 30 min), pot apărea burst-uri de mail-uri trimise simultan. Va fi necesară implementarea unei cozi (Queue) sau utilizarea unui serviciu specializat gen SendGrid/Mailgun pentru a evita penalizările de spam.

---

## 🛠️ Etape de Implementare (Checklist)

1. **Baza de Date & Entități:**
   - [x] Entitatea `Notification` există deja.
   - [ ] Creare `NotificationRepository`.

2. **Arhitectura de Evenimente:**
   - [ ] Creare `OrderCreatedEvent` și `OrderStatusChangedEvent`.
   - [ ] Injectare `ApplicationEventPublisher` în serviciile de comenzi și publicare evenimente la schimbarea statusurilor.
   - [ ] Creare `NotificationEventListener` care salvează în baza de date.

3. **Notificări Email:**
   - [ ] Adăugare dependență `spring-boot-starter-mail`.
   - [ ] Creare `EmailService` și configurare variabile SMTP de test în `application.properties`.
   - [ ] Design șabloane de mail în Thymeleaf.

4. **WebSockets (In-App):**
   - [ ] Creare `WebSocketConfig` pentru STOMP.
   - [ ] Implementare push-message din `NotificationEventListener` către frontend pe `/topic/pharmacy/{locationId}`.
   - [ ] Adăugare script JS în `layout/admin.html` pentru afișarea Toast-urilor.

---

## 🤖 Prompt Pentru Agentul de Codare

_Copiază acest bloc de text și dă-l agentului de codare pentru a începe implementarea:_

```text
Te rog să implementezi sistemul de Notificări pentru aplicația Spring Boot folosind arhitectura din fișierul `notifications_flow.md`. Entitatea `Notification` există deja.

Implementăm doar Notificări pe Email (pentru clienți) și Notificări In-App / WebSockets (pentru admini/farmaciști).

Iată ce trebuie să faci:

1. ARHITECTURA DE EVENIMENTE:
- Creează evenimentele custom `OrderCreatedEvent` și `OrderStatusChangedEvent`.
- Modifică `OrderService` și `ClientOrderService` să publice aceste evenimente asincron prin `ApplicationEventPublisher`. (Nu bloca baza de date cu trimiterea de email-uri).
- Creează `NotificationEventListener` (asincron/tranzacțional) care ascultă evenimentele și salvează rândurile în `NotificationRepository`.

2. NOTIFICĂRI EMAIL (Pentru Client):
- Adaugă `spring-boot-starter-mail` în `build.gradle`.
- Creează `EmailService` și șabloane HTML curate în Thymeleaf.
- Trimite email când o comandă este ACCEPTED, READY_FOR_PICKUP, REJECTED, sau CANCELLED.
- Tratare erori (MVP): Dacă SMTP-ul pică sau aruncă eroare, prinde eroarea (`catch`) și doar marchează rândul din tabela `Notification` ca `FAILED`. Nu vrem mecanism de retry la acest stadiu.

3. NOTIFICĂRI IN-APP / UI (Pentru Farmacist):
- Configurează WebSockets (`STOMP`) și creează flow-ul astfel încât la declanșarea `OrderCreatedEvent` să fie trimis un mesaj pe topicul locației respective.
- Modifică `layout/admin.html` (sau JS-ul asociat) să afișeze un "Toast" (pop-up Bootstrap) alertând farmacistul că are o comandă nouă.
- ADAUGĂ UN CLOPOȚEL: Lângă profilul din meniul de admin, adaugă un clopoțel care, la încărcarea paginii, face un request GET să preia notificările cu status `UNREAD` din baza de date pentru farmacistul curent (așa rezolvăm cazul în care el a fost offline când s-a trimis mesajul prin WebSocket).
- EROARE CONCURENȚĂ: Dacă 2 farmaciști din aceeași locație dau click pe aceeași notificare pentru a accepta comanda, asigură-te că primul reușește, iar al doilea primește o eroare prietenoasă pe UI (ex: "Această comandă a fost deja procesată de un coleg").
```
