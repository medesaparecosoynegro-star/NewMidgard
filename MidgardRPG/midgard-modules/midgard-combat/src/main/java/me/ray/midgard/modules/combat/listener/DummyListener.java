package me.ray.midgard.modules.combat.listener;

import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;

public class DummyListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof LivingEntity)) return;
        LivingEntity entity = (LivingEntity) event.getEntity();

        if (entity.getScoreboardTags().contains("midgard_dummy")) {
            // If damage would kill the dummy, prevent it but simulate impact
            if (entity.getHealth() - event.getFinalDamage() <= 0) {
                event.setDamage(0);
                entity.setHealth(entity.getAttribute(Attribute.MAX_HEALTH).getValue());
            }
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onDamageMonitor(EntityDamageEvent event) {
        if (event.isCancelled()) return;
        if (!(event.getEntity() instanceof LivingEntity)) return;
        LivingEntity entity = (LivingEntity) event.getEntity();

        if (entity.getScoreboardTags().contains("midgard_dummy")) {
            // Heal back to full after the tick to simulate infinite health
            // We use a scheduler or just set it immediately if it's not dead
            // But setting it immediately might look like no damage was taken on the health bar
            // For a dummy, we want it to stay alive.
            
            double max = entity.getAttribute(Attribute.MAX_HEALTH).getValue();
            if (entity.getHealth() < max) {
                entity.setHealth(max);
            }
        }
    }

    @EventHandler
    public void onDeath(EntityDeathEvent event) {
         if (event.getEntity().getScoreboardTags().contains("midgard_dummy")) {
             event.getDrops().clear();
             event.setDroppedExp(0);
         }
    }
}
