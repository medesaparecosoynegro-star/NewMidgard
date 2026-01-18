package me.ray.midgard.modules.spells.task;

import me.ray.midgard.core.text.MessageUtils;
import me.ray.midgard.modules.spells.SpellsModule;
import me.ray.midgard.modules.spells.data.SpellProfile;
import me.ray.midgard.modules.spells.manager.SpellManager;
import me.ray.midgard.modules.spells.obj.Spell;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

public class SkillBarTask extends BukkitRunnable {

    private final SpellsModule module;
    private final SpellManager manager;

    public SkillBarTask(SpellsModule module) {
        this.module = module;
        this.manager = module.getSpellManager();
    }

    @Override
    public void run() {
        for (UUID uuid : manager.getCastingPlayers()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null) continue;

            updateActionBar(player);
        }
    }

    private void updateActionBar(Player player) {
        SpellProfile profile = manager.getProfile(player);
        if (profile == null) return;
        
        double currentMana = module.getResourceProvider().getMana(player);

        StringBuilder bar = new StringBuilder();
        
        // Verifica o estilo
        if (profile.getCastingStyle() == SpellProfile.CastingStyle.COMBO) {
            bar.append(module.getMessage("actionbar.combo_prefix"));
            for (int i = 1; i <= 6; i++) {
                SpellProfile.ComboBinding binding = profile.getComboSlot(i);
                String result;
                if (binding != null && binding.getSpellId() != null) {
                    Spell spell = manager.getSpell(binding.getSpellId());
                    result = (spell != null) ? module.getMessage("actionbar.combo_set").replace("%sequence%", binding.getSequence()) 
                                             : module.getMessage("actionbar.error");
                } else {
                     // Mostra sequencia vazia
                     String seq = manager.getDefaultCombo(i);
                     result = module.getMessage("actionbar.combo_default").replace("%sequence%", seq);
                }
                bar.append(result).append(" ");
            }
        } else {
            // Loop slots 1 to 6 (SkillBar Mode)
            for (int i = 1; i <= 9; i++) {
                String spellId = manager.getSkillInVirtualSlot(player, i - 1);
                
                if (spellId == null) {
                   if (i <= 6) bar.append("<dark_gray>").append(i).append(":✖");
                } else {
                    Spell spell = manager.getSpell(spellId);
                    if (spell == null) {
                        bar.append("<red>").append(i).append(":ERR");
                    } else {
                        // Check status
                        boolean cooldown = profile.isOnCooldown(spellId);
                        boolean noMana = (currentMana < spell.getManaCost());
                        
                        if (cooldown) {
                            long cd = profile.getCooldownRemainingKey(spellId) / 1000;
                            if (cd == 0) cd = 1; 
                            bar.append("<red>").append(i).append(":").append(spell.getDisplayName()).append(" ⏳").append(cd).append("s");
                        } else if (noMana) {
                            bar.append("<blue>").append(i).append(":").append(spell.getDisplayName());
                        } else {
                            bar.append("<green>").append(i).append(":").append(spell.getDisplayName());
                        }
                    }
                }
                 if (i < 9 && (i <= 6 || spellId != null)) bar.append("   ");
            }
        }
        
        MessageUtils.sendActionBar(player, bar.toString());
    }
}
