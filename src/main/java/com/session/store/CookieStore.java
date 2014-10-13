package com.session.store;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.Cookie;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.session.DefinedCookie;
import com.session.DefinedSessionFilter;
import com.session.DefinedSessionServletRequest;
import com.session.DefinedSessionServletResponse;
import com.session.config.CookieConfig;
import com.session.config.AbstractConfig;
import com.session.exception.IllegalConfigException;
import com.session.log.SessionLogger;
import com.session.util.Base64Utils;
import com.session.util.BlowfishUtils;
import com.session.util.ConfigUtils;

public class CookieStore implements SessionStore {
	private static final Logger logger = Logger.getLogger(CookieStore.class);
	private String sessionId;
	private CookieConfig cookieConfig;
	public static final String COMBINE_SEPARATOR = "&";
	public static final String KEY_VALUE_SEPARATOR = "=";
	private static final String URL_ENCODING = "UTF-8";
	private DefinedSessionServletRequest request;
	private DefinedSessionServletResponse response;

	/**
	 * 存放未解析的属性
	 */
	private Map<String, String> cookies;
	/**
	 * 存放解析的属性
	 */
	private Map<String, String> attributes;

	/*
	 * 添加到response中去
	 */
	private void addCookieToResponse(String name, String value) {
		String domain = cookieConfig.getDomain();
		String path = cookieConfig.getPath();
		int maxAge = cookieConfig.getMaxAge();
		DefinedCookie cookie = buildCookie(name, value, domain, path, maxAge);
		response.addCookie(cookie);
	}

	/*
	 * 创建cookie
	 */
	private DefinedCookie buildCookie(String name, String value, String domain,
			String path, int maxAge) {
		DefinedCookie cookie = new DefinedCookie(name, value);
		if (StringUtils.isNotBlank(domain)) {
			cookie.setDomain(domain);
		}
		if (StringUtils.isNotBlank(path)) {
			cookie.setPath(path);
		} else {
			cookie.setPath(cookieConfig.getDomain());
		}
		cookie.setMaxAge(maxAge);
		return cookie;
	}

	/*
	 * 获取属性，解码之后全部存放在attributes中
	 */
	@Override
	public Object getAttribute(String key) {
		String value = attributes.get(key);
		if (value == null) {
			decodeCookies(key);
			value = attributes.get(key);
		}
		if (cookieConfig.isCookieTrace()) {
			logger.warn("read cookie:" + "sid=" + sessionId + "name=" + key
					+ "value=" + value);
		}
		return value;
	}

	/*
	 * 解码cookie
	 */
	private void decodeCookies(String key) {
		if (cookieConfig.isCompress()) {
			decodeCompressCookie(key);
		} else {
			decodeSingleCookie(key);
		}
	}

	private void decodeSingleCookie(String key) {
		String cookieValue = cookies.get(key);
		attributes.put(key, decodeValue(cookieValue));
	}

	private void decodeCompressCookie(String key) {
		String cookieValue = cookies.get(key);
		Map<String, String> separateCookies = separateCookies(cookieValue);
		if (separateCookies != null) {
			Iterator<Entry<String, String>> it = separateCookies.entrySet()
					.iterator();
			while (it.hasNext()) {
				Entry<String, String> pairs = it.next();
				attributes.put(pairs.getKey(), decodeValue(pairs.getValue()));
			}
		} else {
			logger.error("separateCookies error: sid= " + sessionId
					+ " cookieValue=" + cookieValue);
		}
	}

	private String decodeValue(String value) {
		if (value != null) {
			try {
				value = URLDecoder.decode(value, URL_ENCODING);
			} catch (Exception e) {
				logger.error("utf-8 decode error: " + " value=" + value
						+ " configEntry=" + cookieConfig.toString(), e);
				// 解码失败时直接返回，不继续解析
				return value;
			}
			try {
				value = StringEscapeUtils.unescapeJava(value);
			} catch (Exception e) {
				logger.error("cookie unescapeJava error: " + " value=" + value
						+ "configEntry=" + cookieConfig.toString(), e);
				// 特殊字符逆向转义失败直接返回，不继续解析
				return value;
			}
			if (cookieConfig.isEncrypt()) {
				value = BlowfishUtils.decryptBlowfish(value, getBlowfishKey());
				if (cookieConfig.isBase64()) {
					value = Base64Utils.removeBase64Head(value);
				}
			} else if (cookieConfig.isBase64()) {
				value = Base64Utils.decodeBase64(value);
			}
		}
		return value;
	}

