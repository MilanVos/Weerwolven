package nl.multitime.weerwolven.game;

import org.bukkit.ChatColor;

public enum Role {
    // Weerwolven team
    WEERWOLF("Weerwolf", ChatColor.RED, Team.WEERWOLVEN, true, "Je bent een weerwolf! 's Nachts kun je samen met andere weerwolven iemand elimineren."),
    ALPHA_WEERWOLF("Alpha Weerwolf", ChatColor.DARK_RED, Team.WEERWOLVEN, true, "Je bent de alpha weerwolf! Je hebt een extra sterke stem bij weerwolf stemming."),
    
    // Dorpelingen team
    BURGER("Burger", ChatColor.GREEN, Team.DORPELINGEN, false, "Je bent een gewone burger. Help het dorp de weerwolven te vinden!"),
    SEER("Waarzegger", ChatColor.BLUE, Team.DORPELINGEN, false, "Je bent de waarzegger! Elke nacht kun je iemands rol onderzoeken."),
    HEKS("Heks", ChatColor.DARK_PURPLE, Team.DORPELINGEN, false, "Je bent de heks! Je hebt een genees- en een gifdrankje."),
    JAGER("Jager", ChatColor.DARK_GREEN, Team.DORPELINGEN, false, "Je bent de jager! Als je sterft, mag je iemand meenemen."),
    BESCHERMER("Beschermer", ChatColor.YELLOW, Team.DORPELINGEN, false, "Je bent de beschermer! Elke nacht kun je iemand beschermen."),
    BURGEMEESTER("Burgemeester", ChatColor.GOLD, Team.DORPELINGEN, false, "Je bent de burgemeester! Je stem telt dubbel tijdens de dag."),
    
    // Speciale rollen
    CUPIDO("Cupido", ChatColor.LIGHT_PURPLE, Team.DORPELINGEN, false, "Je bent cupido! Kies twee geliefden aan het begin van het spel."),
    GELIEFDE("Geliefde", ChatColor.LIGHT_PURPLE, Team.SPECIAL, false, "Je bent verliefd! Als je geliefde sterft, sterf jij ook."),
    
    // Neutrale rollen
    DIEF("Dief", ChatColor.GRAY, Team.NEUTRAL, false, "Je bent de dief! Je kunt aan het begin een rol stelen."),
    
    // Spectator
    SPECTATOR("Toeschouwer", ChatColor.DARK_GRAY, Team.SPECTATOR, false, "Je bent uitgeschakeld en kunt het spel bekijken.");
    
    private final String displayName;
    private final ChatColor color;
    private final Team team;
    private final boolean isEvil;
    private final String description;
    
    Role(String displayName, ChatColor color, Team team, boolean isEvil, String description) {
        this.displayName = displayName;
        this.color = color;
        this.team = team;
        this.isEvil = isEvil;
        this.description = description;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public ChatColor getColor() {
        return color;
    }
    
    public Team getTeam() {
        return team;
    }
    
    public boolean isEvil() {
        return isEvil;
    }
    
    public String getDescription() {
        return description;
    }
    
    public String getColoredName() {
        return color + displayName + ChatColor.RESET;
    }
    
    public enum Team {
        WEERWOLVEN("Weerwolven", ChatColor.RED),
        DORPELINGEN("Dorpelingen", ChatColor.GREEN),
        SPECIAL("Speciaal", ChatColor.LIGHT_PURPLE),
        NEUTRAL("Neutraal", ChatColor.GRAY),
        SPECTATOR("Toeschouwers", ChatColor.DARK_GRAY);
        
        private final String displayName;
        private final ChatColor color;
        
        Team(String displayName, ChatColor color) {
            this.displayName = displayName;
            this.color = color;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public ChatColor getColor() {
            return color;
        }
        
        public String getColoredName() {
            return color + displayName + ChatColor.RESET;
        }
    }
}