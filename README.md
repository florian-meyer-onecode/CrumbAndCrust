# Crumb & Crust

Brotrezepte und Backabläufe mit Compose Multiplatform für Web und Android.

## Stand

Schritt 4: Rezepte im Browser anlegen und lokal in IndexedDB speichern.
Das Formular erfasst Name, optionale Kurzbeschreibung, Zutaten in Gramm sowie
geordnete Schritte mit optionaler Anleitung und Dauer in Stunden/Minuten.
Zutaten und Schritte lassen sich hinzufügen oder entfernen; Schritte können
verschoben werden. Gespeicherte Rezepte erscheinen in der Sammlung und bleiben
nach Neuladen erhalten. Bearbeiten und die vollständige Ablaufanzeige folgen
in den nächsten Freigabeschritten. Siehe [PLAN.md](PLAN.md).

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
erst nach dem erfolgreichen Transaktionsabschluss bestätigt. Fehler überschreiben
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