	private String getBlowfishKey() {
		String key = cookieConfig.getBlowfishKey();
		if (StringUtils.isNotBlank(key)) {
			// blowfish key need
			if (StringUtils.indexOf(key, "=TAOBAO=") == -1
					&& !"taobao123".equals(key)) {
				key = BlowfishUtils.decryptBlowfish(key, "SEDe%&SDF*");
			}
			return key;
		} else {
			throw new IllegalConfigException("必须指定blowfish.cipherKey属性");
		}
	}

	private Map<String, String> separateCookies(String cookieValue) {
		Map<String, String> separateCookies = new HashMap<String, String>();

		String[] contents = StringUtils.split(cookieValue, COMBINE_SEPARATOR);
		if (contents != null && contents.length > 0) {
			for (String content : contents) {
				String[] keyValue = StringUtils.split(content,
						KEY_VALUE_SEPARATOR, 2);
				if (keyValue.length == 2) {
					String key = keyValue[0];
					String value = keyValue[1];
					separateCookies.put(key, value);
				}
			}
		}

		return separateCookies;
	}

	/*
	 * 设置属性，首先存放在attributes中
	 */
	@Override
	public void setAttribute(String key, Object value) {
		if (value != null) {
			String v = value.toString();
			attributes.put(key, v);
		} else {
			attributes.put(key, null);
		}

	}

	/*
	 * 统一将属性写出到response中去，根据cookie的设置，确定是否压缩
	 */
	public void commit(String token) {
		// 设置cookie的最后使用时间
		attributes.put("_token_", token);
		if (cookieConfig.isCompress()) {
			encodeCompressCookie();
		} else {
			Iterator<Entry<String, String>> it = attributes.entrySet()
					.iterator();
			while (it.hasNext()) {
				Entry<String, String> pairs = it.next();
				String key = pairs.getKey();
				encodeSingleCookie(key);
			}
		}
	}

	private void encodeCompressCookie() {
		StringBuilder compressBuilder = new StringBuilder();
		Iterator<Entry<String, String>> it = attributes.entrySet().iterator();
		boolean first = true;
		while (it.hasNext()) {
			Entry<String, String> pairs = it.next();
			String key = pairs.getKey();
			String value = pairs.getValue();
			value = encodeValue(value);
			if (value == null || "ERROR".equals(value)) { // 本属性已被删除或编码错误时忽略当前属性
				continue;
			}
			if (first) {
				first = false;
			} else {
				compressBuilder.append(COMBINE_SEPARATOR);
			}
			compressBuilder.append(key).append(KEY_VALUE_SEPARATOR)
					.append(value);
		}
		addCookieToResponse(cookieConfig.getCompressKey(),
				compressBuilder.toString());
	}

	public void encodeSingleCookie(String key) {
		String value = attributes.get(key);
		value = encodeValue(value);
		if ("ERROR".equals(value)) { // 编码错误时忽略当前cookie
			return;
		}
		addCookieToResponse(key, value);
	}

	public String encodeValue(String value) {
		if (StringUtils.isEmpty(value)) {
			return value;
		}
		// 转义特殊字符
		value = StringEscapeUtils.escapeJava(value);
		if (cookieConfig.isEncrypt()) {
			if (cookieConfig.isBase64()) {
				value = Base64Utils.addBase64Head(value);
			}
			value = BlowfishUtils.encryptBlowfish(value, getBlowfishKey());
		} else if (cookieConfig.isBase64()) {
			value = Base64Utils.encodeBase64(value);
		}
		try {
			// 总是编码
			value = URLEncoder.encode(value, URL_ENCODING);
		} catch (Exception e) {
			logger.error("utf-8 encode error: " + " value=" + value
					+ " cookieConfig=" + cookieConfig.toString(), e);
			// 编码失败时，返回错误标记，且不保存到cookies中
			return "ERROR";
		}
		return value;
	}

	/*
	 * 初始化cookiestore类，根据当前的sessionid、request、response
	 */
	@Override
	public void init(PropertiesConfiguration config,
			DefinedSessionServletRequest request,
			DefinedSessionServletResponse response, AbstractConfig abstractConfig) {
		this.request = request;
		this.response = response;
		cookieConfig = (CookieConfig) abstractConfig;
		cookieConfig.fetchConfig(config);
		cookies = new HashMap<String, String>();
		attributes = new HashMap<String, String>();
		fetchCookies();
	}

	private void fetchCookies() {
		Cookie[] cookies = request.getCookies();
		if (cookies != null && cookies.length > 0) {
			for (Cookie cookie : cookies) {
				this.cookies.put(cookie.getName(), cookie.getValue());
			}
		}
		if (cookieConfig.isCookieTrace()) {
			logger.warn("cookie from request header:" + "cookies="
					+ cookies.toString());
		}
	}

	/*
	 * 不做任何事情，该方法预留给初始化redis
	 */
	@Override
	public void init(PropertiesConfiguration config,
			AbstractConfig abstractConfig) {

	}
}
