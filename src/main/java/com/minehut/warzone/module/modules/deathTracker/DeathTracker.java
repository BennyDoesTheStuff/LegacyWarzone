package com.minehut.warzone.module.modules.deathTracker;

import com.minehut.warzone.event.CardinalDeathEvent;
import com.minehut.warzone.module.modules.tracker.DamageTracker;
import com.minehut.warzone.module.modules.tracker.Type;
import com.minehut.warzone.module.modules.tracker.event.TrackerDamageEvent;
import com.minehut.warzone.GameHandler;
import com.minehut.warzone.module.Module;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.PlayerDeathEvent;

public class DeathTracker implements Module {

    protected DeathTracker() {
    }

    @Override
    public void unload() {
        HandlerList.unregisterAll(this);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (!event.getEntity().hasMetadata("teamChange")) {
            Player killer = null;
            TrackerDamageEvent tracker = DamageTracker.getEvent(event.getEntity());
            boolean time = tracker != null && System.currentTimeMillis() - tracker.getTime() <= 7500;
            if (tracker != null && (tracker.getType().equals(Type.KNOCKED) || tracker.getType().equals(Type.SHOT)) && event.getEntity().getKiller() != null && event.getEntity().getKiller().equals(tracker.getDamager())) {
                killer = tracker.getDamager().getPlayer();
            } else if (time) {
                killer = tracker.getDamager().getPlayer();
            }
            CardinalDeathEvent deathEvent = new CardinalDeathEvent(event.getEntity(), killer);
            if (time && DamageTracker.getEvent(event.getEntity()).getDamager().getPlayer() != null) {
                deathEvent.setTrackerDamageEvent(tracker);
            }
            Bukkit.getServer().getPluginManager().callEvent(deathEvent);
        } else {
            event.getEntity().removeMetadata("teamChange", GameHandler.getGameHandler().getPlugin());
        }
        event.setDeathMessage(null);
    }
}