package me.ray.midgard.modules.combat.mechanics;

import me.ray.midgard.core.attribute.CoreAttributeData;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageEvent;

public interface CombatMechanic {
    /**
     * Tenta aplicar a mecânica de combate.
     *
     * @param event O evento de dano.
     * @param victim A vítima do dano.
     * @param victimAttributes Os atributos da vítima.
     * @return true se a mecânica foi ativada e deve interromper o processamento (ex: esquiva total), false caso contrário.
     */
    boolean apply(EntityDamageEvent event, LivingEntity victim, CoreAttributeData victimAttributes);
}
