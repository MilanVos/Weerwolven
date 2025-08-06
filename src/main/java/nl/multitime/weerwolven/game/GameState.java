package nl.multitime.weerwolven.game;

public enum GameState {
    WAITING("Wachten op spelers"),
    STARTING("Spel start binnenkort"),
    ROLE_ASSIGNMENT("Rollen worden toegewezen"),
    CUPIDO_PHASE("Cupido kiest geliefden"),
    DIEF_PHASE("Dief kiest rol"),
    FIRST_NIGHT("Eerste nacht"),
    DAY_DISCUSSION("Dag - Discussie"),
    DAY_VOTING("Dag - Stemming"),
    NIGHT_WEERWOLVEN("Nacht - Weerwolven"),
    NIGHT_SEER("Nacht - Waarzegger"),
    NIGHT_HEKS("Nacht - Heks"),
    NIGHT_BESCHERMER("Nacht - Beschermer"),
    NIGHT_OTHER("Nacht - Andere rollen"),
    GAME_ENDED("Spel beëindigd");
    
    private final String displayName;
    
    GameState(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public boolean isNightPhase() {
        return this == FIRST_NIGHT || 
               this == NIGHT_WEERWOLVEN || 
               this == NIGHT_SEER || 
               this == NIGHT_HEKS || 
               this == NIGHT_BESCHERMER || 
               this == NIGHT_OTHER;
    }
    
    public boolean isDayPhase() {
        return this == DAY_DISCUSSION || this == DAY_VOTING;
    }
    
    public boolean isGameActive() {
        return this != WAITING && this != STARTING && this != GAME_ENDED;
    }
}