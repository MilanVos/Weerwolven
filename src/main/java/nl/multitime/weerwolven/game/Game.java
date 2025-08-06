package nl.multitime.weerwolven.game;

import nl.multitime.weerwolven.Weerwolven;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.stream.Collectors;

public class Game {
    private final String gameId;
    private final Weerwolven plugin;
    private final Map<UUID, GamePlayer> players;
    private final Map<UUID, GamePlayer> spectators;
    
    private GameState state;
    private int dayNumber;
    private int minPlayers;
    private int maxPlayers;
    private BukkitTask gameTask;
    private int phaseTimer;
    
    private final int DAY_DISCUSSION_TIME = 300; // 5 minutes
    private final int DAY_VOTING_TIME = 120; // 2 minutes
    private final int NIGHT_PHASE_TIME = 60; // 1 minute per night phase
    
    private GamePlayer weerwolvenTarget;
    private GamePlayer seerTarget;
    private GamePlayer heksHealTarget;
    private GamePlayer heksPoisonTarget;
    private GamePlayer beschermerTarget;
    
    public Game(String gameId, Weerwolven plugin) {
        this.gameId = gameId;
        this.plugin = plugin;
        this.players = new HashMap<>();
        this.spectators = new HashMap<>();
        this.state = GameState.WAITING;
        this.dayNumber = 0;
        this.minPlayers = 4;
        this.maxPlayers = 20;
        this.phaseTimer = 0;
    }
    
    public String getGameId() {
        return gameId;
    }
    
    public GameState getState() {
        return state;
    }
    
    public void setState(GameState state) {
        this.state = state;
        broadcastToAll(ChatColor.YELLOW + "=== " + state.getDisplayName() + " ===");
    }
    
    public int getDayNumber() {
        return dayNumber;
    }
    
    public boolean addPlayer(Player player) {
        if (players.size() >= maxPlayers || state.isGameActive()) {
            return false;
        }
        
        GamePlayer gamePlayer = new GamePlayer(player);
        players.put(player.getUniqueId(), gamePlayer);
        
        broadcastToAll(ChatColor.GREEN + player.getName() + " heeft het spel betreden! (" + 
                      players.size() + "/" + maxPlayers + ")");
        
        if (players.size() >= minPlayers && state == GameState.WAITING) {
            startCountdown();
        }
        
        return true;
    }
    
    public boolean removePlayer(Player player) {
        GamePlayer gamePlayer = players.remove(player.getUniqueId());
        if (gamePlayer == null) {
            return false;
        }
        
        broadcastToAll(ChatColor.RED + player.getName() + " heeft het spel verlaten!");
        
        if (state.isGameActive()) {
            handlePlayerDeath(gamePlayer, "heeft het spel verlaten");
        } else if (players.size() < minPlayers && state == GameState.STARTING) {
            cancelCountdown();
        }
        
        return true;
    }
    
    public GamePlayer getPlayer(UUID uuid) {
        return players.get(uuid);
    }
    
