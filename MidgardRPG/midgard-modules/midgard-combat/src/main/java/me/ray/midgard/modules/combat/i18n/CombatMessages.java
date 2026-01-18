package me.ray.midgard.modules.combat.i18n;

import me.ray.midgard.core.i18n.MessageKey;
import me.ray.midgard.core.i18n.MessageRegistry;

/**
 * Constantes tipadas de mensagens do módulo Combat.
 * <p>
 * Uso:
 * <pre>
 * MessageUtils.send(player, CombatMessages.COMBAT_ENABLED);
 * MessageUtils.send(player, CombatMessages.TIME_REMAINING, 
 *     Placeholder.of("time", "5"));
 * </pre>
 *
 * @since 2.0.0
 */
public final class CombatMessages {
    
    private CombatMessages() {
        // Classe utilitária
    }
    
    // ============================================
    // MODO COMBATE
    // ============================================
    
    public static final MessageKey COMBAT_ENABLED = register(
            MessageKey.builder("combat.combat_mode.enabled")
                    .module("combat")
                    .fallback("<red>⚔ <white>Você entrou em combate!")
                    .build()
    );
    
    public static final MessageKey COMBAT_DISABLED = register(
            MessageKey.builder("combat.combat_mode.disabled")
                    .module("combat")
                    .fallback("<green>✔ <white>Você saiu do modo de combate!")
                    .build()
    );
    
    public static final MessageKey COMBAT_WARNING_5S = register(
            MessageKey.builder("combat.combat_mode.warning_5s")
                    .module("combat")
                    .fallback("<yellow>⚠ <white>Combate termina em <yellow>5 <white>segundos!")
                    .build()
    );
    
    public static final MessageKey COMBAT_WARNING_3S = register(
            MessageKey.builder("combat.combat_mode.warning_3s")
                    .module("combat")
                    .fallback("<yellow>⚠ <white>Combate termina em <yellow>3 <white>segundos!")
                    .build()
    );
    
    public static final MessageKey COMBAT_EXPIRED = register(
            MessageKey.builder("combat.combat_mode.expired")
                    .module("combat")
                    .fallback("<green>✔ <white>Combate expirado!")
                    .build()
    );
    
    public static final MessageKey CANNOT_LOGOUT = register(
            MessageKey.builder("combat.combat_mode.cannot_logout")
                    .module("combat")
                    .fallback("<red>✖ <white>Você não pode deslogar durante o combate!")
                    .build()
    );
    
    public static final MessageKey CANNOT_TELEPORT = register(
            MessageKey.builder("combat.combat_mode.cannot_teleport")
                    .module("combat")
                    .fallback("<red>✖ <white>Você não pode teleportar durante o combate!")
                    .build()
    );
    
    public static final MessageKey CANNOT_USE_COMMAND = register(
            MessageKey.builder("combat.combat_mode.cannot_use_command")
                    .module("combat")
                    .fallback("<red>✖ <white>Este comando está bloqueado durante o combate!")
                    .build()
    );
    
    public static final MessageKey TIME_REMAINING = register(
            MessageKey.builder("combat.combat_mode.time_remaining")
                    .module("combat")
                    .placeholders("time")
                    .fallback("<yellow>⚔ <gray>Tempo restante: <white>%time%s")
                    .build()
    );
    
    public static final MessageKey OPPONENT = register(
            MessageKey.builder("combat.combat_mode.opponent")
                    .module("combat")
                    .placeholders("target")
                    .fallback("<red>⚔ <gray>Em combate com: <white>%target%")
                    .build()
    );
    
    // ============================================
    // DANO - Removido (feedback via hologramas)
    // ============================================
    // Mensagens de dano no chat foram removidas.
    // O feedback de dano é exibido via damage indicators (hologramas).
    
    // ============================================
    // ELEMENTAL
    // ============================================
    
    public static final MessageKey ELEMENTAL_FIRE = register(
            MessageKey.builder("combat.elemental.fire")
                    .module("combat")
                    .fallback("<red>🔥 Fogo")
                    .build()
    );
    
    public static final MessageKey ELEMENTAL_WATER = register(
            MessageKey.builder("combat.elemental.water")
                    .module("combat")
                    .fallback("<blue>💧 Água")
                    .build()
    );
    
    public static final MessageKey ELEMENTAL_EARTH = register(
            MessageKey.builder("combat.elemental.earth")
                    .module("combat")
                    .fallback("<dark_green>🌍 Terra")
                    .build()
    );
    
