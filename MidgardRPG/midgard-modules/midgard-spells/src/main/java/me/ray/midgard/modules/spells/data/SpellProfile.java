package me.ray.midgard.modules.spells.data;

import me.ray.midgard.core.profile.ModuleData;
import java.util.HashMap;
import java.util.Map;

public class SpellProfile implements ModuleData {

    // Removed direct reference to UUID as it's owned by the MidgardProfile
    
    // Skill Bar Mapping: Slot (1-9) -> Spell ID
    private final Map<Integer, String> skillBar = new HashMap<>();
    
    // Combo Mapping: Slot (1-6) -> ComboBinding
    private final Map<Integer, ComboBinding> comboSlots = new HashMap<>();
    
    // Cooldowns: Spell ID -> Expiration Timestamp (System.currentTimeMillis)
    private final Map<String, Long> cooldowns = new HashMap<>();


    // Spells Desbloqueados
    private final java.util.Set<String> unlockedSpells = new java.util.HashSet<>();

    // Estilo de Casting
    private CastingStyle castingStyle = CastingStyle.SKILLBAR;

    public static class ComboBinding {
        private String sequence;
        private String spellId;

        public ComboBinding(String sequence, String spellId) {
            this.sequence = sequence;
            this.spellId = spellId;
        }

        public String getSequence() { return sequence; }
        public void setSequence(String sequence) { this.sequence = sequence; }
        public String getSpellId() { return spellId; }
        public void setSpellId(String spellId) { this.spellId = spellId; }
    }

    public enum CastingStyle {
        SKILLBAR,
        COMBO
    }

    public SpellProfile() {
    }

    public void setSkillBarSlot(int slot, String spellId) {
        skillBar.put(slot, spellId);
    }
    
    // ...

    public String getSkillInSlot(int slot) {
        return skillBar.get(slot);
    }

    public void setComboSlot(int slot, String sequence, String spellId) {
        if (slot < 1 || slot > 6) return;
        comboSlots.put(slot, new ComboBinding(sequence, spellId));
    }

    public ComboBinding getComboSlot(int slot) {
        return comboSlots.get(slot);
    }

    public String getSpellByCombo(String comboSequence) {
        for (ComboBinding binding : comboSlots.values()) {
            if (binding.getSequence().equalsIgnoreCase(comboSequence)) {
                return binding.getSpellId();
            }
        }
        return null;
    }
    
    public Map<Integer, ComboBinding> getComboSlots() {
        return java.util.Collections.unmodifiableMap(comboSlots);
    }
    
    // Removed old getCombos/setCombo methods to enforce slot usage, or adapt them
    public void setComboLegacy(String combo, String spellId) {
        // Try to find slot with this sequence
        for (Map.Entry<Integer, ComboBinding> entry : comboSlots.entrySet()) {
            if (entry.getValue().getSequence().equalsIgnoreCase(combo)) {
                entry.getValue().setSpellId(spellId);
                return;
            }
        }
        // Else find first empty slot
        for (int i=1; i<=6; i++) {
            if (!comboSlots.containsKey(i)) {
                comboSlots.put(i, new ComboBinding(combo, spellId));
                return;
            }
        }
    }

    public boolean isOnCooldown(String spellId) {
        return cooldowns.containsKey(spellId) && cooldowns.get(spellId) > System.currentTimeMillis();
    }
    
    public long getCooldownRemainingKey(String spellId) {
        if (!isOnCooldown(spellId)) return 0;
        return cooldowns.get(spellId) - System.currentTimeMillis();
    }
    
    public void setCooldown(String spellId, double seconds) {
        cooldowns.put(spellId, System.currentTimeMillis() + (long)(seconds * 1000L));
    }

    public java.util.Set<String> getUnlockedSpells() {
        return unlockedSpells;
    }

    public void unlockSpell(String spellId) {
        unlockedSpells.add(spellId);
    }

    public boolean hasSpell(String spellId) {
        return unlockedSpells.contains(spellId);
    }

    public CastingStyle getCastingStyle() {
        return castingStyle;
    }

    public void setCastingStyle(CastingStyle castingStyle) {
        this.castingStyle = castingStyle;
    }
}
