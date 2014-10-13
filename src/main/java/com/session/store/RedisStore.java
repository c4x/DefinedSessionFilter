package com.session.store;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;

import com.session.DefinedSessionServletRequest;
import com.session.DefinedSessionServletResponse;
import com.session.config.RedisConfig;
import com.session.config.AbstractConfig;
import com.session.log.SessionLogger;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;

public class RedisStore implements SessionStore {
	private RedisConfig redisConfig;
	private RedisManagerFactory redisFactory;
	private Map<String, String> attributes;
	private String key;
	 private static final Logger logger = SessionLogger.getRedisLogger();

	@Override
	public Object getAttribute(String field) {
		String value = attributes.get(field);
		if (value == null) {
			if (key != null) {
				readFromRedis();
				value = attributes.get(field);
			} else {
				return null;
			}
		}
		return value;
	}

	private void readFromRedis() {
		try {
			Jedis jedis = redisFactory.getInstance();
			attributes = jedis.hgetAll(key);
			redisFactory.setInstance(jedis);
		} catch (Exception e) {
			logger.error("read from redis failure:" + "key=" + key, e);
		}
	}

	public void writeToRedis(String field, String value) {
		try {
			Jedis jedis = redisFactory.getInstance();
			jedis.hset(key, field, value);
			redisFactory.setInstance(jedis);
		} catch (Exception e) {
			logger.error("write field redis failure:" + "key=" + key + "field="
					+ field + "value=" + value, e);
		}
	}

	public void commit(String token) {
		attributes.put("_token_", token);
		try {
			Jedis jedis = redisFactory.getInstance();
			Pipeline pipeline = jedis.pipelined();
			Iterator<Entry<String, String>> it = attributes.entrySet()
					.iterator();
			while (it.hasNext()) {
				Entry<String, String> entry = it.next();
				if (entry.getValue() != null && !"".equals(entry.getValue())) {
					pipeline.hset(key, entry.getKey(), entry.getValue());
				}
			}
			pipeline.sync();
			long commitTime = System.currentTimeMillis();
			jedis.expireAt(key, commitTime
					+ (redisConfig.getExpiredTime() * 1000));
			redisFactory.setInstance(jedis);
		} catch (Exception e) {
			logger.error("write all to redis failure:" + "key=" + key, e);
		}
	}

	@Override
	public void setAttribute(String key, Object value) {
		if (value != null) {
			String v = value.toString();
			attributes.put(key, v);
		} else {
			removeKey(key);
		}
	}

	private void removeKey(String field) {
		attributes.remove(field);
		try {
			Jedis jedis = redisFactory.getInstance();
			jedis.hdel(key, field);
			redisFactory.setInstance(jedis);
		} catch (Exception e) {
			logger.error("remove field of redis failure:" + "key=" + key
					+ "field =" + field, e);
		}
	}

	@Override
	public void init(PropertiesConfiguration config,
			AbstractConfig abstractConfig) {
		attributes = new HashMap<String, String>();
		redisConfig = (RedisConfig) abstractConfig;
		// ConfigUtils.fetchJedisConfig(redisConfig,
		// config);filter中fetch掉2个static的config出来
		if (redisFactory != null) {
			if (!redisFactory.isInit()) {
				redisFactory.iniPool(redisConfig);
			}
		} else {
			redisFactory = new RedisManagerFactory(redisConfig);
		}
	}

	public boolean isSessionIdUsed(String sessionId) {
		Jedis jedis = redisFactory.getInstance();
		return jedis.exists(sessionId + ":" + redisConfig.getNameGroup());
	}

	// 疑问，是否会更新时间。。。。。
	public void updateExpire(Long expireTime) {
		Jedis jedis = redisFactory.getInstance();
		jedis.expireAt(key, expireTime);
	}

	/*
	 * 不做任何事情，该方法预留给初始化cookie
	 */
	@Override
	public void init(PropertiesConfiguration config,
			DefinedSessionServletRequest request,
			DefinedSessionServletResponse response, AbstractConfig abstractConfig) {
	}

	public void setKey(String sessionId) {
		key = sessionId + ":" + redisConfig.getNameGroup();
	}

}
