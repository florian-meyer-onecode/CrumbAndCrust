# Crumb & Crust

Brotrezepte und Backabläufe mit Compose Multiplatform für Web und Android.

## Stand

Schritt 3: responsiver Homescreen im Stil der Designvorlage. Rezeptliste und
Backplan zeigen Leerzustände. „Rezept hinzufügen“ zeigt vorerst einen
schließbaren Verfügbarkeitshinweis. Das Eingabeformular und die lokale
Datenbank folgen in Schritt 4 nach Freigabe. Den abgestimmten Umfang
beschreibt [PLAN.md](PLAN.md).

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

Lokale Datenhaltung wird in Schritt 4 mit IndexedDB ergänzt. Browserdaten
werden dann pro Browserprofil und Origin gespeichert; sie werden nicht mit
Android synchronisiert.

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
