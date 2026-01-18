package me.ray.midgard.core.text;

import me.clip.placeholderapi.PlaceholderAPI;
import me.ray.midgard.core.MidgardCore;
import me.ray.midgard.core.i18n.MessageKey;
import me.ray.midgard.core.i18n.Placeholder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Utilitários para formatação e envio de mensagens.
 * <p>
 * Suporta:
 * <ul>
 *     <li>MiniMessage format</li>
 *     <li>Conversão de códigos legados</li>
 *     <li>PlaceholderAPI</li>
 *     <li>MessageKey tipado</li>
 *     <li>Placeholder tipado</li>
 * </ul>
 *
 * @since 1.0.0
 */
public class MessageUtils {

    private static final MiniMessage MM = MiniMessage.miniMessage();

    /**
     * Parses a MiniMessage string into a Component.
     * @param message The string with MiniMessage tags.
     * @return The parsed Component.
     */
    public static Component parse(String message) {
        return MM.deserialize(convertLegacyColors(message));
    }

    /**
     * Parses a MiniMessage string into a Component, resolving PAPI placeholders if a player is provided.
     * @param player The player for context.
     * @param message The string with MiniMessage tags and PAPI placeholders.
     * @return The parsed Component.
     */
    public static Component parse(Player player, String message) {
        if (player != null) {
            message = PlaceholderAPI.setPlaceholders(player, message);
        }
        return MM.deserialize(convertLegacyColors(message));
    }

    public static String serialize(Component component) {
        return MM.serialize(component);
    }

    public static String center(String message) {
        return center(message, 154);
    }

    public static String center(String message, int centerPx) {
        if (message == null || message.equals("")) return "";
        message = convertLegacyColors(message); // Ensure colors are handled (though MM handles them too)
        // But for length calc we need to strip colors/tags.
        // Simplified legacy strip for length calc:
        // Actually, we should use a method that strips both legacy and MiniMessage tags for length calc?
        // Or just assume legacy & codes are main style here as per usage in CharacterMenu.
        
        int messagePxSize = 0;
        boolean previousCode = false;
        boolean isBold = false;
        
        // Quick & Dirty legacy color strip and bold detection
        // Note: CharacterMenu uses "&" codes.
        for (char c : message.toCharArray()) {
            if (c == '§' || c == '&') {
                previousCode = true;
                continue;
            } else if (previousCode) {
                previousCode = false;
                if (c == 'l' || c == 'L') {
                   isBold = true;
                   continue;
                } else {
                   // Reset bold if color code (0-9, a-f, r)
                   // But simplified: assume any color resets format
                   isBold = false;
                }
            } else {
                DefaultFontInfo dFI = DefaultFontInfo.getDefaultFontInfo(c);
                messagePxSize += isBold ? dFI.getBoldLength() : dFI.getLength();
                messagePxSize++;
            }
        }

        int halvedMessageSize = messagePxSize / 2;
        int toCompensate = centerPx - halvedMessageSize;
        int spaceLength = DefaultFontInfo.SPACE.getLength() + 1;
        int compensated = 0;
        StringBuilder sb = new StringBuilder();
        while (compensated < toCompensate) {
            sb.append(" ");
            compensated += spaceLength;
        }
        sb.append(message);
        return sb.toString();
    }

    private static String convertLegacyColors(String message) {
        // Hex support &#RRGGBB
        message = message.replaceAll("&#([0-9a-fA-F]{6})", "<#$1>");

        return message
                .replace("&0", "<black>").replace("§0", "<black>")
                .replace("&1", "<dark_blue>").replace("§1", "<dark_blue>")
                .replace("&2", "<dark_green>").replace("§2", "<dark_green>")
                .replace("&3", "<dark_aqua>").replace("§3", "<dark_aqua>")
                .replace("&4", "<dark_red>").replace("§4", "<dark_red>")
                .replace("&5", "<dark_purple>").replace("§5", "<dark_purple>")
                .replace("&6", "<gold>").replace("§6", "<gold>")
                .replace("&7", "<gray>").replace("§7", "<gray>")
                .replace("&8", "<dark_gray>").replace("§8", "<dark_gray>")
                .replace("&9", "<blue>").replace("§9", "<blue>")
                .replace("&a", "<green>").replace("§a", "<green>")
                .replace("&b", "<aqua>").replace("§b", "<aqua>")
                .replace("&c", "<red>").replace("§c", "<red>")
                .replace("&d", "<light_purple>").replace("§d", "<light_purple>")
                .replace("&e", "<yellow>").replace("§e", "<yellow>")
                .replace("&f", "<white>").replace("§f", "<white>")
                .replace("&k", "<obfuscated>").replace("§k", "<obfuscated>")
                .replace("&l", "<bold>").replace("§l", "<bold>")
                .replace("&m", "<strikethrough>").replace("§m", "<strikethrough>")
                .replace("&n", "<underlined>").replace("§n", "<underlined>")
                .replace("&o", "<italic>").replace("§o", "<italic>")
                .replace("&r", "<reset>").replace("§r", "<reset>");
    }

