PokeTime
=====================

Ein Workshop mit JavaFX und den allerbesten 8-Bit-Retro-Grafiken um Kindern das Programmieren beizubringen.

http://bit.ly/pokemonworkshop

## Aufbau

Zurzeit nutzt PokeTime OpenJDK 11 und eine aktuelle Maven Version.

Bauen mit
```
mvn package
```

## Starten

Poketime kann als eigenständiges fat-jar gestartet werden. Die pi4j integration braucht
allerdings ein paar Umgebungsvariablen um richtig auf einem Raspberry Pi 2b zu funktionieren.
Also haben wir sie in ein start Script mit einbezogen.

```bash
./start
```
## benötigte Hardware

```bash
Raspberry Pi 3
TSL2561
ADX345
Pi Breakout Board

Breadboard
Connectors
```

fröhliches Hacken!

