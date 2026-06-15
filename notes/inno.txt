IDE:
	-Antigravity
Backend:
	-Java with Spring(/Spring Boot)
	-JDBC/Hibernate
	-REST API
Frontend:
	-JavaScript/TypeScript/React/Angular/ ember js(TEMPALTING LANGUAGE) SAU ALTE PROPUNERI

Links pt. modele de pagini in frontend:
https://www.drmax.ro/enterogermina-forte-suspensie-orala-10-flacoane-sanofi
https://www.compari.ro/monitoare-c3126/asus/rog-strix-oled-xg27aqwmg-p1272818941/
https://openfreemap.org/

Frontend:
-Storefront: 
	1. Listing FARMACII
	2. Harta FARMACII
	3. Listing MEDICAMENTE
	4. SEARCH/FILTER 
	5. CATEGORII FARMACII/MEDICAMENTE
	
*Model de business: 
	***(Home v1)Switch between Medicamente / Farmacii
		if Farmacii -> Listing/Searching/Filtering for Farmacii -> PDP Pharm -> Listing Meds of that Pharm -> PDP Selected Med
		else Home v2
	(Home v2)Listing/Searching/Filtering for Meds -> PDP Selected Med -> Med STOCK
									  -> Click and Collect -> *Payment System(price per time reserved)
									  -> Harta cu FARMACII
									  -> LISTING FARMACII
									  -> FILTRE FARMACII(default: pret, locatie)

	User Account
	Login/Create Account
	

-Admin Panel:
	Create Account pt. Farmacisti
	Login:  -> SUPER USER:	CRUD:
				-> UTILIZATORI ADMIN(FARMACISTI)
				-> FARMACII
				-> MEDICAMENTE
				-> CLIENTI(FRONTEND)
				-> 'ORDERS'(Requests for MEDS)
				+ *GRAFICE/STATISTICI
				+ SYNC MANUAL BAZA DE DATE FARMACIE
				
		-> BUSINESS USER:
				-> FARMACIA/FARMACIILE
				-> MEDICAMENTELE
				-> CLIENTI + 'ORDERS'(Requests for MEDS -> Accept/Reject/Undo)
				+ *GRAFICE/STATISTICI
				+ SYNC MANUAL BAZA DE DATE FARMACIE

Backend:
	BAZA DE DATE RELATIONALA
	REST API
	CLASE/ENTITATI DB: 	- UTILIZATORI(4 tipuri: client, super, owner, farmacist -> locatie)
				- FARMACII
				- LOCATII
				- LM_STOCK(many_to_many)
				- MEDICAMENTE
				- 'ORDERS'(Requests for MEDS)
	SYNC(IMPORT) CU BAZA DE DATE/ERP-UL/(CSV) A UNEI FARMACII
		-> GET
	CLICK AND COLLECT(Reserve)
		-> efecte in admin 
		-> Accept/Reject/Undo
	AUTHENTICATION
	CREATE USER ACCOUNT
	NOTIFICATION in *Admin/*Email/*Phone

	


