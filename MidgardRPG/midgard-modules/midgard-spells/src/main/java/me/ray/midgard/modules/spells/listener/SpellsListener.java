package me.ray.midgard.modules.spells.listener;

import me.ray.midgard.core.text.MessageUtils;
import me.ray.midgard.modules.spells.SpellsModule;
import me.ray.midgard.modules.spells.data.SpellProfile;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.metadata.FixedMetadataValue;

public class SpellsListener implements Listener {

    private final SpellsModule module;
    private final Map<UUID, StringBuilder> comboBuffer = new HashMap<>();
    private final Map<UUID, Integer> comboTasks = new HashMap<>();
    private final Map<UUID, Long> lastClick = new HashMap<>();
    private static final String COMBO_META = "midgard_combo_active";

    public SpellsListener(SpellsModule module) {
        this.module = module;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        comboBuffer.remove(player.getUniqueId());
        comboTasks.remove(player.getUniqueId());
        lastClick.remove(player.getUniqueId());
        player.removeMetadata(COMBO_META, module.getPlugin());
        module.getSpellManager().disableCastingMode(player);
    }
    
    @EventHandler
    public void onSwapHand(PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();
        
        // Removido check de criativo para facilitar testes
        // if (player.getGameMode() == GameMode.CREATIVE) return;
        
        // Removido check de sneaking temporalmente para garantir que o evento dispare
        // if (player.isSneaking()) return; 
        
        event.setCancelled(true);
        module.getSpellManager().toggleCastingMode(player);
    }
    
    @EventHandler
    public void onItemHeld(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        if (module.getSpellManager().isCastingMode(player)) {
            // New logic: Check if target slot has a skill IN VIRTUAL VIEW
            int newSlotIndex = event.getNewSlot(); // 0-8
            
            // Usar o getSkillInVirtualSlot para saber o que tem lá visualmente
            String skillId = module.getSpellManager().getSkillInVirtualSlot(player, newSlotIndex);
            
            if (skillId != null) {
                // Has skill -> Cast and Cancel (Stay in previous slot to allow rapid fire)
                event.setCancelled(true);
                module.getSpellManager().castSpell(player, skillId);
            } else {
                // No skill (Empty/Anchor/Void) -> Allow switch (Move hand to new slot)
                // This becomes the new resting place.
                // NOTE: Se o anchor é fixo na ativação, mover a mão não muda o layout.
                // O layout só muda se o anchor mudar.
                // Pelo requisito "se a skill estiver no slot 1 e ele ativar... ela vai para o slot 2",
                // subentende-se que o anchor é fixo na ativação.
                // Se o player mover a mão para um slot vazio, ele apenas move a mão.
            }
        }
    }

    // --- COMBO SYSTEM ---
    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Action action = event.getAction();
        if (action == Action.PHYSICAL) return;

        Player player = event.getPlayer();
        
        // Verificar se está no modo COMBO antes de processar
        SpellProfile profile = module.getSpellManager().getProfile(player);
        if (profile == null || profile.getCastingStyle() != SpellProfile.CastingStyle.COMBO) {
            return; 
        }
        
        // Debounce para evitar duplo clique (Main Hand + Off Hand)
        long now = System.currentTimeMillis();
        long last = lastClick.getOrDefault(player.getUniqueId(), 0L);
        if (now - last < 100) return; // 100ms debounce
        lastClick.put(player.getUniqueId(), now);

        String clickType = (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK) ? "L" : "R";

        updateCombo(player, clickType);
    }

    private void updateCombo(Player player, String click) {
        UUID uuid = player.getUniqueId();
        
        // Cancel existing clear task
        if (comboTasks.containsKey(uuid)) {
            Bukkit.getScheduler().cancelTask(comboTasks.get(uuid));
        }

        StringBuilder builder = comboBuffer.computeIfAbsent(uuid, k -> new StringBuilder());
        builder.append(click);
        
        // Set metadata to block CombatOverlay
        if (!player.hasMetadata(COMBO_META)) {
            player.setMetadata(COMBO_META, new FixedMetadataValue(module.getPlugin(), true));
        }
        
        // Visual Feedback
        String visualCombo = builder.toString()
                .replace("L", "<yellow>L</yellow>")
                .replace("R", "<btn:red>R</btn:red>"); // Assuming btn tag isn't real, just using simple colors
        
        // Let's use standard naming: L (Left), R (Right)
        String display = builder.toString()
                .replace("L", "<bold><gradient:#3498db:#2980b9>E</gradient></bold>") // Esquerdo
                .replace("R", "<bold><gradient:#e74c3c:#c0392b>D</gradient></bold>"); // Direito
        
        MessageUtils.sendActionBar(player, "<gray>Combo: " + display);
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, click.equals("L") ? 1.5f : 1.0f);

        // Check if valid combo exists
        String currentCombo = builder.toString();
        
        // 1. Check Profile Bindings (Personal overrides)
        SpellProfile profile = module.getSpellManager().getProfile(player);
        String spellId = (profile != null) ? profile.getSpellByCombo(currentCombo) : null;
        
        if (spellId != null) {
            // Found a match!
            boolean casted = module.getSpellManager().castSpell(player, spellId);
            if (casted) {
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 2.0f);
            }
            builder.setLength(0); // Reset after cast attempt
            player.removeMetadata(COMBO_META, module.getPlugin()); // Release lock immediately on success
            MessageUtils.sendActionBar(player, ""); // Clear action bar
        } else {
            // No exact match yet, but limit length
            if (currentCombo.length() >= 3) { // Max combo size 3
                builder.setLength(0);
                MessageUtils.sendActionBar(player, "<red>Combo Falhou");
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
                
                // Clear metadata after short delay to show "Failed" message? 
                // Actually better to clear immediately or after a tiny delay so overlay comes back
                Bukkit.getScheduler().runTaskLater(module.getPlugin(), () -> {
                     player.removeMetadata(COMBO_META, module.getPlugin());
                }, 20L); // Show failure for 1s
                
            } else {
                // Schedule clear
                int taskId = Bukkit.getScheduler().scheduleSyncDelayedTask(module.getPlugin(), () -> {
                    comboBuffer.remove(uuid);
                    player.removeMetadata(COMBO_META, module.getPlugin()); // Release lock on timeout
                    MessageUtils.sendActionBar(player, "");
                }, 30L); // 1.5 seconds to finish combo
                comboTasks.put(uuid, taskId);
            }
        }
    }

}
