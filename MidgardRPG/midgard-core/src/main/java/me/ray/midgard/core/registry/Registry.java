package me.ray.midgard.core.registry;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Registry<K, V> {

    private final Map<K, V> map = new HashMap<>();

    public void register(K key, V value) {
        map.put(key, value);
    }

    public Optional<V> get(K key) {
        return Optional.ofNullable(map.get(key));
    }

    public Collection<V> getAll() {
        return map.values();
    }
    
    public Collection<K> getKeys() {
        return map.keySet();
    }
    
    public boolean contains(K key) {
        return map.containsKey(key);
    }
    
    /**
     * Limpa todos os registros.
     */
    public void clear() {
        map.clear();
    }
    
    /**
     * Retorna o tamanho do registro.
     */
    public int size() {
        return map.size();
    }
}
