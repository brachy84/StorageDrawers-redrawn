package com.jaquadro.minecraft.storagedrawers.security;

import com.jaquadro.minecraft.storagedrawers.api.security.ISecurityProvider;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import java.util.Map;

public class SecurityRegistry {

    private final Map<String, ISecurityProvider> registry = new Object2ObjectOpenHashMap<>();

    public void registerProvider(ISecurityProvider provider) {
        registry.put(provider.getProviderID(), provider);
    }

    public ISecurityProvider getProvider(String name) {
        return registry.get(name);
    }
}
