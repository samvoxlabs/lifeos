package com.familyos.familyos.storage;

public interface StorageService {
    StorageStatus bootstrap(String userId);
    StorageStatus save(String userId);
    StorageStatus status(String userId);
}
