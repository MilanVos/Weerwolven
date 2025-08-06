package nl.multitime.weerwolven.game;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import java.util.UUID;

public class GamePlayer {
    private final UUID uuid;
    private final String name;
    private Role role;
    private Role secondaryRole; // For GELIEFDE status
    private boolean alive;
    private boolean isProtected;
    private GamePlayer lover;
    private boolean hasVoted;
    private GamePlayer votedFor;
    private boolean canUseAbility;
    private boolean hasUsedAbility;
    
    private boolean hasHealPotion;
    private boolean hasPoisonPotion;
    
    private boolean canRevenge;
    
    public GamePlayer(Player player) {
        this.uuid = player.getUniqueId();
        this.name = player.getName();
        this.role = Role.BURGER; // Default role
        this.secondaryRole = null;
        this.alive = true;
        this.isProtected = false;
        this.hasVoted = false;
        this.canUseAbility = true;
        this.hasUsedAbility = false;
        this.hasHealPotion = true;
        this.hasPoisonPotion = true;
        this.canRevenge = false;
    }
    
    public UUID getUuid() {
        return uuid;
    }
    
    public String getName() {
        return name;
    }
    
    public Role getRole() {
        return role;
    }
    
    public void setRole(Role role) {
        this.role = role;
    }
    
    public Role getSecondaryRole() {
        return secondaryRole;
    }
    
    public void setSecondaryRole(Role secondaryRole) {
        this.secondaryRole = secondaryRole;
    }
    
    public boolean hasSecondaryRole() {
        return secondaryRole != null;
    }
    
    public String getFullRoleDescription() {
        if (secondaryRole != null) {
            return role.getColoredName() + ChatColor.WHITE + " + " + secondaryRole.getColoredName();
        }
        return role.getColoredName();
    }
    
    public boolean isGeliefde() {
        return role == Role.GELIEFDE || secondaryRole == Role.GELIEFDE;
    }
    
    public boolean hasRole(Role checkRole) {
        return role == checkRole || secondaryRole == checkRole;
    }
    
    public boolean isAlive() {
        return alive;
    }
    
    public void setAlive(boolean alive) {
        this.alive = alive;
        if (!alive) {
            this.isProtected = false;
            this.hasVoted = false;
            this.votedFor = null;
        }
    }
    
    public boolean isProtected() {
        return isProtected;
    }
    
    public void setProtected(boolean isProtected) {
        this.isProtected = isProtected;
    }
    
    public GamePlayer getLover() {
        return lover;
    }
    
    public void setLover(GamePlayer lover) {
        this.lover = lover;
        if (lover != null) {
            if (this.role != Role.GELIEFDE) {
                this.secondaryRole = Role.GELIEFDE;
            }
        } else {
            if (this.secondaryRole == Role.GELIEFDE) {
                this.secondaryRole = null;
            }
        }
    }
    
    public boolean hasLover() {
        return lover != null;
    }
    
    public boolean hasVoted() {
        return hasVoted;
    }
    
    public void setHasVoted(boolean hasVoted) {
        this.hasVoted = hasVoted;
    }
    
    public GamePlayer getVotedFor() {
        return votedFor;
    }
    
    public void setVotedFor(GamePlayer votedFor) {
        this.votedFor = votedFor;
        this.hasVoted = votedFor != null;
    }
    
    public boolean canUseAbility() {
        return canUseAbility && alive;
    }
    
    public void setCanUseAbility(boolean canUseAbility) {
        this.canUseAbility = canUseAbility;
    }
    
    public boolean hasUsedAbility() {
        return hasUsedAbility;
    }
    
    public void setHasUsedAbility(boolean hasUsedAbility) {
        this.hasUsedAbility = hasUsedAbility;
    }
    
    public boolean hasHealPotion() {
        return hasHealPotion;
    }
    
    public void setHasHealPotion(boolean hasHealPotion) {
        this.hasHealPotion = hasHealPotion;
    }
    
    public boolean hasPoisonPotion() {
        return hasPoisonPotion;
    }
    
    public void setHasPoisonPotion(boolean hasPoisonPotion) {
        this.hasPoisonPotion = hasPoisonPotion;
    }
    
    public boolean canRevenge() {
        return canRevenge;
    }
    
    public void setCanRevenge(boolean canRevenge) {
        this.canRevenge = canRevenge;
    }
    
    public void resetNightActions() {
        this.hasVoted = false;
        this.votedFor = null;
        this.isProtected = false;
        this.canUseAbility = true;
    }
    
    public void resetDayActions() {
        this.hasVoted = false;
        this.votedFor = null;
    }
}