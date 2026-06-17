# Dev

dati un run la tot:

```bash
docker compose up --build
```

porneste si mailpit pentru testare de mailuri, si aplicatia noastra.

si vezi user ui la `http://localhost:8080`
Mailpit server la `http://localhost:8025`

La startup se populeaza baza de date H2 in-memory cu tot ce trebuie din
`medapp/src/main/java/ro/medfinder/medapp/config/DataLoader.java`

La `/admin/` te poti loga cu
mail: `admin@medfinder.ro`
pass: `admin123`

Sau individual doar java-u cu:

```bash
cd medapp && ./gradlew bootRun
```

---

# Demo-ing

Daca vrei sa vezi in actiune, din interfata de user normal, poti sa faci rezervare de o chestie, si dupa din
`/admin/dashboard`, in alt browser vine adminnu sa-ti dea accept la click & collect.
