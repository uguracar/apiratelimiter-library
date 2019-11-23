package com.coda.apiratelimiter.configuration;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.util.HashSet;
import java.util.List;

@Configuration
public class RedisConfiguration {

    @Value("${spring.redis.port}")
    private int redisPort;

    /*@SuppressWarnings("deprecation")
	@Bean
    public JedisConnectionFactory jedisConnectionFactory(RedisProperties redisProperties) {
    	List<String> nodes = redisProperties.getSentinel().getNodes();
    	//String[] nodes = (String[]) nodesStr.toArray();
    	//System.out.println("Redis nodes" + nodes);
    	//new HashSet<>(nodesStr);
        //String[] nodes = redisProperties.getSentinel().getNodes().split(",");
        RedisSentinelConfiguration config = new RedisSentinelConfiguration(redisProperties.getSentinel().getMaster(), new HashSet<>(nodes));
        JedisConnectionFactory jedisConnectionFactory = new JedisConnectionFactory(config);
        jedisConnectionFactory.setPassword(redisProperties.getPassword());
        return jedisConnectionFactory;
    }*/

    @SuppressWarnings("deprecation")
	@Bean
    public JedisConnectionFactory jedisConnectionFactory() {
        JedisConnectionFactory jedisConnectionFactory = new JedisConnectionFactory();
        jedisConnectionFactory.setPort(redisPort);
        return jedisConnectionFactory;
    }

    @Bean
    public RedisTemplate<String, String> redisTemplate(JedisConnectionFactory jedisConnectionFactory) {
        RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(jedisConnectionFactory);
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());
        return redisTemplate;
    }
}