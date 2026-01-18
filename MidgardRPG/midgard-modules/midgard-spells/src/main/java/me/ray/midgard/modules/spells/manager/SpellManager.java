package me.ray.midgard.modules.spells.manager;

import io.lumine.mythic.api.MythicProvider;
import io.lumine.mythic.api.skills.Skill;
import io.lumine.mythic.bukkit.MythicBukkit;
import me.ray.midgard.core.debug.MidgardLogger;
import me.ray.midgard.core.profile.MidgardProfile;
import me.ray.midgard.core.MidgardCore;
import me.ray.midgard.modules.spells.SpellsModule;
import me.ray.midgard.modules.spells.data.SpellProfile;
import me.ray.midgard.modules.spells.obj.Spell;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.*;

import org.bukkit.configuration.ConfigurationSection;

public class SpellManager {

    private final SpellsModule module;
    private final Map<String, Spell> loadedSpells = new HashMap<>();
    private final Set<UUID> castingModePlayers = new HashSet<>();

    private final Map<UUID, Integer> castingAnchors = new HashMap<>();

    private final Map<Integer, String> defaultCombos = new HashMap<>();

    public SpellManager(SpellsModule module) {
        this.module = module;
        loadDefaultCombos();
    }

    private void loadDefaultCombos() {
        // Defaults if config is missing (Plain L/R without hyphens to match Listener)
        defaultCombos.put(1, "LLL");
        defaultCombos.put(2, "RRR");
        defaultCombos.put(3, "LRL");
        defaultCombos.put(4, "RLR");
        defaultCombos.put(5, "LLR");
        defaultCombos.put(6, "RRL");
        
        // Load from module config if available
        if (module.getConfig() != null) {
            ConfigurationSection section = module.getConfig().getConfigurationSection("combos");
            if (section != null) {
                for (String key : section.getKeys(false)) {
                    try {
                        int slot = Integer.parseInt(key);
                        String seq = section.getString(key);
                        if (seq != null) {
                            defaultCombos.put(slot, seq.toUpperCase());
                        }
                    } catch (NumberFormatException ignored) {}
                }
            }
        }
    }
    
    public String getDefaultCombo(int slot) {
        return defaultCombos.getOrDefault(slot, "Undefined");
    }

    public SpellsModule getModule() {
        return module;
    }

    public void toggleCastingMode(Player player) {
        if (castingModePlayers.contains(player.getUniqueId())) {
            disableCastingMode(player);
        } else {
            enableCastingMode(player);
        }
    }

    @SuppressWarnings("deprecation")
    public void enableCastingMode(Player player) {
        castingModePlayers.add(player.getUniqueId());
        
        // Define Anchor Slot (Current Slot becomes the "Void")
        // Everything to the right of this slot is shifted +1 visuals
        int anchor = player.getInventory().getHeldItemSlot(); 
        castingAnchors.put(player.getUniqueId(), anchor);
        
        player.setMetadata("midgard_casting_mode", new org.bukkit.metadata.FixedMetadataValue(module.getPlugin(), true));
        player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_IRON_TRAPDOOR_OPEN, 1f, 1.5f);
        