    /**
     * Sends a parsed message to a CommandSender.
     * @param sender The recipient.
     * @param message The raw MiniMessage string.
     */
    public static void send(CommandSender sender, String message) {
        sender.sendMessage(parse(message));
    }

    public static void send(Player player, String message) {
        player.sendMessage(parse(player, message));
    }

    /**
     * Sends a component directly to a CommandSender.
     * @param sender The recipient.
     * @param message The component to send.
     */
    public static void send(CommandSender sender, Component message) {
        sender.sendMessage(message);
    }

    /**
     * Sends a parsed message with a prefix.
     * @param sender The recipient.
     * @param message The raw MiniMessage string.
     */
    public static void sendPrefixed(CommandSender sender, String message) {
        String prefix = "<gradient:#5e4fa2:#f79459><bold>Midgard</bold></gradient> <dark_gray>»</dark_gray> <gray>";
        if (sender instanceof Player player) {
            sender.sendMessage(parse(player, prefix + message));
        } else {
            sender.sendMessage(parse(prefix + message));
        }
    }

    public static void sendActionBar(Player player, String message) {
        player.sendActionBar(parse(player, message));
    }
    
    // ============================================
    // MESSAGEKEY SUPPORT
    // ============================================
    
    /**
     * Envia uma mensagem por MessageKey para um jogador.
     *
     * @param player O jogador destinatário
     * @param messageKey A chave da mensagem
     */
    public static void send(Player player, MessageKey messageKey) {
        if (player == null || messageKey == null) return;
        player.sendMessage(MidgardCore.getLanguageManager().getMessage(messageKey));
    }
    
    /**
     * Envia uma mensagem por MessageKey com placeholders.
     *
     * @param player O jogador destinatário
     * @param messageKey A chave da mensagem
     * @param placeholders Os placeholders a substituir
     */
    public static void send(Player player, MessageKey messageKey, Placeholder... placeholders) {
        if (player == null || messageKey == null) return;
        player.sendMessage(MidgardCore.getLanguageManager().getMessage(messageKey, placeholders));
    }
    
    /**
     * Envia uma action bar por MessageKey.
     *
     * @param player O jogador destinatário
     * @param messageKey A chave da mensagem
     */
    public static void sendActionBar(Player player, MessageKey messageKey) {
        if (player == null || messageKey == null) return;
        player.sendActionBar(MidgardCore.getLanguageManager().getMessage(messageKey));
    }
    
    /**
     * Envia uma action bar por MessageKey com placeholders.
     *
     * @param player O jogador destinatário
     * @param messageKey A chave da mensagem
     * @param placeholders Os placeholders a substituir
     */
    public static void sendActionBar(Player player, MessageKey messageKey, Placeholder... placeholders) {
        if (player == null || messageKey == null) return;
        player.sendActionBar(MidgardCore.getLanguageManager().getMessage(messageKey, placeholders));
    }
    
    // ============================================
    // QUICK SEND METHODS
    // ============================================
    
    /**
     * Envia uma mensagem de sucesso.
     *
     * @param player O jogador destinatário
     * @param message A mensagem
     */
    public static void sendSuccess(Player player, String message) {
        player.sendMessage(parse(player, "<green>✔ <white>" + message));
        player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.5f);
    }
    
    /**
     * Envia uma mensagem de erro.
     *
     * @param player O jogador destinatário
     * @param message A mensagem
     */
    public static void sendError(Player player, String message) {
        player.sendMessage(parse(player, "<red>✖ <gray>" + message));
        player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_VILLAGER_NO, 0.8f, 1.0f);
    }
    
    /**
     * Envia uma mensagem de aviso.
     *
     * @param player O jogador destinatário
     * @param message A mensagem
     */
    public static void sendWarning(Player player, String message) {
        player.sendMessage(parse(player, "<gold>⚠ <gray>" + message));
    }
    
    /**
     * Envia uma mensagem informativa.
     *
     * @param player O jogador destinatário
     * @param message A mensagem
     */
    public static void sendInfo(Player player, String message) {
        player.sendMessage(parse(player, "<aqua>ℹ <gray>" + message));
    }
}
