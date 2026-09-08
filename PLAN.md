# Crumb & Crust – Implementierungsplan

## Ziel und Umfang

Brotrezepte lokal verwalten und ihren Ablauf planen. Zunächst wird die
Browseranwendung umgesetzt. Android wird als Projektziel vorbereitet; Oberfläche
und Fachlogik werden zwischen Web und Android geteilt. Kein Backend, kein Konto
und keine Synchronisierung zwischen Geräten oder Browserprofilen.

Die unveränderten Referenzen sind `code.html`, `DESIGN.md` und `screen.png`.
Die Oberfläche wird deutsch, mit 24-Stunden-Zeiten. Farben und Layout orientieren
sich an HTML und Screenshot: Rezepte und Zutaten links, Ablauf rechts, auf
schmalen Bildschirmen untereinander.

## Freigabeschritte

Jeder Schritt endet mit Prüfung, passenden Commits auf `main` und einer
Zusammenfassung. Der nächste Schritt beginnt erst nach dem OK des Nutzers.

1. **Plan:** Funktionen, Architektur und Reihenfolge abstimmen. Freigegeben.
2. **Projekt:** Git, Gradle Wrapper, leeres Compose-Multiplatform-Projekt mit Web-
   und Android-Ziel, Prüfung der Werkzeuge und Builds, Startanleitung.
3. **Homescreen:** Kopfbereich, responsives Grundlayout und Leerzustände für
   Rezeptliste und Ablauf, Einstieg „Rezept hinzufügen“.
4. **Rezept anlegen:** Name, Kurzbeschreibung, Zutaten in Gramm und geordnete
   Schritte mit Titel, Anleitung und Dauer. Einträge hinzufügen/entfernen,
   Schritte verschieben, Eingaben prüfen, speichern/abbrechen. IndexedDB
   anbinden; Rezepte bleiben nach Neuladen erhalten und erscheinen in der Liste.
5. **Rezept bearbeiten:** Dasselbe Formular mit vorhandenen Daten; Speichern
   aktualisiert die bestehende Identität, Abbrechen verwirft Änderungen.
6. **Rezept anzeigen:** Zutaten, vertikale Schrittfolge und Dauern ohne Uhrzeiten.
   Einfacher/doppelter Ansatz oder gewünschtes Gesamtteiggewicht; proportional
   skalierte Anzeige ohne Änderung des gespeicherten Grundrezepts.
7. **Zeitplanung:** Start- oder Zielzeit mit Datum, Vorwärts-/Rückwärtsberechnung,
   sichtbare Datumswechsel, Planung entfernen. Mehrtägige Abläufe testen.

## Fachliche Regeln

- Ein Rezept enthält Zutaten und eine lineare, geordnete Liste von Schritten.
- Die Schrittdauer umfasst die Zeit vom Beginn bis zum nächsten Schritt bzw.
  beim letzten Schritt bis zum Ende des Rezepts. Keine parallelen Abläufe.
- Die Zielzeit ist das Ende des letzten Schritts. Abkühlen wird bei Bedarf als
  eigener Schritt mit Dauer erfasst und damit mitgerechnet.
- Gesamtdauern werden berechnet, nicht aus den Beispielbeschriftungen übernommen.
- Grammangaben sind die Grundlage der Mengenskalierung. Erweiterte Hydrations-
  und Bäckerprozentberechnungen sind zunächst nicht enthalten.
- Druck, Import/Export, Rezeptlöschung und vollständige Android-Speicherung
  bleiben spätere Erweiterungen.

## Architektur und Prüfung

- Kotlin und Compose Multiplatform, Web mit Kotlin/Wasm.
- Gemeinsames Modul für Oberfläche, Datenmodelle und Fachlogik; kleine getrennte
  Einstiegsmodule für Web und Android.
- Austauschbare Speicherschnittstelle, zunächst IndexedDB im Browser.
- Dauerhafte Browserdaten sind an Browserprofil und Origin gebunden. Ein Löschen
  der Websitedaten entfernt auch die Rezepte.
- Gradle- und Abhängigkeitsversionen werden festgeschrieben.
- Pro Schritt passende Build- und Browserprüfungen. Fachliche Tests für
  Skalierung, Validierung und Zeitrechnung werden mit den Funktionen ergänzt.
- Kein Backend erforderlich; ein lokaler Entwicklungsserver liefert die
  Webdateien aus. Vollständiger Offline-Start als PWA ist ein späteres Thema.

## Umsetzungsstand

Alle sieben freigegebenen Schritte sind implementiert und auf `main` committed.
Die Browser-Version speichert Rezepte lokal in IndexedDB. Mengenanpassung und
Zeitplanung gelten für die aktuelle Ansicht; sie verändern das Grundrezept nicht.
Android bleibt als gebautes Projektziel mit gemeinsamer Oberfläche vorbereitet,
mit browserexklusiver Speicherung wie für diese erste Version vereinbart.
Prüfungen und Bedienung sind in `README.md` dokumentiert.
