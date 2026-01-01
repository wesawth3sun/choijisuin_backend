package com.finance.domain.external;

import java.time.Duration;

public interface RedisClient {
    String get(String key);
    void set(String key, String value, Duration time);
}
