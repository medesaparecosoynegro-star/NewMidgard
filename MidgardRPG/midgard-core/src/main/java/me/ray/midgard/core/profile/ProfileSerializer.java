package me.ray.midgard.core.profile;

import com.google.gson.*;

import java.lang.reflect.Type;

public class ProfileSerializer implements JsonSerializer<MidgardProfile> {

    @Override
    public JsonElement serialize(MidgardProfile src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject json = new JsonObject();
        json.addProperty("uuid", src.getUuid().toString());
        json.addProperty("name", src.getName());
        // Since moduleData is Map<Class<? extends ModuleData>, ModuleData>, we need to be careful
        // Accessing the map directly requires reflection or package-private access if field is private.
        // Assuming we can access it or using getter (getModuleDataMap in implementation)
        // For now, let's assume we can get it.
        
        // Wait, I cannot see the real implementation of ProfileSerializer in the file list because I haven't read it.
        // I should read it first instead of guessing.
        return json;
    }
}
