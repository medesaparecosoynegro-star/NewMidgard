package me.ray.midgard.modules.combat.mechanics;

import me.ray.midgard.core.MidgardCore;
import me.ray.midgard.core.profile.MidgardProfile;
import me.ray.midgard.core.attribute.AttributeInstance;
import me.ray.midgard.core.attribute.CoreAttributeData;
import me.ray.midgard.modules.combat.CombatAttributes;
import me.ray.midgard.modules.combat.CombatManager;
import me.ray.midgard.modules.combat.RPGDamageCategory;
import me.ray.midgard.modules.combat.RPGDamageContext;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class PhysicalDamageCalculator implements DamageCalculator {

    private final CombatManager combatManager;

    public PhysicalDamageCalculator(CombatManager combatManager) {
        this.combatManager = combatManager;
    }

    @Override
    public DamageResult calculate(Player attacker, LivingEntity victim, CoreAttributeData attackerAttributes, RPGDamageContext context, double baseDamage) {
        me.ray.midgard.modules.combat.CombatConfig config = me.ray.midgard.modules.combat.CombatManager.getInstance().getConfig();
        double damage = config.baseHandDamage; // Começa com dano base da configuração (ex: 1.0)
        
        // Verifica se é um item Midgard para adicionar o dano da arma
        // Se a arma tiver dano vanilla 7, o baseDamage do evento vem como ~7 (dependendo do attack cooldown)
        // Aqui nós ignoramos o damage do evento (baseDamage param) para ter controle total,
        // mas precisamos somar o dano da arma se não for item customizado.
        
        boolean isCritical = false;

        // Verifica se é um item Midgard para ignorar o dano vanilla
        ItemStack mainHand = attacker.getInventory().getItemInMainHand();
        AttributeInstance weaponDmgAttr = attackerAttributes.getInstance(CombatAttributes.WEAPON_DAMAGE);
        double weaponDamage = weaponDmgAttr != null ? weaponDmgAttr.getValue() : 0.0;

        // Se weaponDamage é 0, pode ser uma arma vanilla sem atributos carregados
        // Se quisermos suportar vanilla weapons no sistema novo, teríamos que mapear Materiais -> Dano
        // Por enquanto, assumimos que se weaponDamage > 0, usamos ele. Se não, usamos base.
        
        AttributeInstance physDmgAttr = attackerAttributes.getInstance(CombatAttributes.PHYSICAL_DAMAGE);
        double flatPhysicalDamage = physDmgAttr != null ? physDmgAttr.getValue() : 0.0;
        
        // --- CÁLCULO DA FÓRMULA ---
        if (config.damageFormulaMode == me.ray.midgard.modules.combat.CombatConfig.ScalingMode.MULTIPLICATIVE) {
            // Modo RPG Moderno (Wynncraft/RuneScape style)
            // Dano = (BaseHand + WeaponDmg) * (1 + (Strength * Multiplier)) + FlatBonuses
            
            AttributeInstance strAttr = attackerAttributes.getInstance(CombatAttributes.STRENGTH);
            double strength = strAttr != null ? strAttr.getValue() : 0.0;
            
            double baseTotal = damage + weaponDamage; // (1 + Weapon)
            double multiplier = 1.0 + (strength * config.strengthMultiplier);
            
            damage = (baseTotal * multiplier) + flatPhysicalDamage;
            
        } else {
            // Modo Clássico (Aditivo)
            // Dano = BaseHand + WeaponDmg + (Strength -> Flat via Listener) + FlatBonuses
            // Note que 'flatPhysicalDamage' JÁ CONTÉM a força convertida pelo StatScalingListener nesse modo.
            
            damage = damage + weaponDamage + flatPhysicalDamage;
        }

        if (context.hasCategory(RPGDamageCategory.PROJECTILE)) {
            AttributeInstance projDmgAttr = attackerAttributes.getInstance(CombatAttributes.PROJECTILE_DAMAGE);
            double projectileDamage = projDmgAttr != null ? projDmgAttr.getValue() : 0.0;
            damage += projectileDamage;
        }

        AttributeInstance undeadDmgAttr = attackerAttributes.getInstance(CombatAttributes.UNDEAD_DAMAGE);
        double undeadDamage = undeadDmgAttr != null ? undeadDmgAttr.getValue() : 0.0;

        if (victim instanceof org.bukkit.entity.Monster && (victim.getType().name().contains("ZOMBIE") || victim.getType().name().contains("SKELETON") || victim.getType().name().contains("PHANTOM") || victim.getType().name().contains("WITHER"))) {
            damage += undeadDamage;
        }

        // Acerto Crítico
        AttributeInstance critChanceAttr = attackerAttributes.getInstance(CombatAttributes.CRITICAL_CHANCE);
        double critChance = critChanceAttr != null ? critChanceAttr.getValue() : 5.0;

        // Critical Resistance (Reduz a chance de crítico do atacante)
        if (victim instanceof Player p) {
             MidgardProfile profile = MidgardCore.getProfileManager().getProfile(p.getUniqueId());
             if (profile != null) {
                 CoreAttributeData victimData = profile.getOrCreateData(CoreAttributeData.class);
                 AttributeInstance critResAttr = victimData.getInstance(CombatAttributes.CRITICAL_RESISTANCE);
                 if (critResAttr != null) {
                     critChance = Math.max(0, critChance - critResAttr.getValue());
                 }
             }
        }

        AttributeInstance critDamageAttr = attackerAttributes.getInstance(CombatAttributes.CRITICAL_DAMAGE);
        double critDamage = critDamageAttr != null ? critDamageAttr.getValue() : 150.0;

        if (ThreadLocalRandom.current().nextDouble() * 100 < critChance) {
            damage *= (critDamage / 100.0);
            isCritical = true;
        }

        List<String> types = new ArrayList<>();
        boolean isProjectile = context.hasCategory(RPGDamageCategory.PROJECTILE);

        if (isProjectile) {
            types.add("Projectile");
        }

        if (context.hasCategory(RPGDamageCategory.PHYSICAL)) {
            types.add("Physical");
        }

        // Fallback se por algum motivo não tiver nenhum
        if (types.isEmpty()) {
            types.add("Physical");
        }

        String key = String.join("+", types);
        return new DamageResult(damage, isCritical, key);
    }
}
