package me.ray.midgard.modules.combat.task;

import me.ray.midgard.core.MidgardCore;
import me.ray.midgard.core.attribute.AttributeInstance;
import me.ray.midgard.core.attribute.AttributeModifier;
import me.ray.midgard.core.attribute.AttributeOperation;
import me.ray.midgard.core.attribute.CoreAttributeData;
import me.ray.midgard.core.profile.MidgardProfile;
import me.ray.midgard.modules.combat.CombatAttributes;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class StatScalingTask implements Runnable {

    @Override
    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            MidgardProfile profile = MidgardCore.getProfileManager().getProfile(player.getUniqueId());
            if (profile == null) continue;

            CoreAttributeData attributeData = profile.getOrCreateData(CoreAttributeData.class);

            // --- Fase 5: Escalonamento de Atributos (Primário -> Secundário) ---
            // Força: +0.5 Dano Físico, +1.0 Dano de Terra
            // Inteligência: +1.0 Mana, +0.1 Regen Mana, +1.0 Dano Mágico, +1.0 Dano de Água
            // Destreza: +0.2 Chance Crítica, +1.0 Dano de Trovão
            // Defesa: +1.0 Dano de Fogo
            // Agilidade: +0.1% Velocidade, +0.2% Esquiva, +1.0 Dano de Ar

            AttributeInstance strAttr = attributeData.getInstance(CombatAttributes.STRENGTH);
            AttributeInstance intAttr = attributeData.getInstance(CombatAttributes.INTELLIGENCE);
            AttributeInstance dexAttr = attributeData.getInstance(CombatAttributes.DEXTERITY);
            AttributeInstance defAttr = attributeData.getInstance(CombatAttributes.DEFENSE);
            AttributeInstance agiAttr = attributeData.getInstance(CombatAttributes.AGILITY);

            double strength = strAttr != null ? strAttr.getValue() : 0;
            double intelligence = intAttr != null ? intAttr.getValue() : 0;
            double dexterity = dexAttr != null ? dexAttr.getValue() : 0;
            double defense = defAttr != null ? defAttr.getValue() : 0;
            double agility = agiAttr != null ? agiAttr.getValue() : 0;

            // --- FORÇA ---
            // Dano Físico
            AttributeInstance physDmgAttr = attributeData.getInstance(CombatAttributes.PHYSICAL_DAMAGE);
            if (physDmgAttr != null) {
                physDmgAttr.removeModifier("StatScaling_STR");
                if (strength > 0) {
                    physDmgAttr.addModifier(new AttributeModifier("StatScaling_STR", strength * 0.5, AttributeOperation.ADD_NUMBER));
                }
            }
            // Dano de Terra
            AttributeInstance earthDmgAttr = attributeData.getInstance(CombatAttributes.EARTH_DAMAGE);
            if (earthDmgAttr != null) {
                earthDmgAttr.removeModifier("StatScaling_STR");
                if (strength > 0) {
                    earthDmgAttr.addModifier(new AttributeModifier("StatScaling_STR", strength * 1.0, AttributeOperation.ADD_NUMBER));
                }
            }

            // --- INTELIGÊNCIA ---
            // Dano Mágico
            AttributeInstance magicDmgAttr = attributeData.getInstance(CombatAttributes.MAGIC_DAMAGE);
            if (magicDmgAttr != null) {
                magicDmgAttr.removeModifier("StatScaling_INT");
                if (intelligence > 0) {
                    magicDmgAttr.addModifier(new AttributeModifier("StatScaling_INT", intelligence * 1.0, AttributeOperation.ADD_NUMBER));
                }
            }
            // Mana Máxima
            AttributeInstance maxManaAttr = attributeData.getInstance(CombatAttributes.MAX_MANA);
            if (maxManaAttr != null) {
                maxManaAttr.removeModifier("StatScaling_INT");
                if (intelligence > 0) {
                    maxManaAttr.addModifier(new AttributeModifier("StatScaling_INT", intelligence * 1.0, AttributeOperation.ADD_NUMBER));
                }
            }
            // Regen Mana
            AttributeInstance manaRegenAttr = attributeData.getInstance(CombatAttributes.MANA_REGEN);
            if (manaRegenAttr != null) {
                manaRegenAttr.removeModifier("StatScaling_INT");
                if (intelligence > 0) {
                    manaRegenAttr.addModifier(new AttributeModifier("StatScaling_INT", intelligence * 0.1, AttributeOperation.ADD_NUMBER));
                }
            }
            // Dano de Água
            AttributeInstance waterDmgAttr = attributeData.getInstance(CombatAttributes.WATER_DAMAGE);
            if (waterDmgAttr != null) {
                waterDmgAttr.removeModifier("StatScaling_INT");
                if (intelligence > 0) {
                    waterDmgAttr.addModifier(new AttributeModifier("StatScaling_INT", intelligence * 1.0, AttributeOperation.ADD_NUMBER));
                }
            }

            // --- DESTREZA ---
            // Chance Crítica
            AttributeInstance critAttr = attributeData.getInstance(CombatAttributes.CRITICAL_CHANCE);
            if (critAttr != null) {
                critAttr.removeModifier("StatScaling_DEX");
                if (dexterity > 0) {
                    critAttr.addModifier(new AttributeModifier("StatScaling_DEX", dexterity * 0.2, AttributeOperation.ADD_NUMBER));
                }
            }
            // Dano de Trovão
            AttributeInstance thunderDmgAttr = attributeData.getInstance(CombatAttributes.THUNDER_DAMAGE);
            if (thunderDmgAttr != null) {
                thunderDmgAttr.removeModifier("StatScaling_DEX");
                if (dexterity > 0) {
                    thunderDmgAttr.addModifier(new AttributeModifier("StatScaling_DEX", dexterity * 1.0, AttributeOperation.ADD_NUMBER));
                }
            }

            // --- DEFESA ---
            // Dano de Fogo
            AttributeInstance fireDmgAttr = attributeData.getInstance(CombatAttributes.FIRE_DAMAGE);
            if (fireDmgAttr != null) {
                fireDmgAttr.removeModifier("StatScaling_DEF");
                if (defense > 0) {
                    fireDmgAttr.addModifier(new AttributeModifier("StatScaling_DEF", defense * 1.0, AttributeOperation.ADD_NUMBER));
                }
            }

            // --- AGILIDADE ---
            // Velocidade
            AttributeInstance speedAttr = attributeData.getInstance(CombatAttributes.SPEED);
            if (speedAttr != null) {
                speedAttr.removeModifier("StatScaling_AGI");
                if (agility > 0) {
                    speedAttr.addModifier(new AttributeModifier("StatScaling_AGI", agility * 0.1, AttributeOperation.ADD_NUMBER));
                }
            }
            // Esquiva
            AttributeInstance dodgeAttr = attributeData.getInstance(CombatAttributes.DODGE_RATING); // ou DODGE_CHANCE se houver
            if (dodgeAttr != null) {
                dodgeAttr.removeModifier("StatScaling_AGI");
                if (agility > 0) {
                    dodgeAttr.addModifier(new AttributeModifier("StatScaling_AGI", agility * 0.2, AttributeOperation.ADD_NUMBER));
                }
            }
            // Dano de Ar
            AttributeInstance airDmgAttr = attributeData.getInstance(CombatAttributes.AIR_DAMAGE);
            if (airDmgAttr != null) {
                airDmgAttr.removeModifier("StatScaling_AGI");
                if (agility > 0) {
                    airDmgAttr.addModifier(new AttributeModifier("StatScaling_AGI", agility * 1.0, AttributeOperation.ADD_NUMBER));
                }
            }

            // Atualização de Velocidade
            speedAttr = attributeData.getInstance(CombatAttributes.SPEED);
            if (speedAttr != null) {
                double speedVal = speedAttr.getValue();
                float defaultSpeed = 0.2f;
                float newSpeed = defaultSpeed * (float)(speedVal / 100.0);
                newSpeed = Math.min(1.0f, Math.max(0.0f, newSpeed)); // Limitar (Clamp)
                if (player.getWalkSpeed() != newSpeed) {
                    player.setWalkSpeed(newSpeed);
                }
            }
        }
    }
}
