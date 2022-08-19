package com.bocloud.dfs.component;


public abstract class AbstractStorageService implements StorageService {


    public AbstractStorageService(String storageServiceType) {
        StorageServiceFactory.registerStorageServiceForFactory(storageServiceType, this);
    }

}
