package com.hww.cms.webservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.UnknownHostException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

@Configuration
@EnableCaching
public class RedisCacheConfig {
    @Bean
    public CacheManager cacheManager(RedisTemplate<?,?>redisTemplate) {

           CacheManager cacheManager =new RedisCacheManager(redisTemplate);
           return cacheManager;

    }
    
//    @Bean
//    public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory factory) {
//
//           RedisTemplate<String,String>redisTemplate = new RedisTemplate<String, String>();
//           redisTemplate.setConnectionFactory(factory);
//
//           //key序列化方式;（不然会出现乱码;）,但是如果方法上有Long等非String类型的话，会报类型转换错误；
//
//           //所以在没有自己定义key生成策略的时候，以下这个代码建议不要这么写，可以不配置或者自己实现ObjectRedisSerializer
//
//           //或者JdkSerializationRedisSerializer序列化方式;
//
////        RedisSerializer<String>redisSerializer = new StringRedisSerializer();//Long类型不可以会出现异常信息;
//
////        redisTemplate.setKeySerializer(redisSerializer);
//
////        redisTemplate.setHashKeySerializer(redisSerializer);
//           return redisTemplate;
//
//    }
    
    
//    
//  @Bean(name="redisTemplate")
//  public RedisTemplate<String, Object> readRedisTemplate(RedisConnectionFactory factory) {
//
//         RedisTemplate<String,Object>redisTemplate = new RedisTemplate<String, Object>();
//         redisTemplate.setConnectionFactory(factory);
//
//         //key序列化方式;（不然会出现乱码;）,但是如果方法上有Long等非String类型的话，会报类型转换错误；
//
//         //所以在没有自己定义key生成策略的时候，以下这个代码建议不要这么写，可以不配置或者自己实现ObjectRedisSerializer
//
//         //或者JdkSerializationRedisSerializer序列化方式;
//
////      RedisSerializer<String>redisSerializer = new StringRedisSerializer();//Long类型不可以会出现异常信息;
//
////      redisTemplate.setKeySerializer(redisSerializer);
//
////      redisTemplate.setHashKeySerializer(redisSerializer);
//         return redisTemplate;
//
//  }
//  @Autowired
//  WriteRedisAutoConfiguration.WriteRedisConnectionConfiguration writeRedisConnectionConfiguration; 
//  
//  @Bean(name="writeRedisTemplate")
//  public RedisTemplate<Object, Object> writeRedisTemplate(WriteRedisConnectionConfiguration writeRedisConnectionConfiguration) throws UnknownHostException {
//
//		WriteJedisConnectionFactory redisConnectionFactory=writeRedisConnectionConfiguration.writeRedisConnectionFactory();
//		RedisTemplate<Object, Object> template = new RedisTemplate<Object, Object>();
//		template.setConnectionFactory(redisConnectionFactory);
//		return template;
//
//  }
}
