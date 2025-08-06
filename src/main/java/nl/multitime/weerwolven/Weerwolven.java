package nl.multitime.weerwolven;

import nl.multitime.weerwolven.commands.WeerwolvenCommand;
import nl.multitime.weerwolven.game.GameManager;
import nl.multitime.weerwolven.listeners.PlayerListener;
import org.bukkit.plugin.java.JavaPlugin;

public final class Weerwolven extends JavaPlugin {

    private static Weerwolven instance;
    private GameManager gameManager;

    @Override
    public void onEnable() {
        instance = this;
        
        this.gameManager = new GameManager(this);
        
        WeerwolvenCommand commandHandler = new WeerwolvenCommand(this);
        getCommand("weerwolven").setExecutor(commandHandler);
        getCommand("weerwolven").setTabCompleter(commandHandler);
        
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        
        getLogger().info("Weerwolven plugin enabled!");
    }

    @Override
    public void onDisable() {
        if (gameManager != null) {
            gameManager.shutdown();
        }
        getLogger().info("Weerwolven plugin disabled!");
    }
    
    public static Weerwolven getInstance() {
        return instance;
    }
    
    public GameManager getGameManager() {
        return gameManager;
    }
}
