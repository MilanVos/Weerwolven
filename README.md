# Weerwolven van Wakkerdam - Minecraft Plugin

Een complete implementatie van het Weerwolven spel voor Minecraft servers.

## Features

### 🎭 Rollen
- **Weerwolven Team:**
  - Weerwolf - Elimineert 's nachts andere spelers
  - Alpha Weerwolf - Heeft extra stemkracht bij weerwolf stemming

- **Dorpelingen Team:**
  - Burger - Gewone dorpeling
  - Waarzegger - Kan 's nachts rollen onderzoeken
  - Heks - Heeft een genees- en gifdrankje
  - Jager - Kan bij dood iemand meenemen
  - Beschermer - Kan 's nachts iemand beschermen
  - Burgemeester - Heeft dubbele stemkracht overdag

- **Speciale Rollen:**
  - Cupido - Kiest geliefden aan het begin
  - Geliefde - Sterft als partner sterft
  - Dief - Kan rol stelen aan het begin

### 🌙 Game Mechanics
- **Dag/Nacht Cyclus** - Automatische fase overgangen
- **Stemming Systeem** - Democratische eliminatie overdag
- **Rol-specifieke Acties** - Unieke abilities per rol
- **Chat Systeem** - Verschillende chat modes per fase
- **Spectator Mode** - Dode spelers kunnen toekijken

### 🎮 Lobby Systeem
- Meerdere games tegelijk
- Automatische rol toewijzing
- Configureerbare speler limieten
- Real-time game status

## Commando's

### Basis Commando's
- `/ww join <spel-id>` - Doe mee aan een spel
- `/ww leave` - Verlaat je huidige spel
- `/ww list` - Toon alle actieve spellen
- `/ww info` - Toon informatie over je huidige spel
- `/ww help` - Toon alle commando's

### Spel Commando's
- `/ww vote <speler>` - Stem op een speler (dag/nacht)
- `/ww investigate <speler>` - Onderzoek een speler (Waarzegger)
- `/ww heal <speler>` - Genees een speler (Heks)
- `/ww poison <speler>` - Vergiftig een speler (Heks)
- `/ww protect <speler>` - Bescherm een speler (Beschermer)

## Installatie

1. Compileer de plugin met Maven of IntelliJ IDEA
2. Plaats het JAR bestand in je `plugins/` folder
3. Herstart je server
4. Spelers kunnen nu `/ww join game1` gebruiken om te beginnen!

## Spel Flow

### 1. Lobby Fase
- Spelers joinen met `/ww join <spel-id>`
- Minimum 4 spelers nodig om te starten
- Automatische countdown bij genoeg spelers

### 2. Rol Toewijzing
- Rollen worden automatisch toegewezen
- Spelers krijgen privé bericht met hun rol
- Weerwolven zien elkaar

### 3. Speciale Fasen (indien van toepassing)
- Cupido kiest geliefden
- Dief kiest nieuwe rol

### 4. Nacht Fasen
- **Weerwolven Fase:** Weerwolven kiezen slachtoffer
- **Waarzegger Fase:** Onderzoekt iemands rol
- **Heks Fase:** Kan genezen of vergiftigen
- **Beschermer Fase:** Kiest iemand om te beschermen

### 5. Dag Fasen
- **Discussie:** Spelers bespreken wie verdacht is
- **Stemming:** Democratische eliminatie
- Burgemeester heeft dubbele stem

### 6. Win Condities
- **Dorpelingen winnen:** Alle weerwolven geëlimineerd
- **Weerwolven winnen:** Evenveel of meer weerwolven dan dorpelingen

## Chat Systeem

### Dag Chat
- Alle levende spelers kunnen praten
- Dode spelers zien alles als spectator

### Nacht Chat
- Alleen weerwolven kunnen onderling praten tijdens hun fase
- Andere spelers kunnen niet praten

### Spectator Chat
- Dode spelers hebben eigen chat kanaal
- Kunnen het spel becommentariëren zonder levende spelers te beïnvloeden

## Technische Details

- **Minecraft Versie:** 1.19.4+
- **Server Software:** Paper/Spigot
- **Java Versie:** 17+
- **Dependencies:** Geen externe dependencies

## Configuratie

De plugin werkt out-of-the-box zonder configuratie. Standaard instellingen:
- Minimum spelers: 4
- Maximum spelers: 20
- Discussie tijd: 5 minuten
- Stemming tijd: 2 minuten
- Nacht fase tijd: 1 minuut per fase

## Ontwikkeling

Het project is gestructureerd in verschillende packages:
- `game/` - Core game logic en speler management
- `commands/` - Command handling en tab completion
- `listeners/` - Event handling voor chat en speler events

### Belangrijke Klassen
- `Game` - Hoofdklasse voor spel logica
- `GameManager` - Beheert meerdere games
- `GamePlayer` - Wrapper voor spelers met game-specifieke data
- `Role` - Enum met alle rollen en hun eigenschappen
- `GameState` - Enum voor verschillende spel fasen

## Toekomstige Features

- [ ] Configuratie bestand voor game settings
- [ ] Database integratie voor statistieken
- [ ] Meer rollen (Wilde Kind, Oude Man, etc.)
- [ ] Custom maps/arena's
- [ ] Spectator GUI voor dode spelers
- [ ] Game replay systeem

## Licentie

Dit project is gemaakt voor educatieve doeleinden.