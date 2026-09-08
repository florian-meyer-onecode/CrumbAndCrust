# Crumb & Crust

Brotrezepte und Backabläufe mit Compose Multiplatform für Web und Android.

## Stand

Schritt 7: Rezepte im Browser anlegen, bearbeiten und vollständig anzeigen,
lokal in IndexedDB. Zutaten lassen sich für einen einfachen/doppelten Ansatz
oder ein gewünschtes Gesamtteiggewicht skalieren. Eine Start- oder Zielzeit
mit Datum berechnet Beginn und Ende jedes Schritts, einschließlich mehrtägiger
Abläufe und Zeitumstellungen. Das gespeicherte Grundrezept bleibt unverändert.
Alle sieben vereinbarten Umsetzungsschritte sind implementiert. Siehe [PLAN.md](PLAN.md).

## Voraussetzungen

- JDK 21 (`JAVA_HOME` auf das JDK-Verzeichnis setzen).
- Android SDK mit Platform 37 und Build Tools 36.0.0 für den Android-Build.
- Aktueller Browser mit WebAssembly GC, beispielsweise Chrome oder Edge.
- Beim ersten Build Internetzugriff für Gradle und Abhängigkeiten.

Gradle 9.5.0 wird über den mitgelieferten Wrapper bereitgestellt. Node.js und
Yarn für den Web-Build lädt Gradle automatisch. Eine globale Installation von
Gradle, Kotlin oder Node.js ist nicht erforderlich.

Für das SDK eine nicht versionierte `local.properties` im Projektordner anlegen:

```properties
sdk.dir=C:/Users/DEIN_NAME/AppData/Local/Android/Sdk
```

Alternativ `ANDROID_HOME` konfigurieren. Auf diesem Rechner ist der SDK-Pfad
bereits lokal eingetragen. Auch beim Web-Build wird das Android-Projekt von
Gradle konfiguriert; deshalb ist für diesen Projektaufbau das SDK vorzuhalten.

## Web starten

Im Projektordner in PowerShell:

```powershell
.\gradlew.bat :webApp:wasmJsBrowserDevelopmentRun
```

Anschließend http://localhost:8080 öffnen. Mit Strg+C den Entwicklungsserver
beenden. Er liefert nur die Webdateien aus; es gibt kein Anwendungsbackend.

Produktionsdateien bauen:

```powershell
.\gradlew.bat :webApp:wasmJsBrowserDistribution
```

Ergebnis: `webApp/build/dist/wasmJs/productionExecutable/`.
Diesen Ordner über einen statischen HTTP-Server bereitstellen, nicht die
HTML-Datei direkt per Doppelklick öffnen. Der Server muss `.wasm` als
`application/wasm` ausliefern.

## Android bauen

```powershell
.\gradlew.bat :androidApp:assembleDebug
```

APK: `androidApp/build/outputs/apk/debug/androidApp-debug.apk`.

Bei verbundenem Tablet mit aktiviertem USB-Debugging:

```powershell
.\gradlew.bat :androidApp:installDebug
```

Alternativ das Projekt in Android Studio öffnen und `androidApp` starten.
Die Mindestversion ist Android 7.0 (API 24).

Auf macOS/Linux dieselben Gradle-Aufgaben mit `./gradlew` aufrufen.

## Struktur

- `shared/`: gemeinsame Compose-Oberfläche; später Modelle und Fachlogik.
- `webApp/`: WebAssembly-Einstieg, HTML-Hülle und Web-Build.
- `androidApp/`: Android-Einstieg und Manifest.
- `gradle/libs.versions.toml`: festgeschriebene Abhängigkeitsversionen.
- `code.html`, `DESIGN.md`, `screen.png`: ursprüngliche Designreferenzen.

