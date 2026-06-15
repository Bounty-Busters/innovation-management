Make changes on your own branch.
Try to put as many of your agent-specific artifacts right into this repo, tracked, not somewhere in "~". Things like `implementation_plan` and whatever else you create

---

## Project Architecture & Tech Decisions

- **Backend Framework:** Spring Boot
- **Template Engine:** Thymeleaf
- **Frontend View:** Landed on **Home v2** (Medications/Meds-first list) as the main landing page of the Storefront:
  1. User searches/filters medications on the home page.
  2. Clicking a medication goes to its PDP (Product Detail Page).
  3. The PDP displays stock availability per pharmacy, pricing, and locations on a map.
  4. User can reservation-collect (Click & Collect) with a timed-holding fee.

## Medication & EAN/CIM Database Integration

### Public Databases Research

1. **ANMDMR (Romania):** Publishes the official "Nomenclatorul medicamentelor de uz uman" (Human Medicines Nomenclature) including CIM codes, commercial name, DCI (active substance), form, ATC code, and manufacturer. This is public but only as an Excel/CSV download, not a hosted SQL database.
2. **openFDA / EMA (Global/EU):** Offer downloadable APIs/datasets (JSON/CSV) for drug details but no direct-access SQL server.

### Decided Implementation Strategy

Since no public, live hosted SQL database with EAN codes is directly connectable:

- **Importer Connector:** We will implement an import module to load pharmacy inventory/medications from structured files (CSV/Excel format mimicking ANMDMR/CIM schema).
- **Mock Synthesizer:** If no actual data files are provided, the system will use a local database initializer to synthesize realistic mock medication data (with valid ATC structures, CIM/EAN codes, and categories) to populate the schema.
