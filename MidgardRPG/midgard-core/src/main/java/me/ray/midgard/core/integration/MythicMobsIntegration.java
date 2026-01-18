package me.ray.midgard.core.integration;

import io.lumine.mythic.api.skills.placeholders.PlaceholderString;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.mobs.ActiveMob;
import me.ray.midgard.core.attribute.Attribute;
import me.ray.midgard.core.attribute.AttributeRegistry;
import org.bukkit.entity.LivingEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Integração completa com MythicMobs via API.
 * <p>
 * Permite que Mobs tenham atributos do MidgardRPG definidos via:
 * 1. Variáveis do MythicMobs (Dinâmico - permite buffs/debuffs em tempo real).
 *    Ex: setvariable{var=midgard.attr.strength;val=150}
 *    Acesso via placeholder: <mob.var.midgard.attr.strength>
 * 2. Tags (Estático - configuração simples).
 *    Ex: Tags: [midgard.attr.strength.150]
 */
public class MythicMobsIntegration {

    private static final String VAR_PREFIX = "midgard.attr.";
    private static final String TAG_PREFIX = "midgard.attr.";

    public static boolean isMythicMob(LivingEntity entity) {
        return MythicBukkit.inst().getMobManager().isActiveMob(entity.getUniqueId());
    }

    public static Optional<ActiveMob> getActiveMob(LivingEntity entity) {
        return MythicBukkit.inst().getMobManager().getActiveMob(entity.getUniqueId());
    }

    /**
     * Obtém o valor de um atributo de um MythicMob.
     * Prioridade: Variável (Dinâmico) > Tag (Estático).
     */
    public static double getAttributeValue(LivingEntity entity, String attributeId) {
        Optional<ActiveMob> mobOpt = getActiveMob(entity);
        if (mobOpt.isEmpty()) return 0.0;

        ActiveMob mob = mobOpt.get();
        String key = attributeId.toLowerCase();

        // 1. Tentar ler Variável via Placeholder API do MythicMobs
        // Isso garante que pegamos o valor correto independente do escopo interno
        try {
            String placeholder = "<mob.var." + VAR_PREFIX + key + ">";
            String result = PlaceholderString.of(placeholder).get(mob);
            
            // Se o placeholder não for resolvido, ele retorna a própria string ou vazio dependendo da versão
            if (result != null && !result.isEmpty() && !result.contains("<mob.var.")) {
                return Double.parseDouble(result);
            }
        } catch (Exception ignored) {
            // Falha ao parsear ou variável não existe
        }

        // 2. Fallback para Tags (Configuração estática)
        String targetTag = TAG_PREFIX + key + ".";
        for (String tag : entity.getScoreboardTags()) {
            if (tag.toLowerCase().startsWith(targetTag)) {
                try {
                    return Double.parseDouble(tag.substring(targetTag.length()));
                } catch (NumberFormatException ignored) {}
            }
        }

        return 0.0;
    }

    /**
     * Lê todos os atributos configurados no Mob.
     * Itera sobre todos os atributos registrados no Midgard e verifica se o Mob possui algum deles.
     */
    public static Map<String, Double> getAttributes(LivingEntity entity) {
        Map<String, Double> attributes = new HashMap<>();

        Optional<ActiveMob> mobOpt = getActiveMob(entity);
        if (mobOpt.isEmpty()) return attributes;

        ActiveMob mob = mobOpt.get();

        // Itera sobre todos os atributos conhecidos do sistema
        for (Attribute attr : AttributeRegistry.getInstance().getAll()) {
            String id = attr.getId();
            
            // Check Variable
            try {
                String placeholder = "<mob.var." + VAR_PREFIX + id + ">";
                String result = PlaceholderString.of(placeholder).get(mob);
                
                if (result != null && !result.isEmpty() && !result.contains("<mob.var.")) {
                    attributes.put(id, Double.parseDouble(result));
                    continue; // Variável tem prioridade
                }
            } catch (Exception ignored) {}

            // Check Tag (se não achou variável)
            String tagPrefix = TAG_PREFIX + id + ".";
            for (String tag : entity.getScoreboardTags()) {
                if (tag.toLowerCase().startsWith(tagPrefix)) {
                    try {
                        attributes.put(id, Double.parseDouble(tag.substring(tagPrefix.length())));
                    } catch (NumberFormatException ignored) {}
                }
            }
        }

        return attributes;
    }
}
