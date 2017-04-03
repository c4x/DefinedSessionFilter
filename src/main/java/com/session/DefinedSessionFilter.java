package com.session;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.session.config.CookieConfig;
import com.session.config.RedisConfig;
import com.session.log.SessionLogger;
import com.session.store.CookieStore;
import com.session.store.RedisStore;
import com.session.util.ConfigUtils;
import com.session.util.UserCheckUtil;

public class DefinedSessionFilter implements Filter {
	private static final Logger logger = SessionLogger.getSessionLogger();
	// private static final Logger logger =
	// Logger.getLogger(DefinedSessionFilter.class);
	private static boolean isLoading = false;
	private PropertiesConfiguration config;
	private static RedisConfig redisConfig;
	private static CookieConfig cookieConfig;
	private static final String SESSION_CONFIG = "sessionConfig";
	private FilterConfig filterConfig;

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		this.filterConfig = filterConfig;
		String propertiesFile = filterConfig.getInitParameter(SESSION_CONFIG);
		String realPath = filterConfig.getServletContext().getRealPath(
				propertiesFile);

		try {
			if (!isLoading) {
				config = new PropertiesConfiguration(realPath);
				redisConfig = new RedisConfig();
				ConfigUtils.fetchJedisConfig(redisConfig, config);
				cookieConfig = new CookieConfig();
				ConfigUtils.fetchCookieConfig(cookieConfig, config);
				isLoading = true;
			}
		} catch (Exception e) {
			// e.printStackTrace();
			logger.error("load properties error:", e);
		}
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		DefinedSessionServletRequest definedSessionServletRequest = null;
		DefinedSessionServletResponse definedSessionServletResponse = null;
		DefinedSession session = null;
		try {
			request = new DefinedSessionServletRequest(
					(HttpServletRequest) request);
			response = new DefinedSessionServletResponse(
					(HttpServletResponse) response);
			session = createDefinedSession(definedSessionServletRequest, definedSessionServletResponse);
			definedSessionServletRequest.setSession(session);
			definedSessionServletResponse.setSession(session);
			boolean isDomainEqual = UserCheckUtil.domainCheck(definedSessionServletRequest,
					config);
			if (isDomainEqual) {
				checkLoginCookie(session);
				updateReidsExpired(session);
			}
			try {
				chain.doFilter(request, response);
				if (null != session) {
					session.commit();
				}
			} catch (Exception e) {
				// e.printStackTrace();
				logger.error("continue filter and sesion commit error:", e);
			}
		} catch (Exception e) {
			// e.printStackTrace();
			logger.error("session cookie check and session init failure:", e);
		}

	}

	private void updateReidsExpired(DefinedSession session) {
		RedisStore redisStore = session.getRedisStore();
		redisStore.updateExpire(System.currentTimeMillis()
				+ redisConfig.getExpiredTime() * 1000);
	}

	private void checkLoginCookie(DefinedSession session) {
		String login = (String) session.getAttribute("login");
		RedisStore redisStore = session.getRedisStore();
		CookieStore cookieStore = session.getCookieStore();
		boolean isSameUser = true;
		// 判断是否登录
		if ("true".equals(login)) {
			String nickFromRedis = (String) redisStore.getAttribute("_nk_");
			String nickFromCookie = (String) cookieStore.getAttribute("_nk_");
			// 已登录判断nick是否相同
			if (!StringUtils.equals(nickFromRedis, nickFromCookie)) {
				isSameUser = false;
				logger.warn("clear cookie: " + " ip="
						+ session.getRequest().getRemoteAddr() + " sessionID="
						+ session.getId() + " nickFromRedis=" + nickFromRedis
						+ " nickFromCookie=" + nickFromCookie);
			} else {
				// nick相同的情况下判断生成的token，防止被篡改
				String tokenFromRedis = (String) redisStore
						.getAttribute("_token_");
				String tokenFromCookie = (String) cookieStore
						.getAttribute("_token_");
				if (!StringUtils.equals(tokenFromRedis, tokenFromCookie)) {
					isSameUser = false;
					logger.warn("clear cookie: " + " ip="
							+ session.getRequest().getRemoteAddr()
							+ " sessionID=" + session.getId()
							+ " tokenFromCookie=" + tokenFromCookie
							+ " tokenFromRedis=" + tokenFromRedis);
				}
			}

		}
		// 验证失败，清楚cookie
		if (!"true".equals(login) || !isSameUser) {
			session.setAttribute("_nk_", null);
			session.setAttribute("userID", null);
			session.setAttribute("login", null);
			session.setAttribute("_token_", null);
			session.setAttribute("_l_g_", null);

			Cookie cookie = new Cookie("_nk_", null);
			cookie.setMaxAge(0);
			cookie.setPath("/");
			session.getResponse().addCookie(cookie);

			cookie = new Cookie("userID", null);
			cookie.setMaxAge(0);
			cookie.setPath("/");
			session.getResponse().addCookie(cookie);

			cookie = new Cookie("login", null);
			cookie.setMaxAge(0);
			cookie.setPath("/");
			session.getResponse().addCookie(cookie);

			cookie = new Cookie("_token_", null);
			cookie.setMaxAge(0);
			cookie.setPath("/");
			session.getResponse().addCookie(cookie);

			cookie = new Cookie("_l_g_", null);
			cookie.setMaxAge(0);
			cookie.setPath("/");
			session.getResponse().addCookie(cookie);
		}
	}

	private DefinedSession createDefinedSession(DefinedSessionServletRequest request,
			DefinedSessionServletResponse response) {
		DefinedSession session = null;
		try {
			session = new DefinedSession(request, response,
					filterConfig.getServletContext(), config, cookieConfig,
					redisConfig);
			session.init();
		} catch (Exception e) {
			// e.printStackTrace();
			logger.error(
					"DefinedSessionFilter-createDefinedSession failure: "
							+ e.toString(), e);
		}
		return session;
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub

	}

}
