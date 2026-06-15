Mai gândesc așa:

1. Unul din noi să facă research de ce baze de date folosesc ăștia + să facă un mic docker compose simulat vreo 3 baze de date

2. Altcineva face backendul central care se conecteaza la toate bazele alea și ține tot ce primește de la ele on init pe un cache care ține o zi (am căutat, cam asta e rata de aprovizionare la farmacii) Atunci când cineva adaugă în coș un produs, se citește direct de pe baza de date principala, ca să nu se întâmple sa se adauge ceva care defapt nu mai e din greșeală, doar pt că e cache-u valid.
   In rest servește un GeoJSON cu datele de localizare pe hartă a fiecărui produs, și Buy endpoint pentru:

3. Frontend care e the usual eCokmerce website care are și harta aia frumușică și așa

---

Cel cu researchu caute ce combinație de baza de date + sistem de caching extern bazei cam folosesc farmaciile astea de la noi din țară (ajută poate daca se caută cam ce firme de IT s-au ocupat cu chestiile astea )

Daca nu se ajunge la un singur răspuns clar (probabil o sa fie cazu) doar îl luăm pe cel mai probabil / popular și mergem doar cu ăla. Ideea e doar să putem arăta cum sistemul nostru centralizează chestii din mai multe baze de date chiar dacă sunt diferite / a schemauri diferite

############

Pentru backender main:

Încă un selling point de-al nostru care mi-a venit acum este tocmai că, VIRGULA cache-ul nostru e mai eficient și cost-effective decât al farmaciilor, și putem sa le cerem practic un abonament mai mic decât prețul pe cloud / curent whatever ce plăteau ei inainte, și păstrăm THE MARGINS (asta zis in prezentare)

Pentru frontend și backend ar fi frumix sa avem aceleași typeuri de typescript, cu bun ca runtime local (sau cloudflare workers in prod)

Dacă vouă chiar va place mai mult HP facem cu protobufuri să eliminăm greșelile de LLMuri. AA sau PHP asigura typeurile pe frontend idkk

Dacă aveți voi detalii mai bune de systems design pls drop it in chat