    public static final MessageKey ELEMENTAL_AIR = register(
            MessageKey.builder("combat.elemental.air")
                    .module("combat")
                    .fallback("<white>💨 Ar")
                    .build()
    );
    
    public static final MessageKey ELEMENTAL_ICE = register(
            MessageKey.builder("combat.elemental.ice")
                    .module("combat")
                    .fallback("<aqua>❄ Gelo")
                    .build()
    );
    
    public static final MessageKey ELEMENTAL_THUNDER = register(
            MessageKey.builder("combat.elemental.thunder")
                    .module("combat")
                    .fallback("<yellow>⚡ Trovão")
                    .build()
    );
    
    public static final MessageKey EFFECT_BURNING = register(
            MessageKey.builder("combat.elemental.burning")
                    .module("combat")
                    .placeholders("damage")
                    .fallback("<red>🔥 <white>Você está queimando! <red>-%damage% HP")
                    .build()
    );
    
    public static final MessageKey EFFECT_FROZEN = register(
            MessageKey.builder("combat.elemental.frozen")
                    .module("combat")
                    .fallback("<aqua>❄ <white>Você está congelado!")
                    .build()
    );
    
    public static final MessageKey EFFECT_SHOCKED = register(
            MessageKey.builder("combat.elemental.shocked")
                    .module("combat")
                    .placeholders("damage")
                    .fallback("<yellow>⚡ <white>Você levou um choque! <red>-%damage% HP")
                    .build()
    );
    
    public static final MessageKey EFFECT_POISONED = register(
            MessageKey.builder("combat.elemental.poisoned")
                    .module("combat")
                    .placeholders("damage")
                    .fallback("<dark_green>☠ <white>Você está envenenado! <red>-%damage% HP")
                    .build()
    );
    
    // ============================================
    // ARMAS
    // ============================================
    
    public static final MessageKey WEAPON_EQUIPPED = register(
            MessageKey.builder("combat.weapons.equipped")
                    .module("combat")
                    .placeholders("weapon")
                    .fallback("<green>⚔ <white>Arma equipada: <yellow>%weapon%")
                    .build()
    );
    
    public static final MessageKey WEAPON_UNEQUIPPED = register(
            MessageKey.builder("combat.weapons.unequipped")
                    .module("combat")
                    .placeholders("weapon")
                    .fallback("<gray>⚔ <white>Arma desequipada: <yellow>%weapon%")
                    .build()
    );
    
    public static final MessageKey WEAPON_DURABILITY_LOW = register(
            MessageKey.builder("combat.weapons.durability_low")
                    .module("combat")
                    .placeholders("weapon", "durability")
                    .fallback("<yellow>⚠ <white>Durabilidade baixa: <yellow>%weapon% <gray>(%durability%%)")
                    .build()
    );
    
    public static final MessageKey WEAPON_BROKEN = register(
            MessageKey.builder("combat.weapons.durability_broken")
                    .module("combat")
                    .placeholders("weapon")
                    .fallback("<red>✖ <white>Sua arma <yellow>%weapon% <white>quebrou!")
                    .build()
    );
    
    public static final MessageKey WEAPON_REPAIRED = register(
            MessageKey.builder("combat.weapons.durability_repaired")
                    .module("combat")
                    .placeholders("weapon")
                    .fallback("<green>✔ <white>Arma reparada: <yellow>%weapon%")
                    .build()
    );
    
    public static final MessageKey WEAPON_SKILL_ACTIVATED = register(
            MessageKey.builder("combat.weapons.skill_activated")
                    .module("combat")
                    .placeholders("skill")
                    .fallback("<light_purple>★ <white>Habilidade ativada: <light_purple>%skill%")
                    .build()
    );
    
    public static final MessageKey WEAPON_SKILL_COOLDOWN = register(
            MessageKey.builder("combat.weapons.skill_cooldown")
                    .module("combat")
                    .placeholders("time")
                    .fallback("<red>✖ <white>Habilidade em recarga! <gray>(%time%s)")
                    .build()
    );
    
    // ============================================
    // MÉTODO AUXILIAR
    // ============================================
    
    /**
     * Registra uma MessageKey no registry global.
     * Chamado automaticamente para cada constante.
     */
    private static MessageKey register(MessageKey key) {
        return MessageRegistry.getInstance().register(key);
    }
    
    /**
     * Inicializa todas as constantes (força o carregamento da classe).
     * Deve ser chamado no onEnable do módulo.
     */
    public static void init() {
        // Força o carregamento de todas as constantes static
        // O simples ato de referenciar a classe já carrega tudo
    }
}
