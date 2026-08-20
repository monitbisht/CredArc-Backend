package com.credarc.credarc.redis;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class CacheEvictionService {

    @CacheEvict(value = "userAccounts", key = "#userId")
    public void evictAccountDetailsCache(UUID userId){}
}
