# HugoOreOverlay 0.3

Fabric-Client-Mod für Minecraft Java 1.21.11 (Java 21).

## Bedienung

- Taste **O** startet den Scan.
- Der Scan ist absichtlich auf eine lokale Singleplayer-Welt beschränkt.
- Vor dem Scan wird geprüft, ob der Welt-Seed exakt `-2394875239847523984` ist.
- Suchradius: **256 Blöcke horizontal**, **16 Blöcke nach oben und unten**.
- Unterstützte Erze: Diamant, Gold, Eisen, Redstone, Lapis, Smaragd, Kupfer, Kohle, Nether-Gold, Netherquarz und Ancient Debris.
- Pro Erztyp wird die nächste gefundene Position mit X/Y/Z im Chat ausgegeben.

## Wichtige technische Grenze

Der Seed wird zur Sicherheit mit dem Seed der lokalen integrierten Singleplayer-Welt abgeglichen. Die aktuelle Version ist **kein Seed-Worldgen-Prädiktor**: Sie scannt ausschließlich bereits geladene Blockdaten. Nicht geladene Chunks und durch Server-Anti-Xray ersetzte/verdeckte Blockdaten werden nicht rekonstruiert. Auf Multiplayer-Servern ist der Scan deaktiviert.

## Build

Das Projekt verwendet GitHub Actions. Öffne im Repository **Actions**, starte **Build HugoOreOverlay**, und lade nach erfolgreichem Lauf das Artefakt **HugoOreOverlay-1.21.11** herunter. Die JAR kommt in `%appdata%\\.minecraft\\mods`.

Benötigt Fabric Loader und Fabric API für Minecraft 1.21.11 sowie Java 21.
