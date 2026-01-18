package me.ray.midgard.core.command;

/**
 * Interface para registro de subcomandos administrativos.
 * Permite que módulos registrem seus próprios comandos de admin
 * sem precisar de dependência direta com o loader.
 */
public interface AdminCommandRegistry {
    
    /**
     * Registra um subcomando de admin.
     * 
     * @param command O comando a ser registrado
     */
    void registerSubcommand(MidgardCommand command);
    
    /**
     * Remove um subcomando de admin.
     * 
     * @param name Nome do subcomando
     */
    void unregisterSubcommand(String name);
}
