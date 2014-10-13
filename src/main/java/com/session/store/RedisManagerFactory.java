package com.session.store;

import org.apache.log4j.Logger;

import com.session.config.RedisConfig;
import com.session.log.SessionLogger;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class RedisManagerFactory {
	private static final Logger logger = Logger
			.getLogger(RedisManagerFactory.class);
	private static JedisPool jedisPool;
	// private static Jedis jedis;
	private boolean init = false;

	// 初始化redispool
	public RedisManagerFactory(RedisConfig redisConfig) {
		try {
			if (jedisPool == null) {
				iniPool(redisConfig);
			}
		} catch (Exception e) {
			logger.error(
					"jedis pool init error:host--->" + redisConfig.getHost()
							+ ";port--->" + redisConfig.getPort() + ";db--->"
							+ redisConfig.getDb(), e);
		}
	}

	public void iniPool(RedisConfig redisConfig) {
		try {
			if (!init) {
				jedisPool = new JedisPool(redisConfig.getPoolConfig(),
						redisConfig.getHost(), redisConfig.getPort(), 2, null,
						redisConfig.getDb(), null);
				init = true;
			}
		} catch (Exception e) {
			logger.error(
					"jedis pool init error:poolconfig--->"
							+ redisConfig.getPoolConfig().toString()
							+ ";host--->" + redisConfig.getHost() + ";port--->"
							+ redisConfig.getPort() + ";db--->"
							+ redisConfig.getDb(), e);
		}
	}

	// public void init(RedisConfig redisConfig){
	// try{
	// jedis = new Jedis(redisConfig.getHost(),redisConfig.getPort(),2);
	// jedis.select(redisConfig.getDb());
	// init = true;
	// }catch(Exception e){
	// logger.error("jedis pool init error:host--->"+redisConfig.getHost()+";port--->"+redisConfig.getPort()+";db--->"+redisConfig.getDb());
	// logger.error(e.getMessage());
	// }
	// }
	public boolean isInit() {
		return init;
	}

	public Jedis getInstance() {
		if (jedisPool != null) {
			return jedisPool.getResource();
		} else {
			return null;
		}
	}

	public void setInstance(Jedis jedis) {
		jedisPool.returnResource(jedis);
	}
}
