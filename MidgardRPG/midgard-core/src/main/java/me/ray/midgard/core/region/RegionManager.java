package me.ray.midgard.core.region;

import org.bukkit.Location;

import java.util.Collections;
import java.util.Set;

/**
 * Gerencia a integração com sistemas de regiões (como WorldGuard).
 * Permite verificar em quais regiões um jogador está.
 */
public class RegionManager {

    private static RegionManager instance;
    private RegionProvider provider;

    /**
     * Obtém a instância única do RegionManager.
     *
     * @return Instância do RegionManager.
     */
    public static RegionManager getInstance() {
        if (instance == null) {
            instance = new RegionManager();
        }
        return instance;
    }

    private RegionManager() {
        // Default provider (empty)
        this.provider = location -> Collections.emptySet();
    }

    /**
     * Define o provedor de regiões (ex: WorldGuardIntegration).
     *
     * @param provider Provedor de regiões.
     */
    public void setProvider(RegionProvider provider) {
        this.provider = provider;
    }

    /**
     * Obtém as regiões em uma determinada localização.
     *
     * @param location Localização a ser verificada.
     * @return Conjunto de IDs das regiões.
     */
    public Set<String> getRegions(Location location) {
        return provider.getRegions(location);
    }

    /**
     * Verifica se uma localização está dentro de uma região específica.
     *
     * @param location Localização a ser verificada.
     * @param regionId ID da região.
     * @return true se estiver na região, false caso contrário.
     */
    public boolean isInRegion(Location location, String regionId) {
        return provider.isInRegion(location, regionId);
    }
}
