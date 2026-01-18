package me.ray.midgard.core.profile;

import me.ray.midgard.core.database.DatabaseManager;
import me.ray.midgard.core.debug.DebugCategory;
import me.ray.midgard.core.debug.MidgardLogger;
import me.ray.midgard.core.redis.RedisManager;
import me.ray.midgard.core.profile.data.VanillaData; // Import added
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Gerencia os perfis dos jogadores (carregamento, salvamento e cache).
 */
public class ProfileManager implements Listener {

    private final Map<UUID, MidgardProfile> profiles = new ConcurrentHashMap<>();
    private final Map<UUID, java.util.concurrent.CompletableFuture<Void>> pendingSaves = new ConcurrentHashMap<>();
    private final ProfileRepository repository;
    private final RedisManager redisManager;

    /**
     * Construtor do ProfileManager.
     *
     * @param plugin Instância do plugin.
     * @param databaseManager Gerenciador de banco de dados.
     * @param redisManager Gerenciador do Redis (pode ser null).
     */
    public ProfileManager(JavaPlugin plugin, DatabaseManager databaseManager, RedisManager redisManager) {
        this.repository = new ProfileRepository(databaseManager);
        this.redisManager = redisManager;
        Bukkit.getPluginManager().registerEvents(this, plugin);
        loadOnlinePlayers();
    }

    private void loadOnlinePlayers() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            repository.loadProfile(player.getUniqueId(), player.getName()).thenAccept(profile -> {
                if (profile != null) {
                    profiles.put(player.getUniqueId(), profile);
                    MidgardLogger.debug(DebugCategory.CORE, "Perfil carregado para %s (recuperação de reload)", player.getName());
                }
            });
        }
    }

    /**
     * Obtém o perfil de um jogador pelo UUID.
     *
     * @param uuid UUID do jogador.
     * @return Perfil do jogador ou null se não carregado.
     */
    public MidgardProfile getProfile(UUID uuid) {
        return profiles.get(uuid);
    }
    
    /**
     * Obtém o perfil de um jogador.
     *
     * @param player Jogador.
     * @return Perfil do jogador ou null se não carregado.
     */
    public MidgardProfile getProfile(Player player) {
        return getProfile(player.getUniqueId());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onAsyncLogin(AsyncPlayerPreLoginEvent event) {
        if (event.getLoginResult() != AsyncPlayerPreLoginEvent.Result.ALLOWED) return;

        // Redis Locking Check
        if (redisManager != null && redisManager.isEnabled()) {
            String lockKey = "lock:profile:" + event.getUniqueId();
            int retries = 0;
            boolean locked = true;
            
            while (retries < 10) {
                java.util.function.Function<redis.clients.jedis.Jedis, Boolean> checkLock = j -> j.exists(lockKey);
                Boolean exists = redisManager.execute(checkLock);
                if (exists == null || !exists) {
                    locked = false;
                    break;
                }
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
                retries++;
            }
            
            if (locked) {
                event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, net.kyori.adventure.text.Component.text("Sessão anterior ainda está sendo salva. Tente novamente em alguns segundos."));
                return;
            }
        }

        // Load profile from DB
        try {
            MidgardProfile profile = me.ray.midgard.core.debug.MidgardProfiler.monitor("profile_load_async",
                () -> repository.loadProfile(event.getUniqueId(), event.getName()).join()
            );

            if (profile != null) {
                profiles.put(event.getUniqueId(), profile);
                MidgardLogger.debug(DebugCategory.CORE, "Perfil carregado assincronamente para %s (UUID: %s)", event.getName(), event.getUniqueId());
            }
        } catch (Exception e) {
            MidgardLogger.error("Erro ao carregar perfil para " + event.getName(), e);
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        // Profile is already loaded in AsyncPreLogin
        MidgardProfile profile = profiles.get(event.getPlayer().getUniqueId());
        if (profile == null) {
            // Fallback if something went wrong
            event.getPlayer().kick(net.kyori.adventure.text.Component.text("Falha ao carregar perfil. Por favor, reconecte-se."));
            return;
        }

        // Apply Vanilla Data (Inventory, Health, etc.)
        if (profile.hasData(VanillaData.class)) {
            VanillaData vanillaData = profile.getData(VanillaData.class);
            vanillaData.applyTo(event.getPlayer());
            MidgardLogger.debug(DebugCategory.CORE, "Dados vanilla restaurados para %s", event.getPlayer().getName());
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        MidgardProfile profile = profiles.remove(event.getPlayer().getUniqueId());
        if (profile != null) {
            MidgardLogger.debug(DebugCategory.CORE, "Salvando perfil de %s ao sair...", event.getPlayer().getName());
            
            // Capture Vanilla Data
            try {
                VanillaData vanillaData = VanillaData.fromPlayer(event.getPlayer());
                profile.setData(vanillaData);
            } catch (Exception e) {
                MidgardLogger.error("Erro ao capturar dados vanilla de " + event.getPlayer().getName(), e);
            }

            String lockKey = "lock:profile:" + event.getPlayer().getUniqueId();
            if (redisManager != null && redisManager.isEnabled()) {
                redisManager.execute(jedis -> { jedis.setex(lockKey, 10, "locked"); });
            }
            
            saveProfile(profile).thenRun(() -> {
                if (redisManager != null && redisManager.isEnabled()) {
                    redisManager.execute(jedis -> {
                        jedis.del(lockKey);
                        jedis.publish("sync:saved:" + profile.getUuid(), "saved");
                    });
                }
            });
        }
    }
    
    /**
     * Salva um perfil no banco de dados.
     *
     * @param profile Perfil a ser salvo.
     * @return Future que completa quando o salvamento termina.
     */
    public java.util.concurrent.CompletableFuture<Void> saveProfile(MidgardProfile profile) {
        java.util.concurrent.CompletableFuture<Void> future = repository.saveProfile(profile);
        pendingSaves.put(profile.getUuid(), future);
        future.thenRun(() -> pendingSaves.remove(profile.getUuid()));
        return future;
    }
    
    /**
     * Encerra o gerenciador e salva todos os perfis.
     */
    public void shutdown() {
        // Save remaining profiles
        for (MidgardProfile profile : profiles.values()) {
            MidgardLogger.debug(DebugCategory.CORE, "Salvando perfil remanescente no shutdown: %s", profile.getName());
            saveProfile(profile);
        }
        profiles.clear();
        
        // Wait for all pending saves
        if (!pendingSaves.isEmpty()) {
            MidgardLogger.info("Aguardando " + pendingSaves.size() + " salvamentos pendentes...");
            try {
                java.util.concurrent.CompletableFuture.allOf(pendingSaves.values().toArray(new java.util.concurrent.CompletableFuture[0]))
                    .get(20, java.util.concurrent.TimeUnit.SECONDS);
            } catch (Exception e) {
                MidgardLogger.error("Timeout ou erro ao aguardar salvamento de perfis! Dados podem ter sido perdidos.", e);
            }
        }
    }
}
