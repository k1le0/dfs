package com.bocloud.dfs.component;

import org.springframework.util.CollectionUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class StorageServiceFactory {


    private static final ConcurrentHashMap<String, StorageService> STORAGE_SERVICES = new ConcurrentHashMap<>();

    public static StorageService getStorageServiceClass(String storageServiceType) {
        StorageService storageService = null;
        if (CollectionUtils.isEmpty(STORAGE_SERVICES)) {
            return null;
        }
        for (Map.Entry<String, StorageService> ss : STORAGE_SERVICES.entrySet()) {
            if (ss.getKey().equalsIgnoreCase(storageServiceType)) {
                storageService = ss.getValue();
                break;
            }
        }
        return storageService;
    }


    public static void registerStorageServiceForFactory(String storageServiceType, StorageService storageService) {
        STORAGE_SERVICES.put(storageServiceType, storageService);
    }
}
