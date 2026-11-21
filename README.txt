BananCustomItems - GUI z customowymi itemami (Purpur / Paper 1.21.11, Java 21)

Budowanie:  mvn clean package   ->  target/BananCustomItems-1.0.0.jar
Instalacja: wrzuc jar do plugins/, zrestartuj serwer.

Komendy:
  /citems                       otwiera menu
  /citems reload                przeladowuje items.yml
  /citems give <gracz> <id> [n] daje item

Uprawnienia:
  banansmp.customitems.use    (domyslnie wszyscy)
  banansmp.customitems.take   (domyslnie op)
  banansmp.customitems.admin  (domyslnie op)

Itemy edytujesz w plugins/BananCustomItems/items.yml

BEZ INSTALOWANIA JAVY - budowanie jara na GitHubie:
1. Zaloz darmowe konto na github.com i nowe repozytorium (New repository).
2. Kliknij "uploading an existing file" i przeciagnij TUTAJ ZAWARTOSC rozpakowanego folderu
   (pom.xml, src, .github itd.). Kliknij "Commit changes".
3. Wejdz w zakladke "Actions" - po ok. minucie build sie skonczy (zielony ptaszek).
4. Kliknij build -> na dole "Artifacts" -> pobierz BananCustomItems-jar.
5. Rozpakuj - w srodku jest BananCustomItems-1.0.0.jar. Wrzuc do plugins/ i zrestartuj serwer.
