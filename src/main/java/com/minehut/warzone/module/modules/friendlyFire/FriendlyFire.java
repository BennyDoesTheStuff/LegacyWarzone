package com.minehut.warzone.module.modules.friendlyFire;

import com.google.common.base.Optional;
import com.minehut.warzone.match.Match;
import com.minehut.warzone.module.modules.scoreboard.ScoreboardModule;
import com.minehut.warzone.module.modules.team.TeamModule;
import com.minehut.warzone.GameHandler;
import com.minehut.warzone.module.Module;
import com.minehut.warzone.tnt.TntTracker;
import com.minehut.warzone.util.Teams;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.UUID;

public class FriendlyFire implements Module {

    private Match match;
    private boolean arrowReturn;

    protected FriendlyFire(Match match, boolean enabled, boolean arrowReturn) {
        this.match = match;
        this.arrowReturn = arrowReturn;
        if (enabled) {
            for (TeamModule team : Teams.getTeams()) {
                for (ScoreboardModule scoreboard : GameHandler.getGameHandler().getMatch().getModules().getModules(ScoreboardModule.class)) {
                    scoreboard.getSimpleScoreboard().getScoreboard().getTeam(team.getId()).setAllowFriendlyFire(false);
                }
            }
        }
    }

    @EventHandler
    public void onEntityDamageEvent(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Projectile && ((Projectile) event.getDamager()).getShooter() == event.getEntity()){
            event.setCancelled(true);
            return;
        }

        if (event.getEntity() instanceof Player) {
            Player hurt = (Player) event.getEntity();

            if (event.getDamager() instanceof TNTPrimed) {
                UUID uuid = TntTracker.getWhoPlaced(event.getDamager());
                if (uuid != null) {
                    Player attacker = Bukkit.getPlayer(uuid);
                    if (attacker != null) {
                        if (Teams.getTeamByPlayer(hurt).get().contains(attacker)) {
                            event.setCancelled(true);
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPotionSplash(PotionSplashEvent event) {
        boolean proceed = false;
        for (PotionEffect effect : event.getPotion().getEffects()) {
            if (effect.getType().equals(PotionEffectType.POISON) || effect.getType().equals(PotionEffectType.BLINDNESS) ||
                    effect.getType().equals(PotionEffectType.CONFUSION) || effect.getType().equals(PotionEffectType.HARM) ||
                    effect.getType().equals(PotionEffectType.HUNGER) || effect.getType().equals(PotionEffectType.SLOW) ||
                    effect.getType().equals(PotionEffectType.SLOW_DIGGING) || effect.getType().equals(PotionEffectType.WITHER) ||
                    effect.getType().equals(PotionEffectType.WEAKNESS)) {
                proceed = true;
            }
        }
        if (proceed && event.getPotion().getShooter() instanceof Player && Teams.getTeamByPlayer((Player) event.getPotion().getShooter()) != null) {
            Optional<TeamModule> team = Teams.getTeamByPlayer((Player) event.getPotion().getShooter());
            for (LivingEntity affected : event.getAffectedEntities()) {
                if (affected instanceof Player && Teams.getTeamByPlayer((Player) affected) != null && Teams.getTeamByPlayer((Player) affected).equals(team) && !affected.equals(event.getPotion().getShooter())) {
                    event.setIntensity(affected, 0);
                }
            }
        }
    }

    @Override
    public void unload() {
        HandlerList.unregisterAll(this);
    }
}
