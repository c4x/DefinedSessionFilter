package com.session;

import java.util.Enumeration;
import java.util.Random;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.session.config.CookieConfig;
import com.session.config.RedisConfig;
import com.session.log.SessionLogger;
import com.session.store.CookieStore;
import com.session.store.RedisStore;
import com.session.util.Base64Utils;
import com.session.util.UniqID;
import com.session.util.UserCheckUtil;

@SuppressWarnings("deprecation")
public class DefinedSession implements HttpSession {
	 private static final Logger logger = SessionLogger.getSessionLogger();
	 private static final Logger sessionIdLogger = SessionLogger
	 .getSessionIdLogger();
	public static final String SESSION_ID = KeyConstants.ATTRIBUTE_SESSION_ID;
	public static final String TOKEN = KeyConstants.ATTRIBUTE_TOKEN;
	private int maxInactiveInterval = 1800;
	private PropertiesConfiguration config;
	private CookieConfig cookieConfig;
	private RedisConfig redisConfig;
	private CookieStore cookieStore;
	private RedisStore redisStore;
	private String sessionId;
	private ServletContext context;
	private long creationTime;
	private DefinedSessionServletRequest request;
	private DefinedSessionServletResponse response;

	public DefinedSession() {
	};

	public DefinedSession(DefinedSessionServletRequest request,
			DefinedSessionServletResponse response, ServletContext context,
			PropertiesConfiguration config, CookieConfig cookieConfig,
			RedisConfig redisConfig) {
		this.creationTime = System.currentTimeMillis();
		this.request = request;
		this.response = response;
		this.context = context;
		this.config = config;
		this.redisConfig = redisConfig;
		this.cookieConfig = cookieConfig;
	}

	public CookieStore getCookieStore() {
		return cookieStore;
	}

	public RedisStore getRedisStore() {
		return redisStore;
	}

	public void init() {
		// 初始化store
		initStores();
		// 先获取或者生成一个sessionid
		fetchSessionId();
		redisStore.setKey(sessionId);
	}

	public void commit() {
		String value = System.currentTimeMillis() + new Random().nextInt() + "";
		String token = Base64Utils.encodeBase64(value);
		cookieStore.commit(token);
		// redis提交
		redisStore.commit(token);
	}

	/*
	 * 根据cookie获取sessionid，没取到则生成一个sessionid
	 */
	private void fetchSessionId() {
		sessionId = (String) getAttribute(SESSION_ID);
		if (UserCheckUtil.domainCheck(request, config)) {
			if (StringUtils.isBlank(sessionId)) {
				generateSessionId();
				setAttribute(SESSION_ID, sessionId);
			}
		}

	}

	private String generateSessionId() {
		boolean duplicate = true;
		String uniqID = null;
		while (duplicate) {
			uniqID = UniqID.getInstance().getUniqID();
			sessionId = DigestUtils.md5Hex(uniqID);
			try {
				if (redisStore.isSessionIdUsed(sessionId)) {
					continue;
				} else {
					duplicate = false;
					break;
				}
			} catch (Exception e) {
				// e.printStackTrace();
				sessionIdLogger.error("GenerateSessionId failure: " + " generateSessionId="
						+ sessionId + " ip=" + getRequest().getRemoteAddr(), e);
			}
		}
		return sessionId;
	}

	/*
	 * 初始化store
	 */
	private void initStores() {
		cookieStore = new CookieStore();
		cookieStore.init(config, request, response, cookieConfig);
		redisStore = new RedisStore();
		redisStore.init(config, redisConfig);
	}

	public DefinedSessionServletRequest getRequest() {
		return request;
	}

	public DefinedSessionServletResponse getResponse() {
		return response;
	}

	@Override
	public long getCreationTime() {
		return this.creationTime;
	}

	@Override
	public String getId() {
		return this.sessionId;
	}

	@Override
	public long getLastAccessedTime() {
		return this.creationTime;
	}

	@Override
	public ServletContext getServletContext() {
		return this.context;
	}

	@Override
	public void setMaxInactiveInterval(int interval) {
		this.maxInactiveInterval = interval;
	}

	@Override
	public int getMaxInactiveInterval() {
		return this.maxInactiveInterval;
	}

	@Deprecated
	public HttpSessionContext getSessionContext() {
		throw new UnsupportedOperationException();
	}

	/*
	 * 获取属性，先从cookie中找，cookie找不到则从redis中取
	 */
	@Override
	public Object getAttribute(String name) {
		Object value = null;
		if (UserCheckUtil.domainCheck(request, config)) {
			// 先从cookie中取，取到直接返回
			value = cookieStore.getAttribute(name);
			if (value != null) {
				return value;
			} else {
				try {
					value = redisStore.getAttribute(name);
				} catch (Exception e) {
					// // 只有redis可能出错
					logger.error("Redis read failure:" + " sessionid="
							+ sessionId, e);
					// e.printStackTrace();
					return value;
				}
			}
		}
		return value;
	}

	@Override
	public Object getValue(String name) {
		return getAttribute(name);
	}

	// 该方法让其失效因为attributeName会被覆盖，取出所有的意义不大
	@SuppressWarnings("rawtypes")
	@Override
	public Enumeration getAttributeNames() {
		return null;
	}

	// 该方法让其失效因为attributeName会被覆盖，取出所有的意义不大
	@Override
	public String[] getValueNames() {
		return null;
	}

	/*
	 * 设置属性，全部暂存在store的map中，等待commit
	 */
	@Override
	public void setAttribute(String name, Object value) {
		cookieStore.setAttribute(name, value);
		try {
			redisStore.setAttribute(name, value);
		} catch (Exception e) {
			logger.error("setAttribute to redis failure:" + "key=" + name
					+ "value=" + value.toString() + "sessionid=" + sessionId, e);
			// e.printStackTrace();
		}
	}

	@Override
	public void putValue(String name, Object value) {
		setAttribute(name, value);
	}

	@Override
	public void removeAttribute(String name) {
		setAttribute(name, null);
	}

	@Override
	public void removeValue(String name) {
		setAttribute(name, null);
	}

	@Override
	public void invalidate() {

	}

	@Override
	public boolean isNew() {
		return false;
	}

}
