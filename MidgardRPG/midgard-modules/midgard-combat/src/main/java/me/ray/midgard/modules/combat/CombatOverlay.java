package me.ray.midgard.modules.combat;

import me.ray.midgard.core.MidgardCore;
import me.ray.midgard.core.attribute.AttributeInstance;
import me.ray.midgard.core.attribute.CoreAttributeData;
import me.ray.midgard.core.profile.MidgardProfile;
import me.ray.midgard.core.text.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Gerenciador de Overlay (Action Bar) de combate.
 * <p>
 * Exibe informações vitais (Vida, Mana, Stamina) na Action Bar do jogador em tempo real.
 */
public class CombatOverlay implements Runnable {

    private final JavaPlugin plugin;

    /**
     * Construtor do CombatOverlay.
     *
     * @param plugin Instância do plugin principal.
     */
    public CombatOverlay(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Inicia a tarefa de atualização da Action Bar.
     * Executa a cada 5 ticks (0.25 segundos).
     */
    public void start() {
        Bukkit.getScheduler().runTaskTimer(plugin, this, 0L, 5L); // Atualiza a cada 5 ticks
    }

    /**
     * Executado periodicamente para atualizar a Action Bar de todos os jogadores online.
     * Recupera os dados de combate e atributos para formatar a mensagem.
     */
    @Override
    public void run() {
        try {
            for (Player player : Bukkit.getOnlinePlayers()) {
                // Check for combo override (from SpellsModule) or Casting Mode
                if (player.hasMetadata("midgard_combo_active") || player.hasMetadata("midgard_casting_mode")) {
                    continue;
                }
                
                MidgardProfile profile = MidgardCore.getProfileManager().getProfile(player.getUniqueId());
                if (profile == null) continue;

                CombatData combatData = profile.getData(CombatData.class);
                CoreAttributeData attributeData = profile.getData(CoreAttributeData.class);
                
                // Se os dados estiverem faltando, não podemos exibir mana corretamente, mas podemos exibir vida
                if (combatData == null) {
                    // Tenta inicializar se estiver faltando? Ou apenas pula mana
                    combatData = profile.getOrCreateData(CombatData.class);
                    profile.setData(combatData);
                }
                
                if (attributeData == null) {
                    attributeData = profile.getOrCreateData(CoreAttributeData.class);
                    profile.setData(attributeData);
                }

                double currentHealth = combatData.getCurrentHealth();
                AttributeInstance maxHealthAttr = attributeData.getInstance(CombatAttributes.MAX_HEALTH);
                double maxHealth = maxHealthAttr != null ? maxHealthAttr.getValue() : 100;
                
                double currentMana = combatData.getCurrentMana();
                AttributeInstance maxManaAttr = attributeData.getInstance(CombatAttributes.MAX_MANA);
                double maxMana = maxManaAttr != null ? maxManaAttr.getValue() : 100;

                double currentStamina = combatData.getCurrentStamina();
                AttributeInstance maxStaminaAttr = attributeData.getInstance(CombatAttributes.MAX_STAMINA);
                double maxStamina = maxStaminaAttr != null ? maxStaminaAttr.getValue() : 100;

                String bar = String.format("<red>❤ %.0f/%.0f   <blue>⚡ %.0f/%.0f   <yellow>⚡ %.0f/%.0f", 
                    currentHealth, maxHealth, currentMana, maxMana, currentStamina, maxStamina);
                
                MessageUtils.sendActionBar(player, bar);
            }
        } catch (Exception e) {
            // Logar apenas uma vez por minuto para evitar spam no console se algo estiver permanentemente quebrado
            // Por enquanto, apenas log padrão
            plugin.getLogger().warning("Erro no CombatOverlay: " + e.getMessage());
        }
    }
}
