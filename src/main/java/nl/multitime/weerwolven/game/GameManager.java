package nl.multitime.weerwolven.game;

import nl.multitime.weerwolven.Weerwolven;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GameManager {
    private final Weerwolven plugin;
    private final Map<String, Game> games;
    private final Map<UUID, String> playerGameMap;
    
    public GameManager(Weerwolven plugin) {
        this.plugin = plugin;
        this.games = new HashMap<>();
        this.playerGameMap = new HashMap<>();
    }
    
    public Game createGame(String gameId) {
        if (games.containsKey(gameId)) {
            return null;
        }
        
        Game game = new Game(gameId, plugin);
        games.put(gameId, game);
        return game;
    }
    
    public Game getGame(String gameId) {
        return games.get(gameId);
    }
    
    public Game getPlayerGame(Player player) {
        String gameId = playerGameMap.get(player.getUniqueId());
        return gameId != null ? games.get(gameId) : null;
    }
    
    public boolean joinGame(Player player, String gameId) {
        if (playerGameMap.containsKey(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "Je zit al in een spel! Verlaat eerst je huidige spel.");
            return false;
        }
        
        Game game = games.get(gameId);
        if (game == null) {
            game = createGame(gameId);
        }
        
        if (game.addPlayer(player)) {
            playerGameMap.put(player.getUniqueId(), gameId);
            player.sendMessage(ChatColor.GREEN + "Je bent toegevoegd aan spel: " + gameId);
            return true;
        } else {
            player.sendMessage(ChatColor.RED + "Kon niet deelnemen aan spel " + gameId + " (vol of al gestart)");
            return false;
        }
    }
    
    public boolean leaveGame(Player player) {
        String gameId = playerGameMap.remove(player.getUniqueId());
        if (gameId == null) {
            player.sendMessage(ChatColor.RED + "Je zit niet in een spel!");
            return false;
        }
        
        Game game = games.get(gameId);
        if (game != null) {
            game.removePlayer(player);
            
            if (game.getPlayers().isEmpty()) {
                games.remove(gameId);
            }
        }
        
        player.sendMessage(ChatColor.YELLOW + "Je hebt het spel verlaten.");
        return true;
    }
    
    public boolean voteForPlayer(Player voter, String targetName) {
        Game game = getPlayerGame(voter);
        if (game == null) {
            voter.sendMessage(ChatColor.RED + "Je zit niet in een spel!");
            return false;
        }
        
        GamePlayer gameVoter = game.getPlayer(voter.getUniqueId());
        GamePlayer target = game.getPlayer(targetName);
        
        if (gameVoter == null || target == null) {
            voter.sendMessage(ChatColor.RED + "Speler niet gevonden!");
            return false;
        }
        
        if (!gameVoter.isAlive()) {
            voter.sendMessage(ChatColor.RED + "Je bent dood en kunt niet stemmen!");
            return false;
        }
        
        if (!target.isAlive()) {
            voter.sendMessage(ChatColor.RED + "Je kunt niet stemmen op een dode speler!");
            return false;
        }
        
        GameState state = game.getState();
        if (state == GameState.DAY_VOTING) {
            if (game.voteForPlayer(gameVoter, target)) {
                voter.sendMessage(ChatColor.GREEN + "Je hebt gestemd op " + target.getName());
                return true;
            }
        } else if (state == GameState.NIGHT_WEERWOLVEN) {
            if (gameVoter.getRole().isEvil()) {
                if (game.voteForPlayer(gameVoter, target)) {
                    voter.sendMessage(ChatColor.RED + "Je hebt gestemd om " + target.getName() + " te elimineren");
                    return true;
                }
            } else {
                voter.sendMessage(ChatColor.RED + "Je kunt niet stemmen in deze fase!");
                return false;
            }
        } else {
            voter.sendMessage(ChatColor.RED + "Je kunt nu niet stemmen!");
            return false;
        }
        
        return false;
    }
    
    public boolean investigatePlayer(Player seer, String targetName) {
        Game game = getPlayerGame(seer);
        if (game == null) {
            seer.sendMessage(ChatColor.RED + "Je zit niet in een spel!");
            return false;
        }
        
        GamePlayer gameSeer = game.getPlayer(seer.getUniqueId());
        GamePlayer target = game.getPlayer(targetName);
        
        if (gameSeer == null || target == null) {
            seer.sendMessage(ChatColor.RED + "Speler niet gevonden!");
            return false;
        }
        
        if (game.getState() != GameState.NIGHT_SEER) {
            seer.sendMessage(ChatColor.RED + "Je kunt nu niet onderzoeken!");
            return false;
        }
        
        if (game.investigatePlayer(gameSeer, target)) {
            seer.sendMessage(ChatColor.BLUE + "Je onderzoekt " + target.getName() + "...");
            return true;
        } else {
            seer.sendMessage(ChatColor.RED + "Je kunt deze speler niet onderzoeken!");
            return false;
        }
    }
    
    public boolean healPlayer(Player heks, String targetName) {
        Game game = getPlayerGame(heks);
        if (game == null) {
            heks.sendMessage(ChatColor.RED + "Je zit niet in een spel!");
            return false;
        }
        
        GamePlayer gameHeks = game.getPlayer(heks.getUniqueId());
        GamePlayer target = game.getPlayer(targetName);
        
        if (gameHeks == null || target == null) {
            heks.sendMessage(ChatColor.RED + "Speler niet gevonden!");
            return false;
        }
        
        if (game.getState() != GameState.NIGHT_HEKS) {
            heks.sendMessage(ChatColor.RED + "Je kunt nu niet genezen!");
            return false;
        }
        
        if (game.healPlayer(gameHeks, target)) {
            heks.sendMessage(ChatColor.GREEN + "Je hebt " + target.getName() + " genezen!");
            return true;
        } else {
            heks.sendMessage(ChatColor.RED + "Je kunt deze speler niet genezen!");
            return false;
        }
    }
    
    public boolean poisonPlayer(Player heks, String targetName) {
        Game game = getPlayerGame(heks);
        if (game == null) {
            heks.sendMessage(ChatColor.RED + "Je zit niet in een spel!");
            return false;
        }
        
        GamePlayer gameHeks = game.getPlayer(heks.getUniqueId());
        GamePlayer target = game.getPlayer(targetName);
        
        if (gameHeks == null || target == null) {
            heks.sendMessage(ChatColor.RED + "Speler niet gevonden!");
            return false;
        }
        
        if (game.getState() != GameState.NIGHT_HEKS) {
            heks.sendMessage(ChatColor.RED + "Je kunt nu niet vergiftigen!");
            return false;
        }
        
        if (game.poisonPlayer(gameHeks, target)) {
            heks.sendMessage(ChatColor.DARK_RED + "Je hebt " + target.getName() + " vergiftigd!");
            return true;
        } else {
            heks.sendMessage(ChatColor.RED + "Je kunt deze speler niet vergiftigen!");
            return false;
        }
    }
    
    public boolean protectPlayer(Player beschermer, String targetName) {
        Game game = getPlayerGame(beschermer);
        if (game == null) {
            beschermer.sendMessage(ChatColor.RED + "Je zit niet in een spel!");
            return false;
        }
        
        GamePlayer gameBeschermer = game.getPlayer(beschermer.getUniqueId());
        GamePlayer target = game.getPlayer(targetName);
        
        if (gameBeschermer == null || target == null) {
            beschermer.sendMessage(ChatColor.RED + "Speler niet gevonden!");
            return false;
        }
        
        if (game.getState() != GameState.NIGHT_BESCHERMER) {
            beschermer.sendMessage(ChatColor.RED + "Je kunt nu niet beschermen!");
            return false;
        }
        
        if (game.protectPlayer(gameBeschermer, target)) {
            beschermer.sendMessage(ChatColor.YELLOW + "Je beschermt " + target.getName() + " vannacht!");
            return true;
        } else {
            beschermer.sendMessage(ChatColor.RED + "Je kunt deze speler niet beschermen!");
            return false;
        }
    }
    
    public void listGames(Player player) {
        if (games.isEmpty()) {
            player.sendMessage(ChatColor.YELLOW + "Er zijn momenteel geen actieve spellen.");
            return;
        }
        
        player.sendMessage(ChatColor.GOLD + "=== ACTIEVE SPELLEN ===");
        for (Map.Entry<String, Game> entry : games.entrySet()) {
            Game game = entry.getValue();
            String status = game.getState().getDisplayName();
            int playerCount = game.getPlayers().size();
            
            player.sendMessage(ChatColor.WHITE + entry.getKey() + ChatColor.GRAY + " - " + 
                             status + " (" + playerCount + " spelers)");
        }
    }
    
    public void showGameInfo(Player player) {
        Game game = getPlayerGame(player);
        if (game == null) {
            player.sendMessage(ChatColor.RED + "Je zit niet in een spel!");
            return;
        }
        
        GamePlayer gamePlayer = game.getPlayer(player.getUniqueId());
        if (gamePlayer == null) {
            return;
        }
        
        player.sendMessage(ChatColor.GOLD + "=== SPEL INFO ===");
        player.sendMessage(ChatColor.WHITE + "Spel ID: " + game.getGameId());
        player.sendMessage(ChatColor.WHITE + "Status: " + game.getState().getDisplayName());
        player.sendMessage(ChatColor.WHITE + "Dag: " + game.getDayNumber());
        player.sendMessage(ChatColor.WHITE + "Spelers: " + game.getAlivePlayers().size() + " levend");
        
        if (gamePlayer.isAlive()) {
            player.sendMessage(ChatColor.WHITE + "Je rol: " + gamePlayer.getRole().getColoredName());
            player.sendMessage(ChatColor.WHITE + "Status: " + ChatColor.GREEN + "LEVEND");
        } else {
            player.sendMessage(ChatColor.WHITE + "Status: " + ChatColor.RED + "DOOD");
        }
    }
    
    public void shutdown() {
        for (Game game : games.values()) {
            game.shutdown();
        }
        games.clear();
        playerGameMap.clear();
    }
}