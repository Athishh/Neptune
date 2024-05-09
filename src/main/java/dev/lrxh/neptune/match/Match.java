package dev.lrxh.neptune.match;

import dev.lrxh.neptune.Neptune;
import dev.lrxh.neptune.arena.Arena;
import dev.lrxh.neptune.configs.impl.MessagesLocale;
import dev.lrxh.neptune.kit.Kit;
import dev.lrxh.neptune.match.impl.MatchState;
import dev.lrxh.neptune.match.impl.Participant;
import dev.lrxh.neptune.profile.Profile;
import dev.lrxh.neptune.profile.ProfileState;
import dev.lrxh.neptune.providers.clickable.Replacement;
import dev.lrxh.neptune.utils.CC;
import dev.lrxh.neptune.utils.PlayerUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@Getter
@Setter
public abstract class Match {
    public final List<UUID> spectators = new ArrayList<>();
    private final UUID uuid = UUID.randomUUID();
    private final HashSet<Location> placedBlocks = new HashSet<>();
    private final HashSet<Entity> entities = new HashSet<>();
    public MatchState matchState;
    public Arena arena;
    public Kit kit;
    public List<Participant> participants;
    public int rounds;
    private boolean duel;

    public void playSound(Sound sound) {
        for (Participant participant : participants) {
            Player player = Bukkit.getPlayer(participant.getPlayerUUID());
            if (player == null) continue;
            player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
        }
    }

    public Participant getParticipant(UUID playerUUID) {
        for (Participant participant : participants) {
            if (participant.getPlayerUUID().equals(playerUUID)) {
                return participant;
            }
        }
        return null;
    }

    public void sendTitle(String header, String footer, int duration) {
        for (Participant participant : participants) {
            PlayerUtil.sendTitle(participant.getPlayerUUID(), header, footer, duration);
        }
    }

    public void sendMessage(MessagesLocale message, Replacement... replacements) {
        for (Participant participant : participants) {
            message.send(participant.getPlayerUUID(), replacements);
        }
    }

    public void addSpectator(UUID playerUUID) {
        Player player = Bukkit.getPlayer(playerUUID);
        if (player == null) return;
        Profile profile = Neptune.get().getProfileManager().getByUUID(playerUUID);

        profile.setState(ProfileState.IN_SPECTATOR);
        profile.setMatch(this);
        spectators.add(playerUUID);

        player.setAllowFlight(true);
        player.setFlying(true);
        player.setGameMode(GameMode.SPECTATOR);

        for (Participant participant : participants) {
            Player participiantPlayer = Bukkit.getPlayer(participant.getPlayerUUID());
            if (participiantPlayer == null) return;
            player.showPlayer(Neptune.get(), participiantPlayer);
        }
        broadcast(MessagesLocale.SPECTATE_START, new Replacement("<player>", player.getName()));
    }

    public void setupPlayer(UUID playerUUID) {
        Player player = Bukkit.getPlayer(playerUUID);
        if (player == null) return;
        PlayerUtil.reset(player.getUniqueId());
        Profile profile = Neptune.get().getProfileManager().getByUUID(playerUUID);
        profile.setMatch(this);
        profile.setState(ProfileState.IN_GAME);
        PlayerUtil.giveKit(player.getUniqueId(), kit);
        profile.getData().setDuelRequest(null);

        Neptune.get().getLeaderboardManager().changes.add(playerUUID);
    }


    public void removeSpectator(UUID playerUUID, boolean sendMessage) {
        Player player = Bukkit.getPlayer(playerUUID);
        if (player == null) return;
        Profile profile = Neptune.get().getProfileManager().getByUUID(playerUUID);

        if (profile.getMatch() == null) return;
        PlayerUtil.reset(playerUUID);
        PlayerUtil.teleportToSpawn(playerUUID);
        profile.setState(ProfileState.LOBBY);
        profile.setMatch(null);

        spectators.remove(playerUUID);

        if (sendMessage) {
            broadcast(MessagesLocale.SPECTATE_STOP, new Replacement("<player>", player.getName()));
        }
    }

    public void broadcast(MessagesLocale messagesLocale, Replacement... replacements) {
        for (Participant participant : participants) {
            messagesLocale.send(participant.getPlayerUUID(), replacements);
        }

        for (UUID spectator : spectators) {
            messagesLocale.send(spectator, replacements);
        }
    }

    public void checkRules() {
        for (Participant participant : participants) {
            Player player = Bukkit.getPlayer(participant.getPlayerUUID());
            if (player == null) continue;
            if (kit.isDenyMovement()) {
                if (matchState.equals(MatchState.STARTING)) {
                    PlayerUtil.denyMovement(participant.getPlayerUUID());
                } else {
                    PlayerUtil.allowMovement(participant.getPlayerUUID());
                }
            }
            if (kit.isShowHP()) {
                if (matchState.equals(MatchState.STARTING)) {
                    showHealth(participant.getPlayerUUID());
                } else if (matchState.equals(MatchState.ENDING)) {
                    hideHealth();
                }
            }
        }
    }

    private void hideHealth(){
        for (Participant participant : participants) {
            Player player = Bukkit.getPlayer(participant.getPlayerUUID());
            if (player == null) return;
            Objective objective = player.getScoreboard().getObjective(DisplaySlot.BELOW_NAME);
            if (objective != null) {
                objective.unregister();
            }
        }
    }

    private void showHealth(UUID playerUUID) {
        Player player = Bukkit.getPlayer(playerUUID);
        if (player == null) return;
        for (Participant participant : participants) {
            Player viewer = Bukkit.getPlayer(participant.getPlayerUUID());
            if (viewer == null) return;

            Objective objective = viewer.getScoreboard().getObjective(DisplaySlot.BELOW_NAME);
            if (objective == null) {
                objective = viewer.getScoreboard().registerNewObjective("showhealth", "health");
            }
            objective.setDisplaySlot(DisplaySlot.BELOW_NAME);
            objective.displayName(Component.text(CC.color("&c") + "❤"));
            objective.getScore(player.getName()).setScore((int) Math.floor(player.getHealth() / 2));
        }
    }

    public void removeEntities() {
        for (Entity entity : entities) {
            if (entity != null) {
                entity.remove();
            }
        }
    }

    public void hidePlayer(Participant targetParticipant) {
        Player targetPlayer = Bukkit.getPlayer(targetParticipant.getPlayerUUID());
        if (targetPlayer == null) return;
        for (Participant participant : participants) {
            Player player = Bukkit.getPlayer(participant.getPlayerUUID());
            if (player == null) return;
            player.hidePlayer(Neptune.get(), targetPlayer);
        }
    }

    public void showPlayer(Participant targetParticipant) {
        Player targetPlayer = Bukkit.getPlayer(targetParticipant.getPlayerUUID());
        if (targetPlayer == null) return;
        for (Participant participant : participants) {
            Player player = Bukkit.getPlayer(participant.getPlayerUUID());
            if (player == null) return;
            player.showPlayer(Neptune.get(), targetPlayer);
        }
    }

    public abstract void end();

    public abstract void onDeath(Participant participant);
}
