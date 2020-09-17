# PiServerToJavaClient

## Implementeringsstrategi
Tommy og jeg havde ikke meget erfaring med brugen af en Raspberry Pi i starten af projektet. Derfor ville vi bruge de guides, der var angivet i powerpointen. Her ville vi starte med at flashe et OS til et SD-kort så vores Pi kunne starte og derefter ville vi skabe en SSH forbindelse så vi kunne tilgå vores Pi fra vores egen pc. Fra det punkt af, ville vi teste om vi kunne sende data og derefter lære Python nok til at kunne udføre opgaven.

Hvis vi så fik lavet det grundlæggende i opgaven ville vi bruge resten af tiden på at lære hvordan man lavede grafer samt andre forbedringer vi kunne finde på. 


## Valg af protokol(ler)
IP: Vi bruger IP i vores Client.java klasse til at forbinde denne til vores Pi. Her angiver vi også hvilken port Client.java skal lytte på. På vores Pi kan vi så lave en server, der sender data ud på samme port (se temperatureServer.py).

TCP: De klasser vi bruger i både Java og Python sender data via TCP, hvilket er passende da vi ikke behøver den hurtigere dataoverførsel som UDP tilfører sammenlignet med TCP.

SSH: Gennem SSH kan vi få adgang til vores Pi fra vores laptops. Med programmet Putty åbnes en terminal på Pi’en og man kan derfra sende kommandoer som Pi’en så vil udføre.


## Konklusion
Vi fik i gruppen løst opgaven uden de store problemer. Vi har lavet en server der kan måle temperatur og luftfugtighed fra den sensor vi lånte og sende disse data til vores klient i java. I java kan vi så læse de data ud i en graf ved brug af et eksternt library kaldet Jfreechart. Programmet er udvidet så man kan læse data ved et bestemt tidsinterval, så man kan have den til at læse hver time, minut eller andre tider. 

Vi ved ikke om sensoren virker når vi starter programmet, da man skal vente til et helt klokkeslæt. Så til fremtiden vil vi gerne have en sensor reading når programmet starter, så man omgående kan se at sensoren virker.
