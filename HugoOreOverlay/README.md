# HugoOreOverlay (prototype)

Eigenständiger Fabric-Client-Mod für Minecraft Java 1.21.11.

## Aktueller Funktionsumfang

- Taste **O** startet einen Scan in einem Radius von 12 Blöcken horizontal und 8 Blöcken vertikal.
- Findet die jeweils nächste bereits clientseitig bekannte Position von Diamant, Gold, Eisen, Redstone, Lapis, Smaragd und Kupfer.
- Gibt die Koordinaten im Chat aus.

## Wichtige technische Grenze

Der Mod scannt ausschließlich Blockdaten, die der Server dem Client bereits übermittelt hat. Er kann keine nicht geladenen Chunks vorhersagen und umgeht kein serverseitiges Anti-Xray. Wenn HugoSMP versteckte Erze im Chunk-Datenpaket durch Stein ersetzt, kann dieser Client-Mod diese Erze nicht erkennen. Ein Seed allein liefert auf einem Server mit angepasster Erz- oder Weltgenerierung keine verlässlichen Erzkoordinaten.

## Bauen ohne eigenen PC-Gradle-Aufwand

Im Projekt ist GitHub Actions vorkonfiguriert:

1. ZIP entpacken und den Ordnerinhalt in ein neues GitHub-Repository hochladen.
2. Im Repository **Actions** öffnen.
3. **Build HugoOreOverlay** auswählen und **Run workflow** drücken.
4. Nach erfolgreichem Lauf das Artefakt **HugoOreOverlay-1.21.11** herunterladen.
5. Die JAR aus dem Artefakt in `%appdata%\\.minecraft\\mods` legen.

Benötigt Fabric Loader und Fabric API für Minecraft 1.21.11. Java 21 wird für den Build verwendet.

## Projekt lokal bauen

Mit Java 21 und Gradle 8.14.3:

```bash
gradle build
```

Die fertige JAR liegt danach in `build/libs/`.
