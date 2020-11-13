package net.yatopia.hwaccel.utils.registry;

import com.google.common.base.Preconditions;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Registry<T> {

    private static final Logger LOGGER = LogManager.getLogger();

    final Map<Identifier, T> reg = new ConcurrentHashMap<>();
    final String regName;

    public Registry(String regName) {
        Preconditions.checkNotNull(regName);
        Preconditions.checkArgument(!regName.trim().isBlank());
        this.regName = regName;
    }

    public void register(Identifier identifier, T object) {
        Preconditions.checkNotNull(identifier);
        Preconditions.checkNotNull(object);
        Preconditions.checkArgument(!reg.containsKey(identifier));
        LOGGER.debug("Registered {}: {} in registry {}", identifier, object, regName);
        reg.put(identifier, object);
    }

    public void unregister(String namespace) {
        Preconditions.checkNotNull(namespace);
        Preconditions.checkArgument(!namespace.trim().isBlank() && Identifier.isNamespaceValid(namespace), "Invalid namespace");
        if(!reg.entrySet().removeIf(entry -> entry.getKey().getNamespace().equals(namespace)))
            throw new IllegalArgumentException("No entries found");
    }

    public void unregister(Identifier identifier) {
        Preconditions.checkNotNull(identifier);
        if(!reg.entrySet().removeIf(entry -> entry.getKey().equals(identifier)))
            throw new IllegalArgumentException("No entries found");
    }

    public T tryGet(Identifier identifier) {
        Preconditions.checkNotNull(identifier);
        return reg.get(identifier);
    }

    public T getOrThrow(Identifier identifier) {
        final T t = tryGet(identifier);
        if(t == null)
            throw new IllegalArgumentException("Missing registry entry: " + identifier);
        return t;
    }

    public void clear() {
        reg.clear();
    }

}