    public GamePlayer getPlayer(String name) {
        return players.values().stream()
                .filter(p -> p.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }
    
    public Collection<GamePlayer> getPlayers() {
        return players.values();
    }
    
    public Collection<GamePlayer> getAlivePlayers() {
        return players.values().stream()
                .filter(GamePlayer::isAlive)
                .collect(Collectors.toList());
    }
    
    public Collection<GamePlayer> getPlayersWithRole(Role role) {
        return players.values().stream()
                .filter(p -> p.getRole() == role)
                .collect(Collectors.toList());
    }
    
    public Collection<GamePlayer> getAlivePlayersWithRole(Role role) {
        return players.values().stream()
                .filter(p -> p.getRole() == role && p.isAlive())
                .collect(Collectors.toList());
    }
    
    private void startCountdown() {
        setState(GameState.STARTING);
        phaseTimer = 10;
        
        gameTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (phaseTimer <= 0) {
                    startGame();
                    cancel();
                    return;
                }
                
                if (phaseTimer <= 5) {
                    broadcastToAll(ChatColor.YELLOW + "Spel start in " + phaseTimer + " seconden...");
                }
                
                phaseTimer--;
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }
    
    private void cancelCountdown() {
        if (gameTask != null) {
            gameTask.cancel();
            gameTask = null;
        }
        setState(GameState.WAITING);
        broadcastToAll(ChatColor.RED + "Niet genoeg spelers! Wachten op meer spelers...");
    }
    
    private void startGame() {
        setState(GameState.ROLE_ASSIGNMENT);
        assignRoles();
        sendRoleMessages();
        
        if (hasRole(Role.CUPIDO)) {
            startCupidoPhase();
        } else if (hasRole(Role.DIEF)) {
            startDiefPhase();
        } else {
            startFirstNight();
        }
    }
    
    private void assignRoles() {
        List<GamePlayer> playerList = new ArrayList<>(players.values());
        Collections.shuffle(playerList);
        
        List<Role> rolesToAssign = generateRoleList(playerList.size());
        
        for (int i = 0; i < playerList.size(); i++) {
            playerList.get(i).setRole(rolesToAssign.get(i));
        }
    }
    
    private List<Role> generateRoleList(int playerCount) {
        List<Role> roles = new ArrayList<>();
        
        roles.add(Role.WEERWOLF);
        roles.add(Role.SEER);
        
        if (playerCount >= 8) {
            roles.add(Role.WEERWOLF);
        }
        if (playerCount >= 12) {
            roles.add(Role.ALPHA_WEERWOLF);
        }
        
        if (playerCount >= 6) {
            roles.add(Role.HEKS);
        }
        if (playerCount >= 7) {
            roles.add(Role.JAGER);
        }
        if (playerCount >= 8) {
            roles.add(Role.BESCHERMER);
        }
        if (playerCount >= 10) {
            roles.add(Role.CUPIDO);
        }
        if (playerCount >= 12) {
            roles.add(Role.BURGEMEESTER);
        }
        
        while (roles.size() < playerCount) {
            roles.add(Role.BURGER);
        }
        
        Collections.shuffle(roles);
        return roles;
    }
    
    private void sendRoleMessages() {
        for (GamePlayer gamePlayer : players.values()) {
            Player player = Bukkit.getPlayer(gamePlayer.getUuid());
            if (player != null) {
                player.sendMessage("");
                player.sendMessage(ChatColor.GOLD + "=== JE ROL ===");
                player.sendMessage(ChatColor.WHITE + "Je bent: " + gamePlayer.getRole().getColoredName());
                player.sendMessage(ChatColor.GRAY + gamePlayer.getRole().getDescription());
                player.sendMessage("");
                
                if (gamePlayer.getRole().getTeam() == Role.Team.WEERWOLVEN) {
                    sendWeerwolfTeamInfo(player);
                }
            }
        }
    }
    
    private void sendWeerwolfTeamInfo(Player player) {
        player.sendMessage(ChatColor.RED + "=== WEERWOLF TEAM ===");
        for (GamePlayer teammate : getPlayersWithRole(Role.WEERWOLF)) {
            if (!teammate.getUuid().equals(player.getUniqueId())) {
                player.sendMessage(ChatColor.RED + "- " + teammate.getName());
            }
        }
        for (GamePlayer teammate : getPlayersWithRole(Role.ALPHA_WEERWOLF)) {
            if (!teammate.getUuid().equals(player.getUniqueId())) {
                player.sendMessage(ChatColor.DARK_RED + "- " + teammate.getName() + " (Alpha)");
            }
        }
        player.sendMessage("");
    }
    
    private boolean hasRole(Role role) {
        return players.values().stream().anyMatch(p -> p.getRole() == role);
    }
    
    private void startCupidoPhase() {
        setState(GameState.CUPIDO_PHASE);
        
        Collection<GamePlayer> cupidos = getAlivePlayersWithRole(Role.CUPIDO);
        for (GamePlayer cupido : cupidos) {
            Player player = Bukkit.getPlayer(cupido.getUuid());
            if (player != null) {
                player.sendMessage(ChatColor.LIGHT_PURPLE + "=== CUPIDO FASE ===");
                player.sendMessage(ChatColor.LIGHT_PURPLE + "Kies twee spelers die verliefd worden!");
                player.sendMessage(ChatColor.GRAY + "Gebruik: /ww love <speler1> <speler2>");
                player.sendMessage(ChatColor.YELLOW + "Je hebt 60 seconden om te kiezen...");
            }
        }
        
        new BukkitRunnable() {
            @Override
            public void run() {
                processCupidoAction();
                if (hasRole(Role.DIEF)) {
                    startDiefPhase();
                } else {
                    startFirstNight();
                }
            }
        }.runTaskLater(plugin, 20L * 60); // 60 seconds
    }
    
    private void startDiefPhase() {
        setState(GameState.DIEF_PHASE);
        
        Collection<GamePlayer> dieven = getAlivePlayersWithRole(Role.DIEF);
        for (GamePlayer dief : dieven) {
            Player player = Bukkit.getPlayer(dief.getUuid());
            if (player != null) {
                player.sendMessage(ChatColor.GRAY + "=== DIEF FASE ===");
                player.sendMessage(ChatColor.GRAY + "Je kunt een rol stelen van een andere speler!");
                player.sendMessage(ChatColor.GRAY + "Gebruik: /ww steal <speler>");
                player.sendMessage(ChatColor.YELLOW + "Je hebt 60 seconden om te kiezen...");
                player.sendMessage(ChatColor.DARK_GRAY + "Of laat het over aan het lot door niets te doen.");
            }
        }
        
        new BukkitRunnable() {
            @Override
            public void run() {
                processDiefAction();
                startFirstNight();
            }
        }.runTaskLater(plugin, 20L * 60); // 60 seconds
    }
    
    private void startFirstNight() {
        setState(GameState.FIRST_NIGHT);
        dayNumber = 1;
        broadcastToAll(ChatColor.DARK_BLUE + "De eerste nacht valt... Iedereen slaapt behalve de weerwolven!");
        
        resetNightActions();
        
        startNightPhases();
    }
    
    private void startNightPhases() {
        broadcastToAll(ChatColor.DARK_BLUE + "=== NACHT " + dayNumber + " ===");
        broadcastToAll(ChatColor.DARK_BLUE + "De nacht valt... Iedereen slaapt...");
        
        resetNightActions();
        
        startWeerwolfPhase();
    }
    
    private void startWeerwolfPhase() {
        setState(GameState.NIGHT_WEERWOLVEN);
        
        Collection<GamePlayer> weerwolves = getAlivePlayersWithRole(Role.WEERWOLF);
        weerwolves.addAll(getAlivePlayersWithRole(Role.ALPHA_WEERWOLF));
        
        for (GamePlayer wolf : weerwolves) {
            Player player = Bukkit.getPlayer(wolf.getUuid());
            if (player != null) {
                player.sendMessage(ChatColor.RED + "=== WEERWOLF FASE ===");
                player.sendMessage(ChatColor.RED + "Kies wie jullie willen elimineren!");
                player.sendMessage(ChatColor.GRAY + "Gebruik: /ww vote <speler>");
                player.sendMessage(ChatColor.YELLOW + "Je hebt " + NIGHT_PHASE_TIME + " seconden...");
                
                if (weerwolves.size() > 1) {
                    player.sendMessage(ChatColor.DARK_RED + "Andere weerwolven:");
                    for (GamePlayer otherWolf : weerwolves) {
                        if (!otherWolf.getUuid().equals(wolf.getUuid())) {
                            String prefix = otherWolf.getRole() == Role.ALPHA_WEERWOLF ? "(Alpha) " : "";
                            player.sendMessage(ChatColor.RED + "- " + prefix + otherWolf.getName());
                        }
                    }
                }
            }
        }
        
        new BukkitRunnable() {
            @Override
            public void run() {
                processWeerwolfVotes();
                startSeerPhase();
            }
        }.runTaskLater(plugin, 20L * NIGHT_PHASE_TIME);
    }
    
    private void startSeerPhase() {
        Collection<GamePlayer> seers = getAlivePlayersWithRole(Role.SEER);
        
        if (seers.isEmpty()) {
            startHeksPhase();
            return;
        }
        
        setState(GameState.NIGHT_SEER);
        
        for (GamePlayer seer : seers) {
            Player player = Bukkit.getPlayer(seer.getUuid());
            if (player != null) {
                player.sendMessage(ChatColor.BLUE + "=== WAARZEGGER FASE ===");
                player.sendMessage(ChatColor.BLUE + "Kies iemand om te onderzoeken!");
                player.sendMessage(ChatColor.GRAY + "Gebruik: /ww investigate <speler>");
                player.sendMessage(ChatColor.YELLOW + "Je hebt " + NIGHT_PHASE_TIME + " seconden...");
            }
        }
        
        new BukkitRunnable() {
            @Override
            public void run() {
                processSeerAction();
                startHeksPhase();
            }
        }.runTaskLater(plugin, 20L * NIGHT_PHASE_TIME);
    }
    
    private void startHeksPhase() {
        Collection<GamePlayer> heksen = getAlivePlayersWithRole(Role.HEKS);
        
        if (heksen.isEmpty()) {
            startBeschermerPhase();
            return;
        }
        
        setState(GameState.NIGHT_HEKS);
        
        for (GamePlayer heks : heksen) {
            Player player = Bukkit.getPlayer(heks.getUuid());
            if (player != null) {
                player.sendMessage(ChatColor.DARK_PURPLE + "=== HEKS FASE ===");
                if (weerwolvenTarget != null) {
                    player.sendMessage(ChatColor.RED + weerwolvenTarget.getName() + " wordt aangevallen door weerwolven!");
                    if (heks.hasHealPotion()) {
                        player.sendMessage(ChatColor.GREEN + "Je kunt hem/haar genezen met: /ww heal " + weerwolvenTarget.getName());
                    }
                }
                if (heks.hasPoisonPotion()) {
                    player.sendMessage(ChatColor.DARK_RED + "Je kunt iemand vergiftigen met: /ww poison <speler>");
                }
                if (!heks.hasHealPotion() && !heks.hasPoisonPotion()) {
                    player.sendMessage(ChatColor.GRAY + "Je hebt geen drankjes meer...");
                }
                player.sendMessage(ChatColor.YELLOW + "Je hebt " + NIGHT_PHASE_TIME + " seconden...");
            }
        }
        
        new BukkitRunnable() {
            @Override
            public void run() {
                processHeksActions();
                startBeschermerPhase();
            }
        }.runTaskLater(plugin, 20L * NIGHT_PHASE_TIME);
    }
    
    private void startBeschermerPhase() {
        Collection<GamePlayer> beschermers = getAlivePlayersWithRole(Role.BESCHERMER);
        
        if (beschermers.isEmpty()) {
            processNightResults();
            return;
        }
        
        setState(GameState.NIGHT_BESCHERMER);
        
        for (GamePlayer beschermer : beschermers) {
            Player player = Bukkit.getPlayer(beschermer.getUuid());
            if (player != null) {
                player.sendMessage(ChatColor.YELLOW + "=== BESCHERMER FASE ===");
                player.sendMessage(ChatColor.YELLOW + "Kies iemand om te beschermen!");
                player.sendMessage(ChatColor.GRAY + "Gebruik: /ww protect <speler>");
                player.sendMessage(ChatColor.YELLOW + "Je hebt " + NIGHT_PHASE_TIME + " seconden...");
            }
        }
        
        new BukkitRunnable() {
            @Override
            public void run() {
                processBeschermerAction();
                processNightResults();
            }
        }.runTaskLater(plugin, 20L * NIGHT_PHASE_TIME);
    }
    
    private void processWeerwolfVotes() {
        Map<GamePlayer, Integer> votes = new HashMap<>();
        
        Collection<GamePlayer> weerwolves = getAlivePlayersWithRole(Role.WEERWOLF);
        weerwolves.addAll(getAlivePlayersWithRole(Role.ALPHA_WEERWOLF));
        
        for (GamePlayer wolf : weerwolves) {
            if (wolf.getVotedFor() != null) {
                votes.put(wolf.getVotedFor(), votes.getOrDefault(wolf.getVotedFor(), 0) + 
                         (wolf.getRole() == Role.ALPHA_WEERWOLF ? 2 : 1));
            }
        }
        
        if (!votes.isEmpty()) {
            weerwolvenTarget = votes.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse(null);
        }
    }
    
    private void processSeerAction() {
        Collection<GamePlayer> seers = getAlivePlayersWithRole(Role.SEER);
        for (GamePlayer seer : seers) {
            if (seer.getVotedFor() != null) {
                Player player = Bukkit.getPlayer(seer.getUuid());
                if (player != null) {
                    GamePlayer target = seer.getVotedFor();
                    String result = target.getRole().isEvil() ? "KWAADAARDIG" : "GOED";
                    player.sendMessage(ChatColor.BLUE + target.getName() + " is " + 
                                     (target.getRole().isEvil() ? ChatColor.RED : ChatColor.GREEN) + result);
                }
            }
        }
    }
    
    private void processHeksActions() {
        Collection<GamePlayer> heksen = getAlivePlayersWithRole(Role.HEKS);
        for (GamePlayer heks : heksen) {
            if (heksHealTarget != null && heks.hasHealPotion()) {
                if (heksHealTarget == weerwolvenTarget) {
                    weerwolvenTarget = null; // Cancel weerwolf attack
                }
                heks.setHasHealPotion(false);
            }
            
            if (heksPoisonTarget != null && heks.hasPoisonPotion()) {
                heks.setHasPoisonPotion(false);
            }
        }
    }
    
    private void processBeschermerAction() {
        Collection<GamePlayer> beschermers = getAlivePlayersWithRole(Role.BESCHERMER);
        for (GamePlayer beschermer : beschermers) {
            if (beschermer.getVotedFor() != null) {
                beschermer.getVotedFor().setProtected(true);
            }
        }
    }
    
    private void processCupidoAction() {
        broadcastToAll(ChatColor.LIGHT_PURPLE + "Cupido heeft zijn keuze gemaakt...");
    }
    
    private void processDiefAction() {
        Collection<GamePlayer> dieven = getAlivePlayersWithRole(Role.DIEF);
        for (GamePlayer dief : dieven) {
            if (dief.getVotedFor() != null) {
                GamePlayer target = dief.getVotedFor();
                Role stolenRole = target.getRole();
                
                target.setRole(Role.DIEF);
                dief.setRole(stolenRole);
                
                Player diefPlayer = Bukkit.getPlayer(dief.getUuid());
                if (diefPlayer != null) {
                    diefPlayer.sendMessage(ChatColor.GRAY + "Je hebt de rol van " + target.getName() + " gestolen!");
                    diefPlayer.sendMessage(ChatColor.WHITE + "Je nieuwe rol: " + stolenRole.getColoredName());
                    diefPlayer.sendMessage(ChatColor.GRAY + stolenRole.getDescription());
                }
                
                Player targetPlayer = Bukkit.getPlayer(target.getUuid());
                if (targetPlayer != null) {
                    targetPlayer.sendMessage(ChatColor.GRAY + "Je rol is gestolen door de dief!");
                    targetPlayer.sendMessage(ChatColor.WHITE + "Je nieuwe rol: " + Role.DIEF.getColoredName());
                    targetPlayer.sendMessage(ChatColor.GRAY + Role.DIEF.getDescription());
                }
            }
        }
        broadcastToAll(ChatColor.GRAY + "De dief heeft zijn keuze gemaakt...");
    }
    
    private void processNightResults() {
        List<GamePlayer> deaths = new ArrayList<>();
        List<String> nightEvents = new ArrayList<>();
        
        if (weerwolvenTarget != null) {
            if (weerwolvenTarget.isProtected()) {
                nightEvents.add(ChatColor.YELLOW + weerwolvenTarget.getName() + " werd beschermd tegen een aanval!");
            } else {
                deaths.add(weerwolvenTarget);
                nightEvents.add(ChatColor.RED + weerwolvenTarget.getName() + " werd aangevallen door weerwolven!");
            }
        } else {
            nightEvents.add(ChatColor.GRAY + "De weerwolven hebben niemand aangevallen...");
        }
        
        if (heksPoisonTarget != null) {
            deaths.add(heksPoisonTarget);
            nightEvents.add(ChatColor.DARK_PURPLE + heksPoisonTarget.getName() + " werd vergiftigd door de heks!");
        }
        
        broadcastToAll(ChatColor.YELLOW + "=== NACHT RESULTATEN ===");
        if (deaths.isEmpty()) {
            broadcastToAll(ChatColor.GREEN + "Niemand is gestorven in de nacht!");
        } else {
            for (String event : nightEvents) {
                broadcastToAll(event);
            }
        }
        
        for (GamePlayer death : deaths) {
            handlePlayerDeath(death, "is gestorven in de nacht");
        }
        
        startDayPhase();
    }
    
    private void startDayPhase() {
        setState(GameState.DAY_DISCUSSION);
        broadcastToAll("");
        broadcastToAll(ChatColor.YELLOW + "=== DAG " + dayNumber + " ===");
        broadcastToAll(ChatColor.YELLOW + "De zon komt op... Tijd voor discussie!");
        
        Collection<GamePlayer> alive = getAlivePlayers();
        broadcastToAll(ChatColor.GREEN + "Nog " + alive.size() + " spelers in leven:");
        for (GamePlayer player : alive) {
            broadcastToAll(ChatColor.WHITE + "- " + player.getName());
        }
        
        broadcastToAll("");
        broadcastToAll(ChatColor.GRAY + "Discussie duurt " + (DAY_DISCUSSION_TIME / 60) + " minuten...");
        
        for (GamePlayer player : players.values()) {
            player.resetDayActions();
        }
        
        new BukkitRunnable() {
            @Override
            public void run() {
                startVotingPhase();
            }
        }.runTaskLater(plugin, 20L * DAY_DISCUSSION_TIME);
    }
    
    private void startVotingPhase() {
        setState(GameState.DAY_VOTING);
        broadcastToAll("");
        broadcastToAll(ChatColor.RED + "=== STEMMING ===");
        broadcastToAll(ChatColor.RED + "Tijd om te stemmen! Gebruik /ww vote <speler>");
        broadcastToAll(ChatColor.GRAY + "Stemming duurt " + (DAY_VOTING_TIME / 60) + " minuten...");
        
        Collection<GamePlayer> voters = getAlivePlayers();
        broadcastToAll(ChatColor.YELLOW + "Spelers die kunnen stemmen:");
        for (GamePlayer voter : voters) {
            String extra = voter.getRole() == Role.BURGEMEESTER ? " (2 stemmen)" : "";
            broadcastToAll(ChatColor.WHITE + "- " + voter.getName() + extra);
        }
        broadcastToAll("");
        
        new BukkitRunnable() {
            @Override
            public void run() {
                processDayVotes();
            }
        }.runTaskLater(plugin, 20L * DAY_VOTING_TIME);
    }
    
    private void processDayVotes() {
        Map<GamePlayer, Integer> votes = new HashMap<>();
        Map<GamePlayer, List<String>> voteDetails = new HashMap<>();
        
        for (GamePlayer player : getAlivePlayers()) {
            if (player.getVotedFor() != null) {
                int voteWeight = (player.getRole() == Role.BURGEMEESTER) ? 2 : 1;
                votes.put(player.getVotedFor(), votes.getOrDefault(player.getVotedFor(), 0) + voteWeight);
                
                voteDetails.computeIfAbsent(player.getVotedFor(), k -> new ArrayList<>())
                          .add(player.getName() + (voteWeight > 1 ? " (2 stemmen)" : ""));
            }
        }
        
        broadcastToAll("");
        broadcastToAll(ChatColor.YELLOW + "=== STEMMING RESULTATEN ===");
        
        if (!votes.isEmpty()) {
            for (Map.Entry<GamePlayer, Integer> entry : votes.entrySet()) {
                GamePlayer candidate = entry.getKey();
                int voteCount = entry.getValue();
                List<String> voters = voteDetails.get(candidate);
                
                broadcastToAll(ChatColor.WHITE + candidate.getName() + ": " + ChatColor.YELLOW + voteCount + 
                             " stem" + (voteCount != 1 ? "men" : "") + ChatColor.GRAY + " (" + String.join(", ", voters) + ")");
            }
            
            GamePlayer eliminated = votes.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse(null);
            
            if (eliminated != null) {
                broadcastToAll("");
                broadcastToAll(ChatColor.RED + eliminated.getName() + " wordt geëlimineerd met " + votes.get(eliminated) + " stemmen!");
                handlePlayerDeath(eliminated, "is geëlimineerd door stemming");
            }
        } else {
            broadcastToAll(ChatColor.YELLOW + "Niemand is geëlimineerd - geen stemmen uitgebracht!");
        }
        
        if (!checkWinConditions()) {
            dayNumber++;
            startNightPhases();
        }
    }
    
    private void handlePlayerDeath(GamePlayer player, String reason) {
        player.setAlive(false);
        broadcastToAll(ChatColor.RED + player.getName() + " " + reason + "!");
        broadcastToAll(ChatColor.GRAY + player.getName() + " was " + player.getFullRoleDescription());
        
        if (player.hasLover() && player.getLover().isAlive()) {
            GamePlayer lover = player.getLover();
            lover.setAlive(false);
            broadcastToAll(ChatColor.LIGHT_PURPLE + lover.getName() + " sterft van verdriet!");
            broadcastToAll(ChatColor.GRAY + lover.getName() + " was " + lover.getFullRoleDescription());
        }
        
        if (player.getRole() == Role.JAGER && player.isAlive()) {
            player.setCanRevenge(true);
            Player bukkitPlayer = Bukkit.getPlayer(player.getUuid());
            if (bukkitPlayer != null) {
                bukkitPlayer.sendMessage("");
                bukkitPlayer.sendMessage(ChatColor.DARK_GREEN + "=== JAGER WRAAK ===");
                bukkitPlayer.sendMessage(ChatColor.DARK_GREEN + "Je bent de jager! Kies iemand om mee te nemen!");
                bukkitPlayer.sendMessage(ChatColor.GRAY + "Gebruik: /ww revenge <speler>");
                bukkitPlayer.sendMessage(ChatColor.YELLOW + "Je hebt 30 seconden om te kiezen...");
                
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (player.canRevenge()) {
                            player.setCanRevenge(false);
                            bukkitPlayer.sendMessage(ChatColor.RED + "Te laat! Je wraak is verloren...");
                        }
                    }
                }.runTaskLater(plugin, 20L * 30); // 30 seconds
            }
        }
        