Kotlin 2.4.10, Compose Multiplatform 1.11.1 und Android Gradle Plugin 9.3.2.
Android verwendet das separate App-Modul und das Android-KMP-Library-Plugin
im gemeinsamen Modul, ohne veraltete Kombination von KMP und Android-App-Plugin.
Siehe [JetBrains-Projektstruktur](https://kotlinlang.org/docs/multiplatform/multiplatform-project-agp-9-migration.html).

Rezepte liegen in der IndexedDB-Datenbank `crumb-and-crust` (Version 1),
Object Store `recipes`, mit einer unabhängigen UUID pro Rezept. Schreiben wird
erst nach dem erfolgreichen Transaktionsabschluss bestätigt. Fehlgeschlagene Transaktionen verändern
keine vorhandenen Rezepte; ein ungespeicherter Entwurf bleibt bei Speicherfehlern
offen. Zutaten und Schritte haben ebenfalls stabile IDs, unabhängig von ihrer
Position. Die gespeicherten Objekte tragen eine eigene Schema-Version.

Browserdaten sind an Browserprofil und Origin gebunden. Verwende für die lokale
Entwicklung weiterhin `http://localhost:8080`: Ein anderer Hostname oder Port
hat einen eigenen Speicher. Löschen der Websitedaten entfernt die Rezepte.
Es gibt keine Synchronisierung oder Sicherung auf einem Server. Android zeigt
vorerst einen Hinweis und deaktiviert die Eingabe, statt flüchtige Daten als
gespeichert auszugeben. Die Android-Speicheranbindung bleibt ein späterer Schritt.

## Git und Arbeitsweise

Alle Arbeit erfolgt in sinnvollen Commits direkt auf `main`. Nach jedem der
sieben Schritte wird auf das OK gewartet. SDK-Pfade, Build-Ausgaben und
IDE-Einstellungen bleiben außerhalb der Versionsverwaltung. Der Gradle Wrapper
ist enthalten; die Distribution ist mit einer SHA-256-Prüfsumme abgesichert.

## Verifikation von Schritt 2 (08.09.2026)

- Web-Produktionsbuild und Android-Debug-Build erfolgreich.
- Produktionsausgabe in Edge gestartet: Compose-Canvas 1280 × 800,
  korrekter Seitentitel, keine JavaScript-Laufzeitfehler oder fehlgeschlagenen
  Netzwerkanfragen im Prüflauf.
- Android SDK und ADB zugänglich; noch kein Test auf einem physischen Tablet.
- Gradle-Wrapper-JAR mit offizieller SHA-256-Prüfsumme verifiziert.
- Verbleibende Werkzeughinweise: Kotlin meldet NPM-Auflösung während der
  Konfiguration; Webpack weist auf die Größe der Compose/Skia-WebAssembly-
  Laufzeit hin (zusammen ungefähr 9,2 MiB unkomprimierte WASM-Dateien).
  Beides verhindert den Build oder Browserstart nicht.

Auf diesem Rechner wurde ausschließlich dieses Repository als Git
`safe.directory` registriert, da die initiale Sandbox sein `.git`-Verzeichnis
unter einem anderen Windows-Benutzer angelegt hat.

## Gestaltung

Der Homescreen verwendet Newsreader und Plus Jakarta Sans aus den
Designreferenzen. Die Schriftdateien liegen als gemeinsame Compose-Ressourcen
im Projekt und werden von derselben Origin wie die App geladen, ohne externe
Schriftanfragen. Beide stehen unter der SIL Open Font License; die Lizenztexte
liegen in `licenses/`. Quelle: [Google Fonts](https://github.com/google/fonts).

Ab 840 dp stehen Rezeptliste und Backplan nebeneinander. Darunter werden die
Bereiche gestapelt; der Inhalt ist scrollbar, der Kopfbereich bleibt sichtbar.
Buttons unterstützen Maus, Touch und Tastatur mit sichtbarem Fokus.

## Verifikation von Schritt 3 (08.09.2026)

- Web-Entwicklungsbuild und Android-Debug-Build erfolgreich.
- Edge: Desktop 1280 × 900, Tablet 1024 × 768, schmale Ansichten
  390 × 844 und 320 × 740 visuell geprüft.
- Keine JavaScript-Laufzeitfehler oder fehlgeschlagenen Anfragen im Browserlauf.
- Hinweisanzeige und Schließen per Maus, Aktivierung per Tab/Enter und
  Tablet-Touch geprüft. Sichtbaren Tastaturfokus kontrolliert.
- Gestapeltes Layout, Scrollbarkeit bis zum Seitenende und fester Kopfbereich
  auf schmaler Ansicht geprüft.
- Noch keine Rezeptdaten, Datenbank oder Eingabeformulare implementiert.


## Rezepteingabe

- Pflicht: Rezeptname, mindestens eine benannte Zutat mit positiver Grammmenge
  und mindestens ein benannter Schritt mit gültiger Dauer.
- Mengen akzeptieren Komma oder Punkt als Dezimalzeichen, ohne Tausendertrennzeichen.
  Die zulässige Menge liegt über 0 bis einschließlich 1.000.000 g pro Zutat.
- Dauer: ganze Stunden von 0 bis 8760 und Minuten von 0 bis 59; ein abschließender
  Meilenstein darf die Dauer 0 haben. Abkühlen explizit als Schritt erfassen.
- Bis zu 100 Zutaten bzw. Schritte. Name: 120 Zeichen, Zutatenname: 100,
  Kurzbeschreibung: 1.000, Anleitung pro Schritt: 5.000 Zeichen.
- Abbrechen verwirft den Entwurf. Vor dem Speichern werden keine Rezeptdaten
  geschrieben. Neuladen verwirft ebenfalls einen noch ungespeicherten Entwurf.
- Gleichnamige Rezepte sind erlaubt und werden als getrennte Einträge gespeichert.

## Tests ausführen

Gemeinsame fachliche Tests (das JVM-Ziel ist nur für Tests vorgesehen):

```powershell
.\gradlew.bat :shared:jvmTest
```

Wiederholbare Browserprüfung bei laufender Web-App; hierfür zusätzlich Node.js
mit npm installieren. Die App selbst benötigt weiterhin keine globale
Node.js-Installation.

```powershell
cd tests/browser
npm ci
npx playwright install chromium
npm test
```

Optional mit installiertem Edge statt heruntergeladenem Chromium in PowerShell:

```powershell
$env:BROWSER_CHANNEL = "msedge"
npm test
```

`APP_URL` kann die Adresse überschreiben. Die Prüfung nutzt isolierte
Browserkontexte und verändert keine Rezepte im persönlichen Browserprofil.
Screenshots landen unter `build/browser-tests/`.

## Verifikation von Schritt 4 (08.09.2026)

- Zehn gemeinsame Tests für Eingabeprüfung, Dezimalkomma, Dauergrenzen,
  stabile Identitäten, Umordnen und JSON-Datenkonvertierung erfolgreich.
- Web-Entwicklungsbuild und Android-Debug-Build erfolgreich.
- Browser: mehrere Zutaten und Schritte anlegen, entfernen, umordnen,
  speichern und nach Neuladen mit vollständigen Daten wieder laden.
- Abbrechen erzeugt keinen Datenbankeintrag. Gleiche Rezeptnamen bleiben
  getrennte Datensätze; doppelte IDs können keinen Datensatz überschreiben.
- Schreibfehler behalten den Entwurf; erneutes Speichern funktioniert.
  Während einer laufenden Speicherung sind erneutes Speichern und Abbrechen
  gesperrt. Lesefehler lassen vorhandene Daten unberührt; erneutes Laden funktioniert.
- Desktop- und schmale Formulare visuell geprüft; Eingabe und Speicherung
  auch in einem Browserkontext mit Touch-Unterstützung geprüft.
- Testdaten bleiben in isolierten Browserkontexten. Keine Änderungen an
  Rezepten im Browserprofil des Nutzers.
- Web-Produktionsbuild erfolgreich; optimierte Ausgabe startet ohne Laufzeitfehler und lädt einen isolierten gespeicherten Testdatensatz nach Neuladen korrekt.


## Rezepte bearbeiten

- In der Sammlung ein Rezept auswählen und im rechten Bereich „Rezept bearbeiten“
  öffnen. Auf schmalen Ansichten steht dieser Bereich unter der Sammlung.
- Alle Werte werden vorausgefüllt; Zutaten und Schritte lassen sich wie beim
  Anlegen ändern, hinzufügen, entfernen und umordnen.
- Stunden und Minuten werden aus der gespeicherten Dauer zurückgerechnet.
  Grammangaben erscheinen mit Dezimalkomma, ohne Rundung oder wissenschaftliche
  Schreibweise. Unverändertes Speichern erhält alle Werte und Identitäten.
- Die Rezept-ID bleibt erhalten, auch bei einer Umbenennung. Gleichnamige Rezepte
  werden nicht zusammengeführt. Beibehaltene Zutaten und Schritte behalten ihre IDs.
- Die Datenbankversion bleibt 1; zuvor angelegte Rezepte funktionieren weiter.
  Updates prüfen die vorhandene ID und ersetzen den Datensatz innerhalb einer
  Transaktion. Eine nicht vorhandene ID erzeugt beim Bearbeiten keinen neuen Eintrag.
- Änderungen werden erst beim erfolgreichen Speichern übernommen. Bei einem Fehler
  bleibt der Entwurf offen, das gespeicherte Original bleibt bestehen.
- Abbrechen und Neuladen verwerfen ungespeicherte Änderungen. Die Rezeptauswahl
  bleibt beim Abbrechen erhalten.

Die Browserprüfungen umfassen Anlegen und Bearbeiten. Einzelne Abläufe können
im Ordner `tests/browser` mit `npm run test:create` bzw. `npm run test:edit`
gestartet werden. Beide verwenden isolierte Testdaten.

Für eine Prüfung ohne automatische Seitenneuladungen zuerst die fertige Web-Ausgabe
bauen und anschließend im Ordner `tests/browser` ausführen:

```powershell
npm run test:production
```

Dieser Prüflauf startet selbst einen lokalen statischen Server für die bereits
gebaute Ausgabe und beendet ihn anschließend. Ein Entwicklungsserver ist dafür
nicht erforderlich.

## Verifikation von Schritt 5 (08.09.2026)

- 15 gemeinsame Tests erfolgreich, einschließlich verlustfreier Übernahme
  bestehender Werte, sehr kleiner Grammangaben und stabiler Rezept-/Eintrags-IDs.
- Web-Entwicklungsbuild, Android-Debug-Build und Web-Produktionsbuild erfolgreich.
- Anlegen und Bearbeiten vollständig gegen die fertige Web-Ausgabe geprüft.
- Bestehende Datensätze unverändert öffnen/speichern, umbenennen, Zutaten ändern,
  hinzufügen/entfernen, Schritte umordnen und Dauern ändern; nach Neuladen kontrolliert.
- Abbrechen verwirft Änderungen und behält die Auswahl bei. Ungültige Eingaben
  und Speicherfehler verändern das gespeicherte Original nicht; erneutes Speichern funktioniert.
- Gleichnamige Rezepte bleiben getrennt. Andere Rezepte bleiben unverändert.
  Updates mit einer fehlenden ID werden abgewiesen, ohne einen neuen Datensatz anzulegen.
- Desktop- und schmale Bearbeitungsansicht geprüft; keine Browser-Laufzeitfehler.
  Alle Browserdaten stammten aus isolierten Testkontexten, nicht aus dem Nutzerprofil.

## Rezeptanzeige und Mengenanpassung (Schritt 6)

- Unter der Rezeptliste stehen die Zutaten des ausgewählten Rezepts; rechts
  erscheint dessen vollständiger Ablauf. Auf schmalen Bildschirmen stehen
  Sammlung, Zutaten und Ablauf untereinander.
- Die nummerierte Schrittfolge zeigt Titel, vollständige Anleitungen und
  Einzeldauern. Die Gesamtdauer enthält alle gespeicherten Schritte, auch
  mehrtägige Ruhezeiten. Schritte mit 0 Minuten bleiben sichtbar.
- „1× Ansatz“ und „2× Ansatz“ wählen die Grundmenge bzw. doppelte Menge.
  Alternativ lässt sich das gewünschte Gesamtteiggewicht mit Dezimalkomma oder
  Dezimalpunkt eingeben (über 0 bis 100.000.000 g).
- Alle Zutaten werden proportional aus den ursprünglichen Werten berechnet;
  erst die Anzeige rundet auf zwei Nachkommastellen. Positive Mengen unter
  0,01 g werden als „< 0,01 g“ angezeigt. Gerundete Einzelwerte können in der
  Summe geringfügig vom Zielgewicht abweichen.
- Ungültige Eingaben zeigen einen Hinweis und behalten die letzte gültige
  Zutatenmenge bei. Die Mengenauswahl wird nicht gespeichert; Rezeptwechsel,
  Neuladen oder Rückkehr aus dem Editor beginnen wieder beim Grundrezept.
- Das gespeicherte Rezept, die Eingaben im Editor und die Schrittdauern bleiben
  von der Skalierung unverändert. Uhrzeiten folgen in Schritt 7.
- `npm run test:details` im Ordner `tests/browser` prüft die Anzeige und
  Skalierung mit isolierten Browserdaten. Die Prüfung ist auch in `npm test`
  und `npm run test:production` enthalten.

## Verifikation von Schritt 6 (08.09.2026)

- 20 gemeinsame Tests erfolgreich: zusätzlich proportionale Skalierung,
  unveränderte Originaldaten, Dezimaleingaben, ungültige Zielgewichte,
  Rundung sowie sehr kleine und große Mengen.
- Web-Entwicklungsbuild, Android-Debug-Build und Web-Produktionsbuild erfolgreich.
  Entwicklungs- und Produktionsbuild getrennt aufrufen, da die Kotlin-Wasm-
  Aufgaben dasselbe Zwischenverzeichnis verwenden.
- Browserprüfungen für Anlegen, Bearbeiten und die neue Anzeige gegen die
  fertige Web-Ausgabe erfolgreich. Reihenfolge, mehrzeilige Anleitungen,
  mehrtägige Gesamtdauer und Schritte mit 0 Minuten kontrolliert.
- Einfacher/doppelter Ansatz, freies Zielgewicht, Fehlerkorrektur, Rezeptwechsel
  und Neuladen geprüft. Gespeicherte Daten und Editor-Grundmengen bleiben unverändert.
- Desktop- und schmale Zutaten-/Ablaufansicht visuell geprüft; alle Schritte
  erreichbar, keine Browser-Laufzeitfehler. Ausschließlich isolierte Testdaten.


## Start- und Zielplanung (Schritt 7)

- Beim ausgewählten Rezept „Startzeit“ oder „Zielzeit“ wählen. Datum als
  `TT.MM.JJJJ` und Uhrzeit als `HH:MM` im 24-Stunden-Format eingeben; die Felder
  sind zunächst mit dem aktuellen lokalen Datum und der Uhrzeit vorausgefüllt.
- „Planung berechnen“ erzeugt die Schrittzeiten. Startzeit ist der Beginn des
  ersten Schritts; Zielzeit ist das Ende des letzten Schritts. Ein eingetragener
  Abkühlschritt wird mitgerechnet. Auch Schritte mit 0 Minuten bleiben sichtbar.
- Jeder Schritt zeigt Beginn und Ende mit vollständigem Datum. Die Zusammenfassung
  nennt Start und Fertigstellung. UTC-Abstände machen wiederholte Uhrzeiten bei
  der Umstellung auf Winterzeit eindeutig.
- Es gilt die Zeitzone des Geräts, die im Formular angezeigt wird. Gerechnet wird
  mit verstrichenen Minuten: 24 Stunden Ruhezeit bleiben auch über eine
  Zeitumstellung hinweg 24 Stunden. Nicht existierende Uhrzeiten bei der
  Umstellung auf Sommerzeit werden abgewiesen. Bei doppelt vorkommenden Uhrzeiten
  wird das erste Vorkommen verwendet; der Hinweis steht im Formular.
- Ungültige Kalenderdaten und Uhrzeiten werden markiert. Änderungen an Eingaben
  oder Start-/Zielmodus entfernen die alte Berechnung, damit keine veralteten
  Uhrzeiten zum neuen Entwurf angezeigt werden.
- „Planung entfernen“ kehrt zur Anzeige der Dauern zurück. Die Planung gilt nur
  für die aktuelle Ansicht und wird bei Rezeptwechsel, Bearbeiten oder Neuladen
  zurückgesetzt. Rezeptdaten und Datenbankschema bleiben unverändert.
- Mengenskalierung ändert weder die geplanten Zeiten noch die Schrittdauern.
- Gemeinsame Zeitlogik mit `kotlinx-datetime 0.7.1`; Web-Zeitzonendaten über
  `@js-joda/timezone 2.25.2` mit eingecheckter Yarn-Sperrdatei. Android aktiviert
  Core-Library-Desugaring für die bestehende Mindestversion API 24.
  Grundlage: [kotlinx-datetime-Dokumentation für Version 0.7.1](https://github.com/Kotlin/kotlinx-datetime/tree/v0.7.1).
- `npm run test:schedule` im Ordner `tests/browser` prüft die Planung in einem
  isolierten Browserkontext mit der Zeitzone Europe/Berlin. Auch in `npm test`
  und `npm run test:production` enthalten.

## Verifikation von Schritt 7 (08.09.2026)

- 29 gemeinsame Tests erfolgreich, einschließlich Vorwärts-/Rückwärtsplanung,
  Jahres- und Monatswechsel, Schaltjahren, Abkühl-/Null-Minuten-Schritten,
  Eingabegrenzen und sehr langen Abläufen.
- Sommerzeitlücken werden abgewiesen; wiederholte Herbst-Uhrzeiten verwenden das
  erste Vorkommen. Verstrichene Dauern und UTC-Abstände gezielt geprüft.
- Web-Entwicklungsbuild, Android-Debug-Build und Web-Produktionsbuild erfolgreich.
  Android wurde gebaut; kein zusätzlicher Gerätetest durchgeführt.
- Vollständige Browserprüfung für Anlegen, Bearbeiten, Rezeptanzeige,
  Mengenskalierung und Zeitplanung gegen die fertige Web-Ausgabe erfolgreich.
- Start-/Zielmodus, mehrtägige Schrittzeiten, ungültige Eingaben, Zeitumstellungen,
  Entfernen der Planung, Rezeptwechsel und Neuladen im Browser geprüft.
- Desktop- und schmale Zeitplanansicht visuell kontrolliert. Keine
  Browser-Laufzeitfehler; alle Testdaten in isolierten Browserkontexten.
