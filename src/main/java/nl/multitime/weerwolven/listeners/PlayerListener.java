package nl.multitime.weerwolven.listeners;

import nl.multitime.weerwolven.Weerwolven;
import nl.multitime.weerwolven.game.Game;
import nl.multitime.weerwolven.game.GamePlayer;
import nl.multitime.weerwolven.game.GameState;
import nl.multitime.weerwolven.game.Role;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {
    private final Weerwolven plugin;
    
    public PlayerListener(Weerwolven plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        player.sendMessage("");
        player.sendMessage(ChatColor.GOLD + "=== WELKOM BIJ WEERWOLVEN ===");
        player.sendMessage(ChatColor.YELLOW + "Gebruik " + ChatColor.WHITE + "/ww help" + ChatColor.YELLOW + " voor alle commando's");
        player.sendMessage(ChatColor.YELLOW + "Gebruik " + ChatColor.WHITE + "/ww join <spel-id>" + ChatColor.YELLOW + " om mee te doen!");
        player.sendMessage("");
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        Game game = plugin.getGameManager().getPlayerGame(player);
        
        if (game != null) {
            plugin.getGameManager().leaveGame(player);
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        Game game = plugin.getGameManager().getPlayerGame(player);
        
        if (game == null) {
            return;
        }
        
        GamePlayer gamePlayer = game.getPlayer(player.getUniqueId());
        if (gamePlayer == null) {
            return;
        }
        
        event.setCancelled(true);
        
        String message = event.getMessage();
        GameState state = game.getState();
        
        if (!gamePlayer.isAlive()) {
            handleSpectatorChat(game, gamePlayer, message);
        } else if (state.isNightPhase()) {
            handleNightChat(game, gamePlayer, message, state);
        } else if (state.isDayPhase()) {
            handleDayChat(game, gamePlayer, message);
        } else {
            handleLobbyChat(game, gamePlayer, message);
        }
    }
    
    private void handleSpectatorChat(Game game, GamePlayer gamePlayer, String message) {
        String formattedMessage = ChatColor.DARK_GRAY + "[DOOD] " + 
                                 gamePlayer.getRole().getColor() + gamePlayer.getName() + 
                                 ChatColor.DARK_GRAY + ": " + ChatColor.GRAY + message;
        
        for (GamePlayer player : game.getPlayers()) {
            if (!player.isAlive()) {
                Player bukkitPlayer = plugin.getServer().getPlayer(player.getUuid());
                if (bukkitPlayer != null) {
                    bukkitPlayer.sendMessage(formattedMessage);
                }
            }
        }
    }
    
    private void handleNightChat(Game game, GamePlayer gamePlayer, String message, GameState state) {
        if (state == GameState.NIGHT_WEERWOLVEN && gamePlayer.getRole().isEvil()) {
            String formattedMessage = ChatColor.RED + "[WEERWOLF] " +
                                     gamePlayer.getName() + ": " + message;
            
            for (GamePlayer player : game.getPlayers()) {
                if (player.getRole().isEvil() && player.isAlive()) {
                    Player bukkitPlayer = plugin.getServer().getPlayer(player.getUuid());
                    if (bukkitPlayer != null) {
                        bukkitPlayer.sendMessage(formattedMessage);
                    }
                }
            }
        } else {
            Player bukkitPlayer = plugin.getServer().getPlayer(gamePlayer.getUuid());
            if (bukkitPlayer != null) {
                bukkitPlayer.sendMessage(ChatColor.RED + "Je kunt niet praten tijdens de nacht!");
            }
        }
    }
    
    private void handleDayChat(Game game, GamePlayer gamePlayer, String message) {
        String rolePrefix = "";
        if (gamePlayer.getRole() == Role.BURGEMEESTER) {
            rolePrefix = ChatColor.GOLD + "[BURGEMEESTER] ";
        }
        
        String formattedMessage = rolePrefix + ChatColor.WHITE + gamePlayer.getName() + 
                                 ChatColor.GRAY + ": " + ChatColor.WHITE + message;
        
        for (GamePlayer player : game.getPlayers()) {
            if (player.isAlive()) {
                Player bukkitPlayer = plugin.getServer().getPlayer(player.getUuid());
                if (bukkitPlayer != null) {
                    bukkitPlayer.sendMessage(formattedMessage);
                }
            }
        }
        
        String spectatorMessage = ChatColor.DARK_GRAY + "[SPECTATOR] " + formattedMessage;
        for (GamePlayer player : game.getPlayers()) {
            if (!player.isAlive()) {
                Player bukkitPlayer = plugin.getServer().getPlayer(player.getUuid());
                if (bukkitPlayer != null) {
                    bukkitPlayer.sendMessage(spectatorMessage);
                }
            }
        }
    }
    
    private void handleLobbyChat(Game game, GamePlayer gamePlayer, String message) {
        String formattedMessage = ChatColor.YELLOW + "[LOBBY] " + ChatColor.WHITE +
                                 gamePlayer.getName() + ChatColor.GRAY + ": " + 
                                 ChatColor.WHITE + message;
        
        for (GamePlayer player : game.getPlayers()) {
            Player bukkitPlayer = plugin.getServer().getPlayer(player.getUuid());
            if (bukkitPlayer != null) {
                bukkitPlayer.sendMessage(formattedMessage);
            }
        }
    }
}