        String enableMsg = module.getMessage("casting.mode_enabled");
        me.ray.midgard.core.text.MessageUtils.send(player, enableMsg);
    }

    public void disableCastingMode(Player player) {
        castingModePlayers.remove(player.getUniqueId());
        castingAnchors.remove(player.getUniqueId()); // Limpa anchor
        player.removeMetadata("midgard_casting_mode", module.getPlugin());
        player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_IRON_TRAPDOOR_CLOSE, 1f, 0.5f);
        me.ray.midgard.core.text.MessageUtils.sendActionBar(player, "");
        
        String disableMsg = module.getMessage("casting.mode_disabled");
        me.ray.midgard.core.text.MessageUtils.send(player, disableMsg);
    }

    public boolean isCastingMode(Player player) {
        return castingModePlayers.contains(player.getUniqueId());
    }
    
    public Set<UUID> getCastingPlayers() {
        return Collections.unmodifiableSet(castingModePlayers);
    }
    
    public void castSkillBar(Player player, int slot) {
        SpellProfile profile = getProfile(player);
        if (profile == null) return;
        
        // Use getVirtualSkillId logic here? Or parameter is already virtual?
        // Parameter 'slot' comes from listener which usually sends raw slot (1-9)
        // But we want to abstract this. Let's create a helper method.
        
        String spellId = profile.getSkillInSlot(slot);
        if (spellId == null) return;
        
        castSpell(player, spellId);
    }
    
    // Retorna o ID da skill que estaria no slot visualizado, considerando o deslocamento do Anchor
    public String getSkillInVirtualSlot(Player player, int visualSlotIndex) {
        // visualSlotIndex: 0-8 (Hotbar index)
        SpellProfile profile = getProfile(player);
        if (profile == null) return null;

        Integer anchor = castingAnchors.get(player.getUniqueId());
        if (anchor == null) {
            // Se não tem anchor, retorna normal
            return profile.getSkillInSlot(visualSlotIndex + 1);
        }

        if (visualSlotIndex == anchor) {
            return null; // O slot âncora é sempre vazio/buraco
        }

        // Se o slot visualizado é maior que o âncora, shift right
        if (visualSlotIndex > anchor) {
            // Ex: Anchor 0. Query 1.
            // Skill que deveria estar aqui é a do índice (1-1) = 0.
            int originalIndex = visualSlotIndex - 1;
            return profile.getSkillInSlot(originalIndex + 1); // +1 because profile is 1-based
        }

        // Se o slot visualizado é menor que o âncora, mantém original
        // Ex: Anchor 2. Query 1. Skill Index 1.
        return profile.getSkillInSlot(visualSlotIndex + 1);
    }

    public void loadSpells() {
        loadedSpells.clear();
        
        File moduleFolder = new File(module.getPlugin().getDataFolder(), "modules/spells");
        File spellsFolder = new File(moduleFolder, "spells");
        
        if (!spellsFolder.exists()) {
            spellsFolder.mkdirs();
        }

        createDefaultSpell(spellsFolder, "fireball.yml");
        createDefaultSpell(spellsFolder, "icebolt.yml");
        createDefaultSpell(spellsFolder, "magic_missile.yml");
        createDefaultSpell(spellsFolder, "arcane_pulse.yml");

        File[] files = spellsFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null) return;

        for (File file : files) {
            YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
            String id = file.getName().replace(".yml", "");
            
            String name = config.getString("name", id);
            String mythicSkill = config.getString("mythic-skill");
            double cooldown = config.getDouble("cooldown");
            double mana = config.getDouble("mana");
            double stamina = config.getDouble("stamina");
            List<String> lore = config.getStringList("lore");
            Spell spell = new Spell(id, mythicSkill, name, lore, cooldown, mana, stamina);
            loadedSpells.put(id, spell);
        }
        
        MidgardLogger.info("Loaded " + loadedSpells.size() + " spells from " + spellsFolder.getPath());
    }

    private void createDefaultSpell(File folder, String fileName) {
        File file = new File(folder, fileName);
        if (!file.exists()) {
            try {
                module.getPlugin().saveResource("modules/spells/spells/" + fileName, false);
            } catch (Exception e) {
                MidgardLogger.warn("Could not save default spell " + fileName + ": " + e.getMessage());
            }
        }
    }

    public Spell getSpell(String id) {
        return loadedSpells.get(id);
    }


    public Collection<Spell> getSpells() {
        return Collections.unmodifiableCollection(loadedSpells.values());
    }
    
    public Set<String> getLoadedSpellIds() {
        return Collections.unmodifiableSet(loadedSpells.keySet());
    }

    public SpellProfile getProfile(Player player) {
        MidgardProfile coreProfile = MidgardCore.getProfileManager().getProfile(player.getUniqueId());
        if (coreProfile == null) return null; 
        return coreProfile.getOrCreateData(SpellProfile.class);
    }

    public boolean castSpell(Player player, String spellId) {
        Spell spell = getSpell(spellId);
        if (spell == null) return false;

        SpellProfile profile = getProfile(player);
        if (profile == null) return false;
        
        if (profile.isOnCooldown(spellId)) {
            long remaining = profile.getCooldownRemainingKey(spellId) / 1000;
            String cooldownMsg = module.getMessage("casting.on_cooldown")
                .replace("%spell%", spell.getDisplayName())
                .replace("%time%", String.valueOf(remaining));
            me.ray.midgard.core.text.MessageUtils.send(player, cooldownMsg);
            return false;
        }

        // Check Mana/Stamina
        if (!module.getResourceProvider().consumeMana(player, spell.getManaCost())) {
             me.ray.midgard.core.text.MessageUtils.send(player, module.getMessage("casting.no_mana"));
             return false;
        }

        String skillName = spell.getMythicSkillName();
        
        // Validate Skill
        Optional<Skill> mythicSkill = MythicProvider.get().getSkillManager().getSkill(skillName);
        if (mythicSkill.isEmpty()) {
            String errorMsg = module.getMessage("errors.config_error")
                .replace("%skill%", skillName);
            me.ray.midgard.core.text.MessageUtils.send(player, errorMsg);
            return false;
        }

        // Cast
        boolean success = MythicBukkit.inst().getAPIHelper().castSkill(player, skillName);
        if (success) {
            profile.setCooldown(spellId, spell.getCooldown());
            if (module.getConfig().getBoolean("general.show_cast_messages", true)) {
                String castMsg = module.getMessage("casting.spell_cast")
                    .replace("%spell%", spell.getDisplayName());
                me.ray.midgard.core.text.MessageUtils.send(player, castMsg);
            }
        }
        
        // Cleanup? 
        // Usually fine to leave variables as they are scoped to "last cast state" or overwrite next time.
        // If we want to be safe, we can clear them later, but that might clear variables for other skills running async.
        // A unique prefix like "spell_midgard_var_" is safer.

        return success;
    }

}
