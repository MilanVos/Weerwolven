package nl.multitime.weerwolven.commands;

import nl.multitime.weerwolven.Weerwolven;
import nl.multitime.weerwolven.game.GameManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WeerwolvenCommand implements CommandExecutor, TabCompleter {
    private final Weerwolven plugin;
    private final GameManager gameManager;
    
    public WeerwolvenCommand(Weerwolven plugin) {
        this.plugin = plugin;
        this.gameManager = plugin.getGameManager();
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Dit commando kan alleen door spelers worden gebruikt!");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (args.length == 0) {
            showHelp(player);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "join":
                handleJoin(player, args);
                break;
            case "leave":
                handleLeave(player);
                break;
            case "vote":
                handleVote(player, args);
                break;
            case "investigate":
                handleInvestigate(player, args);
                break;
            case "heal":
                handleHeal(player, args);
                break;
            case "poison":
                handlePoison(player, args);
                break;
            case "protect":
                handleProtect(player, args);
                break;
            case "list":
                handleList(player);
                break;
            case "info":
                handleInfo(player);
                break;
            case "help":
                showHelp(player);
                break;
            default:
                player.sendMessage(ChatColor.RED + "Onbekend commando! Gebruik /ww help voor hulp.");
                break;
        }
        
        return true;
    }
    
    private void handleJoin(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Gebruik: /ww join <spel-id>");
            return;
        }
        
        String gameId = args[1];
        gameManager.joinGame(player, gameId);
    }
    
    private void handleLeave(Player player) {
        gameManager.leaveGame(player);
    }
    
    private void handleVote(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Gebruik: /ww vote <speler>");
            return;
        }
        
        String targetName = args[1];
        gameManager.voteForPlayer(player, targetName);
    }
    
    private void handleInvestigate(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Gebruik: /ww investigate <speler>");
            return;
        }
        
        String targetName = args[1];
        gameManager.investigatePlayer(player, targetName);
    }
    
    private void handleHeal(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Gebruik: /ww heal <speler>");
            return;
        }
        
        String targetName = args[1];
        gameManager.healPlayer(player, targetName);
    }
    
    private void handlePoison(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Gebruik: /ww poison <speler>");
            return;
        }
        
        String targetName = args[1];
        gameManager.poisonPlayer(player, targetName);
    }
    
    private void handleProtect(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Gebruik: /ww protect <speler>");
            return;
        }
        
        String targetName = args[1];
        gameManager.protectPlayer(player, targetName);
    }
    
    private void handleList(Player player) {
        gameManager.listGames(player);
    }
    
    private void handleInfo(Player player) {
        gameManager.showGameInfo(player);
    }
    
    private void showHelp(Player player) {
        player.sendMessage("");
        player.sendMessage(ChatColor.GOLD + "=== WEERWOLVEN COMMANDO'S ===");
        player.sendMessage(ChatColor.YELLOW + "/ww join <spel-id>" + ChatColor.WHITE + " - Doe mee aan een spel");
        player.sendMessage(ChatColor.YELLOW + "/ww leave" + ChatColor.WHITE + " - Verlaat je huidige spel");
        player.sendMessage(ChatColor.YELLOW + "/ww vote <speler>" + ChatColor.WHITE + " - Stem op een speler");
        player.sendMessage(ChatColor.YELLOW + "/ww investigate <speler>" + ChatColor.WHITE + " - Onderzoek een speler (Waarzegger)");
        player.sendMessage(ChatColor.YELLOW + "/ww heal <speler>" + ChatColor.WHITE + " - Genees een speler (Heks)");
        player.sendMessage(ChatColor.YELLOW + "/ww poison <speler>" + ChatColor.WHITE + " - Vergiftig een speler (Heks)");
        player.sendMessage(ChatColor.YELLOW + "/ww protect <speler>" + ChatColor.WHITE + " - Bescherm een speler (Beschermer)");
        player.sendMessage(ChatColor.YELLOW + "/ww list" + ChatColor.WHITE + " - Toon alle actieve spellen");
        player.sendMessage(ChatColor.YELLOW + "/ww info" + ChatColor.WHITE + " - Toon informatie over je huidige spel");
        player.sendMessage(ChatColor.YELLOW + "/ww help" + ChatColor.WHITE + " - Toon deze hulp");
        player.sendMessage("");
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player)) {
            return new ArrayList<>();
        }
        
        Player player = (Player) sender;
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            // First argument - subcommands
            List<String> subCommands = Arrays.asList(
                "join", "leave", "vote", "investigate", "heal", 
                "poison", "protect", "list", "info", "help"
            );
            
            String input = args[0].toLowerCase();
            for (String subCommand : subCommands) {
                if (subCommand.startsWith(input)) {
                    completions.add(subCommand);
                }
            }
        } else if (args.length == 2) {
            String subCommand = args[0].toLowerCase();
            
            if (subCommand.equals("join")) {
                completions.addAll(Arrays.asList("game1", "game2", "lobby"));
            } else if (Arrays.asList("vote", "investigate", "heal", "poison", "protect").contains(subCommand)) {
                String input = args[1].toLowerCase();
                for (Player onlinePlayer : plugin.getServer().getOnlinePlayers()) {
                    if (onlinePlayer.getName().toLowerCase().startsWith(input) && !onlinePlayer.equals(player)) {
                        completions.add(onlinePlayer.getName());
                    }
                }
            }
        }
        
        return completions;
    }
}