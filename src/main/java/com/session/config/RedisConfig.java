package com.session.config;

import org.apache.commons.configuration.PropertiesConfiguration;

import redis.clients.jedis.JedisPoolConfig;

import com.session.exception.IllegalConfigException;


public class RedisConfig extends AbstractConfig{
	private JedisPoolConfig poolConfig ;
	private String host;
	private int port;
	private int db;
	private String nameGroup;
	private int maxExpiredTime; //redis 失效时间
	
	public void fetchConfig(PropertiesConfiguration config){
		if(config!=null){
			poolConfig = new JedisPoolConfig();
			int maxIdle = config.getInt("redis.maxIdle",5);
			//控制一个pool最多有多少个状态为idle(空闲的)的jedis实例。
			poolConfig.setMaxIdle(maxIdle);
			int maxWait = config.getInt("redis.maxWaitMillis",1000*1000);
			poolConfig.setMaxWaitMillis(maxWait);
			int maxTotal = config.getInt("redis.maxTotal",10);
			poolConfig.setMaxTotal(maxTotal);
			boolean test = config.getBoolean("redis.TestOnBorrow",true);
            //在borrow一个jedis实例时，是否提前进行validate操作；如果为true，则得到的jedis实例均是可用的；
			poolConfig.setTestOnBorrow(test);
			this.host = config.getString("redis.host","127.0.0.1");
			this.port = config.getInt("redis.prot",6379);
			this.db = config.getInt("redis.db",0);
			this.maxExpiredTime = config.getInt("redis.expiredTime",4500);
			String nameGroup = config.getString("redis.nameGroup");
			if(nameGroup!=null && !"".equals(nameGroup)){
				this.nameGroup = nameGroup;
			}else{
				// throw namegroup exception
				throw new IllegalConfigException("namegroup must not null");
			}
		}else{
			//throw config no found or parse exception
			throw new IllegalConfigException("PropertiesConfig get error,<param>sessionConfig</param> must not null");
		}
	}

	public JedisPoolConfig getPoolConfig() {
		return poolConfig;
	}

	public String getHost() {
		return host;
	}

	public int getPort() {
		return port;
	}

	public int getDb() {
		return db;
	}

	public String getNameGroup() {
		return nameGroup;
	}
	
	public int getExpiredTime(){
		return this.maxExpiredTime;
	}

}
