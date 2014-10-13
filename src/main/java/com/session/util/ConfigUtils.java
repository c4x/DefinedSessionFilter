package com.session.util;

import org.apache.commons.configuration.PropertiesConfiguration;

import com.session.config.CookieConfig;
import com.session.config.RedisConfig;

public class ConfigUtils {
	public static void fetchJedisConfig(RedisConfig redisConfig,PropertiesConfiguration config){
		redisConfig.fetchConfig(config);
	}
	public static void fetchCookieConfig(CookieConfig cookieConfig,PropertiesConfiguration config){
		cookieConfig.fetchConfig(config);
	}
}
