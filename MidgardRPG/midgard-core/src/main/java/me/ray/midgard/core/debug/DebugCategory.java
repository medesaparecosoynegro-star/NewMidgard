package me.ray.midgard.core.debug;

public enum DebugCategory {
    CORE("Sistema", "§b"),
    COMBAT("Combate", "§c"),
    ITEMS("Itens", "§e"),
    SPELLS("Feitiços", "§5"),
    SCRIPT("Script", "§a"),
    DATABASE("Banco de Dados", "§6"),
    LOADER("Carregador", "§7"),
    ALL("Todos", "§f");

    private final String displayName;
    private final String color;

    DebugCategory(String displayName, String color) {
        this.displayName = displayName;
        this.color = color;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getColor() {
        return color;
    }
}