        spectators.put(player.getUuid(), player);
    }
    
    private boolean checkWinConditions() {
        Collection<GamePlayer> alive = getAlivePlayers();
        
        if (alive.size() == 2) {
            GamePlayer player1 = alive.iterator().next();
            GamePlayer player2 = alive.stream().skip(1).findFirst().orElse(null);
            
            if (player1 != null && player2 != null && 
                player1.hasLover() && player2.hasLover() && 
                player1.getLover() == player2 && player2.getLover() == player1) {
                
                endGame(Role.Team.SPECIAL);
                return true;
            }
        }
        
        long weerwolves = alive.stream().filter(p -> p.getRole().isEvil()).count();
        long villagers = alive.stream().filter(p -> !p.getRole().isEvil()).count();
        
        if (weerwolves == 0) {
            endGame(Role.Team.DORPELINGEN);
            return true;
        } else if (weerwolves >= villagers) {
            endGame(Role.Team.WEERWOLVEN);
            return true;
        }
        
        return false;
    }
    
    private void endGame(Role.Team winningTeam) {
        setState(GameState.GAME_ENDED);
        broadcastToAll("");
        broadcastToAll(ChatColor.GOLD + "=== SPEL BEËINDIGD ===");
        broadcastToAll(winningTeam.getColor() + winningTeam.getDisplayName() + " hebben gewonnen!");
        broadcastToAll("");
        
        broadcastToAll(ChatColor.YELLOW + "=== ROLLEN ===");
        for (GamePlayer player : players.values()) {
            String status = player.isAlive() ? ChatColor.GREEN + "LEVEND" : ChatColor.RED + "DOOD";
            broadcastToAll(status + ChatColor.WHITE + " " + player.getName() + ": " + player.getFullRoleDescription());
        }
        
        if (gameTask != null) {
            gameTask.cancel();
        }
    }
    
    private void resetNightActions() {
        weerwolvenTarget = null;
        seerTarget = null;
        heksHealTarget = null;
        heksPoisonTarget = null;
        beschermerTarget = null;
        
        for (GamePlayer player : players.values()) {
            player.resetNightActions();
        }
    }
    
    public void broadcastToAll(String message) {
        for (GamePlayer gamePlayer : players.values()) {
            Player player = Bukkit.getPlayer(gamePlayer.getUuid());
            if (player != null) {
                player.sendMessage(message);
            }
        }
        for (GamePlayer spectator : spectators.values()) {
            Player player = Bukkit.getPlayer(spectator.getUuid());
            if (player != null) {
                player.sendMessage(ChatColor.DARK_GRAY + "[SPECTATOR] " + ChatColor.RESET + message);
            }
        }
    }
    
    public void broadcastToTeam(Role.Team team, String message) {
        for (GamePlayer gamePlayer : players.values()) {
            if (gamePlayer.getRole().getTeam() == team && gamePlayer.isAlive()) {
                Player player = Bukkit.getPlayer(gamePlayer.getUuid());
                if (player != null) {
                    player.sendMessage(message);
                }
            }
        }
    }
    
    public boolean voteForPlayer(GamePlayer voter, GamePlayer target) {
        if (!voter.isAlive() || !target.isAlive()) {
            return false;
        }
        
        voter.setVotedFor(target);
        return true;
    }
    
    public boolean investigatePlayer(GamePlayer seer, GamePlayer target) {
        if (seer.getRole() != Role.SEER || !seer.isAlive() || !target.isAlive()) {
            return false;
        }
        
        seer.setVotedFor(target);
        return true;
    }
    
    public boolean healPlayer(GamePlayer heks, GamePlayer target) {
        if (heks.getRole() != Role.HEKS || !heks.hasHealPotion() || !heks.isAlive()) {
            return false;
        }
        
        heksHealTarget = target;
        return true;
    }
    
    public boolean poisonPlayer(GamePlayer heks, GamePlayer target) {
        if (heks.getRole() != Role.HEKS || !heks.hasPoisonPotion() || !heks.isAlive() || !target.isAlive()) {
            return false;
        }
        
        heksPoisonTarget = target;
        return true;
    }
    
    public boolean protectPlayer(GamePlayer beschermer, GamePlayer target) {
        if (beschermer.getRole() != Role.BESCHERMER || !beschermer.isAlive() || !target.isAlive()) {
            return false;
        }
        
        beschermer.setVotedFor(target);
        return true;
    }
    
    public boolean lovePlayer(GamePlayer cupido, GamePlayer lover1, GamePlayer lover2) {
        if (cupido.getRole() != Role.CUPIDO || !cupido.isAlive() || 
            !lover1.isAlive() || !lover2.isAlive() || lover1 == lover2) {
            return false;
        }
        
        lover1.setLover(lover2);
        lover2.setLover(lover1);
        
        Player cupidoPlayer = Bukkit.getPlayer(cupido.getUuid());
        if (cupidoPlayer != null) {
            cupidoPlayer.sendMessage(ChatColor.LIGHT_PURPLE + "Je hebt " + lover1.getName() + 
                                   " en " + lover2.getName() + " verliefd gemaakt!");
        }
        
        Player lover1Player = Bukkit.getPlayer(lover1.getUuid());
        if (lover1Player != null) {
            lover1Player.sendMessage(ChatColor.LIGHT_PURPLE + "Je bent verliefd geworden op " + lover2.getName() + "!");
            lover1Player.sendMessage(ChatColor.LIGHT_PURPLE + "Als je geliefde sterft, sterf jij ook!");
            lover1Player.sendMessage(ChatColor.WHITE + "Je rol: " + lover1.getFullRoleDescription());
        }
        
        Player lover2Player = Bukkit.getPlayer(lover2.getUuid());
        if (lover2Player != null) {
            lover2Player.sendMessage(ChatColor.LIGHT_PURPLE + "Je bent verliefd geworden op " + lover1.getName() + "!");
            lover2Player.sendMessage(ChatColor.LIGHT_PURPLE + "Als je geliefde sterft, sterf jij ook!");
            lover2Player.sendMessage(ChatColor.WHITE + "Je rol: " + lover2.getFullRoleDescription());
        }
        
        return true;
    }
    
    public boolean stealRole(GamePlayer dief, GamePlayer target) {
        if (dief.getRole() != Role.DIEF || !dief.isAlive() || !target.isAlive() || dief == target) {
            return false;
        }
        
        dief.setVotedFor(target);
        
        Player diefPlayer = Bukkit.getPlayer(dief.getUuid());
        if (diefPlayer != null) {
            diefPlayer.sendMessage(ChatColor.GRAY + "Je hebt gekozen om de rol van " + target.getName() + " te stelen!");
        }
        
        return true;
    }
    
    public boolean revengeKill(GamePlayer jager, GamePlayer target) {
        if (jager.getRole() != Role.JAGER || !jager.canRevenge() || !target.isAlive()) {
            return false;
        }
        
        jager.setCanRevenge(false);
        handlePlayerDeath(target, "is meegenomen door de jager");
        
        return true;
    }
    
    public void showGameStatus(Player player) {
        GamePlayer gamePlayer = getPlayer(player.getUniqueId());
        if (gamePlayer == null) return;
        
        player.sendMessage("");
        player.sendMessage(ChatColor.GOLD + "=== SPEL STATUS ===");
        player.sendMessage(ChatColor.WHITE + "Spel ID: " + ChatColor.YELLOW + gameId);
        player.sendMessage(ChatColor.WHITE + "Status: " + ChatColor.YELLOW + state.getDisplayName());
        player.sendMessage(ChatColor.WHITE + "Dag: " + ChatColor.YELLOW + dayNumber);
        player.sendMessage(ChatColor.WHITE + "Spelers: " + ChatColor.GREEN + getAlivePlayers().size() + 
                          ChatColor.WHITE + "/" + ChatColor.YELLOW + players.size());
        
        if (gamePlayer.isAlive()) {
            player.sendMessage(ChatColor.WHITE + "Je rol: " + gamePlayer.getFullRoleDescription());
            if (gamePlayer.hasLover()) {
                player.sendMessage(ChatColor.LIGHT_PURPLE + "Je geliefde: " + gamePlayer.getLover().getName());
            }
        } else {
            player.sendMessage(ChatColor.RED + "Je bent dood - je kijkt toe als toeschouwer");
        }
        player.sendMessage("");
    }
    
    public void shutdown() {
        if (gameTask != null) {
            gameTask.cancel();
        }
        broadcastToAll(ChatColor.RED + "Spel wordt afgesloten...");
    }
}