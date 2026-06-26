package com.mojian.idempotent.config;

import com.mojian.idempotent.aspectj.RepeatSubmitAspect;
import com.mojian.utils.RedisUtil;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConfiguration;

/**
 * 幂等功能配置
 *
 * @author Lion Li
 */
@AutoConfiguration(after = RedisConfiguration.class)
public class IdempotentConfig {

    @Bean
    public RepeatSubmitAspect repeatSubmitAspect(RedisUtil redisUtil) {
        return new RepeatSubmitAspect(redisUtil);
    }

}
