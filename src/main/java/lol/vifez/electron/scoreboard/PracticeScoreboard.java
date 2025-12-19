package lol.vifez.electron.scoreboard;

import lol.vifez.electron.Practice;
import lol.vifez.electron.util.assemble.AssembleAdapter;
import lol.vifez.electron.kit.Kit;
import lol.vifez.electron.kit.enums.KitType;
import lol.vifez.electron.match.Match;
import lol.vifez.electron.elo.EloUtil;
import lol.vifez.electron.match.enums.MatchState;
import lol.vifez.electron.profile.Profile;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/* 
 * Electron © Vifez
 * Developed by Vifez
 * Copyright (c) 2025 Vifez. All rights reserved.
*/

public class PracticeScoreboard implements AssembleAdapter {

    private final AtomicInteger titleIndex = new AtomicInteger(0);
    private final ScoreboardConfig scoreboardConfig;
    private final AnimationManager animationManager;

    public PracticeScoreboard() {
        this.scoreboardConfig = Practice.getInstance().getScoreboardConfig();
        this.animationManager = new AnimationManager();
    }

    @Override
    public String getTitle(Player player) {
        Profile profile = Practice.getInstance().getProfileManager().getProfile(player.getUniqueId());
        if (!profile.isScoreboardEnabled() || !scoreboardConfig.getBoolean("SCOREBOARD.ENABLED")) return "";
        String title = scoreboardConfig.getString("SCOREBOARD.TITLE");
        return title == null ? "" : title.replace("%animation%", animationManager.getCurrentFrame());
    }

    @Override
    public List<String> getLines(Player player) {
        List<String> lines = new ArrayList<>();
        Practice plugin = Practice.getInstance();
        Profile profile = plugin.getProfileManager().getProfile(player.getUniqueId());

        if (!profile.isScoreboardEnabled() || !scoreboardConfig.getBoolean("SCOREBOARD.ENABLED")) return lines;

        String footer = scoreboardConfig.getString("SCOREBOARD.FOOTER");
        String globalElo = String.valueOf(EloUtil.getGlobalElo(profile));
        String division = profile.getDivision().getPrettyName();

        Match match = plugin.getMatchManager().getMatch(profile.getUuid());
        List<String> template;

        if (match != null) {
            int hits = match.getHitsMap().get(profile.getUuid());
            MatchState state = match.getMatchState();

            if (state == MatchState.STARTED) {
                template = match.getKit().getKitType() == KitType.BOXING
                        ? scoreboardConfig.getStringList("SCOREBOARD.IN-BOXING.LINES")
                        : scoreboardConfig.getStringList("SCOREBOARD.IN-GAME.LINES");
            } else if (state == MatchState.STARTING) {
                template = scoreboardConfig.getStringList("SCOREBOARD.MATCH-STARTING.LINES");
            } else {
                template = scoreboardConfig.getStringList("SCOREBOARD.MATCH-ENDING.LINES");
            }

            for (String str : template) {
                lines.add(str
                        .replace("<ping>", String.valueOf(profile.getPing()))
                        .replace("<opponent-ping>", match.getOpponent(player) != null ? String.valueOf(match.getOpponent(player).getPing()) : "0")
                        .replace("<opponent>", match.getOpponent(player) != null ? match.getOpponent(player).getName() : "None")
                        .replace("<duration>", match.getDuration() != null ? match.getDuration() : "0s")
                        .replace("<difference>", formatHits(hits))
                        .replace("<their-hits>", match.getOpponent(player) != null ? String.valueOf(match.getHitsMap().get(match.getOpponent(player).getUuid())) : "0")
                        .replace("<your-hits>", String.valueOf(hits))
                        .replace("<global-elo>", globalElo)
                        .replace("<division>", division)
                        .replace("%animation%", animationManager.getCurrentFrame())
                        .replace("<footer>", footer)
                        .replace("<starting-c>", String.valueOf(match.getCurrentCountdown()))
                        .replace("<winner>", match.getWinner() != null ? match.getWinner().getName() : "None")
                        .replace("<loser>", getMatchLoser(player, match))
                        .replace("<kit>", "")
                        .replace("<time>", "")
                        .replace("<online>", String.valueOf(Bukkit.getOnlinePlayers().size()))
                        .replace("<in-queue>", String.valueOf(plugin.getQueueManager().getAllQueueSize()))
                        .replace("<playing>", String.valueOf(plugin.getMatchManager().getAllMatchSize()))
                        .replace("<username>", player.getName())
                );
            }

        } else if (plugin.getQueueManager().getQueue(profile.getUuid()) != null) {
            template = scoreboardConfig.getStringList("SCOREBOARD.IN-QUEUE.LINES");
            Kit queueKit = plugin.getQueueManager().getQueue(profile.getUuid()).getKit();
            boolean isRanked = plugin.getQueueManager().getQueue(profile.getUuid()).isRanked();
            String typeTag = isRanked ? "&c[R]" : "&7[UR]";

            for (String str : template) {
                lines.add(str
                        .replace("<kit>", queueKit.getName() + " " + typeTag)
                        .replace("<time>", plugin.getQueueManager().getQueue(profile.getUuid()).getQueueTime(profile.getUuid()))
                        .replace("<online>", String.valueOf(Bukkit.getOnlinePlayers().size()))
                        .replace("<in-queue>", String.valueOf(plugin.getQueueManager().getAllQueueSize()))
                        .replace("<playing>", String.valueOf(plugin.getMatchManager().getAllMatchSize()))
                        .replace("<username>", player.getName())
                        .replace("<global-elo>", globalElo)
                        .replace("<division>", division)
                        .replace("%animation%", animationManager.getCurrentFrame())
                        .replace("<footer>", footer)
                );
            }

        } else {
            template = scoreboardConfig.getStringList("SCOREBOARD.IN-LOBBY.LINES");
            for (String str : template) {
                lines.add(str
                        .replace("<online>", String.valueOf(Bukkit.getOnlinePlayers().size()))
                        .replace("<in-queue>", String.valueOf(plugin.getQueueManager().getAllQueueSize()))
                        .replace("<playing>", String.valueOf(plugin.getMatchManager().getAllMatchSize()))
                        .replace("<ping>", String.valueOf(profile.getPing()))
                        .replace("<username>", player.getName())
                        .replace("<global-elo>", globalElo)
                        .replace("<division>", division)
                        .replace("%animation%", animationManager.getCurrentFrame())
                        .replace("<footer>", footer)
                );
            }
        }

        return lines;
    }
    private String formatHits(int hits) {
        if (hits < 0) return "&c" + hits;
        if (hits == 0) return "&e" + hits;
        return "&a" + hits;
    }

    private String getMatchLoser(Player player, Match match) {
        if (match.getWinner() == null) return player.getName() + " " + match.getOpponent(player).getName();
        return match.getOpponent(match.getWinner().getPlayer()).getName();
    }
}