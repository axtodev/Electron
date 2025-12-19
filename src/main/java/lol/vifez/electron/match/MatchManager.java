package lol.vifez.electron.match;

import lol.vifez.electron.Practice;
import lol.vifez.electron.elo.EloUtil;
import lol.vifez.electron.kit.Kit;
import lol.vifez.electron.match.enums.MatchState;
import lol.vifez.electron.match.event.MatchEndEvent;
import lol.vifez.electron.match.event.MatchStartEvent;
import lol.vifez.electron.profile.Profile;
import lol.vifez.electron.util.CC;
import lol.vifez.electron.hotbar.Hotbar;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/*
 * Electron © Vifez
 * Developed by Vifez
 * Copyright (c) 2025 Vifez. All rights reserved.
 */

public class MatchManager {

    private final Map<UUID, Match> matches = new ConcurrentHashMap<>();

    public Match getMatch(UUID uuid) {
        return matches.get(uuid);
    }

    public Map<UUID, Match> getMatches() {
        return matches;
    }

    public void add(Match match) {
        matches.put(match.getPlayerOne().getUuid(), match);
        matches.put(match.getPlayerTwo().getUuid(), match);
    }

    public void remove(Match match) {
        match.getArena().setBusy(false);
        matches.remove(match.getPlayerOne().getUuid());
        matches.remove(match.getPlayerTwo().getUuid());
    }

    public int getTotalPlayersInMatches() {
        return matches.values().stream().mapToInt(m -> m.getMatchState() == MatchState.STARTED ? 2 : 0).sum();
    }

    public int getPlayersInKitMatches(Kit kit) {
        return matches.values().stream()
                .filter(match -> match.getKit().equals(kit))
                .filter(match -> match.getMatchState() == MatchState.STARTED)
                .mapToInt(match -> 2)
                .sum();
    }

    public void start(Match match) {
        match.setMatchState(MatchState.STARTING);
        match.getArena().setBusy(true);

        match.teleportAndSetup(match.getPlayerOne(), true);
        match.teleportAndSetup(match.getPlayerTwo(), false);

        Bukkit.getPluginManager().callEvent(new MatchStartEvent(match.getPlayerOne(), match.getPlayerTwo(), match));
    }

    public void end(Match match) {
        match.setMatchState(MatchState.ENDING);

        Profile winner = match.getWinner();
        Profile loser = winner == null ? null : match.getOpponent(winner);

        if (winner != null && match.isRanked()) updateEloForRankedMatch(winner, loser, match.getKit());

        Profile[] profiles = winner == null ? new Profile[]{match.getPlayerOne(), match.getPlayerTwo()} : new Profile[]{winner, loser};

        for (Profile profile : profiles) {
            Player player = profile.getPlayer();
            CC.sendMessage(player, winner == null ? "&cMatch has ended!" : "&aMatch finished!");
            player.playSound(player.getLocation(), Sound.NOTE_PLING, 0.5f, 0.5f);
            resetPlayerAfterMatch(player);
        }

        Bukkit.getPluginManager().callEvent(new MatchEndEvent(match.getPlayerOne(), match.getPlayerTwo(), match));
        remove(match);
    }

    private void resetPlayerAfterMatch(Player player) {
        player.getInventory().setContents(Hotbar.getSpawnItems());
        player.getInventory().setArmorContents(null);
        player.teleport(Practice.getInstance().getSpawnLocation());
        player.getActivePotionEffects().forEach(effect -> player.removePotionEffect(effect.getType()));
    }

    private void updateEloForRankedMatch(Profile winner, Profile loser, Kit kit) {
        int winnerElo = winner.getElo(kit);
        int loserElo = loser.getElo(kit);

        int newWinnerElo = EloUtil.getNewRating(winnerElo, loserElo, true);
        int newLoserElo = EloUtil.getNewRating(loserElo, winnerElo, false);

        winner.setElo(kit, newWinnerElo);
        loser.setElo(kit, newLoserElo);

        winner.checkDivision(kit);
        loser.checkDivision(kit);

        CC.sendMessage(winner.getPlayer(), "&aYou won! &7ELO: " + (newWinnerElo - winnerElo >= 0 ? "&a+" : "&c") + (newWinnerElo - winnerElo));
        CC.sendMessage(loser.getPlayer(), "&cYou lost! &7ELO: " + (newLoserElo - loserElo >= 0 ? "&a+" : "&c") + (newLoserElo - loserElo));
    }
}