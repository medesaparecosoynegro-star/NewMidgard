package me.ray.midgard.core.profile;

import com.google.gson.*;
import me.ray.midgard.core.database.DatabaseManager;
import org.bukkit.Bukkit;

import java.lang.reflect.Type;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

/**
 * Repositório para persistência de perfis no banco de dados.
 */
public class ProfileRepository {

    private final DatabaseManager databaseManager;
    private final Gson gson;

    /**
     * Construtor do ProfileRepository.
     *
     * @param databaseManager Gerenciador de banco de dados.
     */
    public ProfileRepository(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
        this.gson = new GsonBuilder()
                .registerTypeAdapter(MidgardProfile.class, new ProfileSerializer())
                .registerTypeAdapter(MidgardProfile.class, new ProfileDeserializer())
                .create();
        
        initTable();
    }

    private void initTable() {
        databaseManager.execute(conn -> {
            try (PreparedStatement ps = conn.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS midgard_profiles (" +
                            "uuid VARCHAR(36) PRIMARY KEY, " +
                            "name VARCHAR(16), " +
                            "data JSON" +
                            ")")) {
                ps.executeUpdate();
            } catch (SQLException e) {
                Bukkit.getLogger().log(Level.SEVERE, "Erro ao criar tabela de perfis", e);
            }
        });
    }

    /**
     * Carrega um perfil do banco de dados.
     *
     * @param uuid UUID do jogador.
     * @param name Nome do jogador.
     * @return CompletableFuture com o perfil.
     */
    public CompletableFuture<MidgardProfile> loadProfile(UUID uuid, String name) {
        return databaseManager.executeQuery(conn -> {
            try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM midgard_profiles WHERE uuid = ?")) {
                ps.setString(1, uuid.toString());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        String jsonData = rs.getString("data");
                        MidgardProfile profile = gson.fromJson(jsonData, MidgardProfile.class);
                        // Inject transient fields if any (like name if it changed, though we store it)
                        return profile;
                    } else {
                        return new MidgardProfile(uuid, name);
                    }
                }
            } catch (SQLException e) {
                Bukkit.getLogger().log(Level.SEVERE, "Falha ao carregar perfil para " + name, e);
                return new MidgardProfile(uuid, name);
            }
        });
    }

    /**
     * Salva um perfil no banco de dados.
     *
     * @param profile Perfil a ser salvo.
     * @return CompletableFuture vazio.
     */
    public CompletableFuture<Void> saveProfile(MidgardProfile profile) {
        return databaseManager.executeAsync(conn -> {
            String sql;
            boolean isSQLite = databaseManager.getDatabaseType().equalsIgnoreCase("sqlite");
            
            if (isSQLite) {
                sql = "INSERT INTO midgard_profiles (uuid, name, data) VALUES (?, ?, ?) " +
                      "ON CONFLICT(uuid) DO UPDATE SET name = excluded.name, data = excluded.data";
            } else {
                sql = "INSERT INTO midgard_profiles (uuid, name, data) VALUES (?, ?, ?) " +
                      "ON DUPLICATE KEY UPDATE name = ?, data = ?";
            }

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, profile.getUuid().toString());
                ps.setString(2, profile.getName());
                String json = gson.toJson(profile);
                ps.setString(3, json);
                
                if (!isSQLite) {
                    ps.setString(4, profile.getName());
                    ps.setString(5, json);
                }
                
                ps.executeUpdate();
            } catch (SQLException e) {
                Bukkit.getLogger().log(Level.SEVERE, "Falha ao salvar perfil para " + profile.getName(), e);
            }
        });
    }

    // Custom Serializer to handle the polymorphic ModuleData map
    private static class ProfileSerializer implements JsonSerializer<MidgardProfile> {
        @Override
        public JsonElement serialize(MidgardProfile src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject root = new JsonObject();
            root.addProperty("uuid", src.getUuid().toString());
            root.addProperty("name", src.getName());
            
            JsonObject modules = new JsonObject();
            // We need to access the map. Since it's private, we might need a getter or reflection.
            // Assuming we added a getter or made it accessible. 
            // For now, let's assume we can iterate keys.
            // Wait, we can't easily iterate the map from outside if there is no getter.
            // Let's add a getter to MidgardProfile for the raw map or an iterator.
            
            // Actually, let's use reflection here for the adapter to keep the API clean
            try {
                java.lang.reflect.Field field = MidgardProfile.class.getDeclaredField("moduleData");
                field.setAccessible(true);
                @SuppressWarnings("unchecked")
                Map<Class<? extends ModuleData>, ModuleData> map = (Map<Class<? extends ModuleData>, ModuleData>) field.get(src);
                
                for (Map.Entry<Class<? extends ModuleData>, ModuleData> entry : map.entrySet()) {
                    JsonObject moduleWrapper = new JsonObject();
                    moduleWrapper.addProperty("class", entry.getKey().getName());
                    moduleWrapper.add("data", context.serialize(entry.getValue()));
                    modules.add(entry.getKey().getSimpleName(), moduleWrapper);
                }
                
                // Serialize unknown data (Pass-through)
                for (Map.Entry<String, JsonElement> entry : src.getUnknownData().entrySet()) {
                    JsonObject moduleWrapper = new JsonObject();
                    moduleWrapper.addProperty("class", entry.getKey());
                    moduleWrapper.add("data", entry.getValue());
                    // Use class name as key since simple name might collide or be unavailable
                    String simpleName = entry.getKey().substring(entry.getKey().lastIndexOf('.') + 1);
                    // Avoid overwriting active modules if somehow a collision happens, though unlikely
                    if (!modules.has(simpleName)) {
                        modules.add(simpleName, moduleWrapper);
                    } else {
                         modules.add(entry.getKey(), moduleWrapper);
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            
            root.add("modules", modules);
            return root;
        }
    }

    private static class ProfileDeserializer implements JsonDeserializer<MidgardProfile> {
        @Override
        public MidgardProfile deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject root = json.getAsJsonObject();
            UUID uuid = UUID.fromString(root.get("uuid").getAsString());
            String name = root.get("name").getAsString();
            
            MidgardProfile profile = new MidgardProfile(uuid, name);
            
            if (root.has("modules")) {
                JsonObject modules = root.getAsJsonObject("modules");
                for (Map.Entry<String, JsonElement> entry : modules.entrySet()) {
                    JsonObject moduleWrapper = entry.getValue().getAsJsonObject();
                    String className = moduleWrapper.get("class").getAsString();
                    try {
                        Class<?> clazz = Class.forName(className);
                        if (ModuleData.class.isAssignableFrom(clazz)) {
                            ModuleData data = context.deserialize(moduleWrapper.get("data"), clazz);
                            profile.setData(data);
                        }
                    } catch (ClassNotFoundException e) {
                        // Class not found on this server (e.g. Lobby doesn't have Combat module)
                        // Store separately to preserve it
                        profile.addUnknownData(className, moduleWrapper.get("data"));
                    }
                }
            }
            
            return profile;
        }
    }
}
