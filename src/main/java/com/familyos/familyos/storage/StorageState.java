package com.familyos.familyos.storage;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
public class StorageState {

    private static final List<String> DEFAULT_MODULES = List.of("profile", "settings", "configuration", "knowledge", "integrations");

    private Instant lastLoadedAt;
    private Instant lastSavedAt;
    private String lastSavedHash;
    private int schemaVersion = 1;
    private List<String> availableModules = DEFAULT_MODULES;
    private boolean initialized;

    public synchronized void markBootstrap(Instant bootstrapAt, Instant lastSaveAt, String hash) {
        this.initialized = true;
        this.lastLoadedAt = bootstrapAt;
        this.lastSavedAt = lastSaveAt;
        this.lastSavedHash = hash;
    }

    public synchronized void markSaved(Instant at, String hash) {
        this.lastSavedAt = at;
        this.lastSavedHash = hash;
        this.initialized = true;
    }

    public synchronized Instant lastLoadedAt() {
        return lastLoadedAt;
    }

    public synchronized Instant lastSavedAt() {
        return lastSavedAt;
    }

    public synchronized boolean isDirty(String currentHash) {
        return lastSavedHash == null || !lastSavedHash.equals(currentHash);
    }

    public synchronized int schemaVersion() {
        return schemaVersion;
    }

    public synchronized void updateSchemaVersion(int schemaVersion) {
        this.schemaVersion = schemaVersion;
    }

    public synchronized List<String> availableModules() {
        return availableModules;
    }

    public synchronized void updateAvailableModules(List<String> availableModules) {
        this.availableModules = availableModules == null || availableModules.isEmpty()
                ? DEFAULT_MODULES
                : List.copyOf(availableModules);
    }

    public synchronized boolean initialized() {
        return initialized;
    }
}
