package com.session.config;

import org.apache.commons.configuration.PropertiesConfiguration;

import com.session.exception.IllegalConfigException;

public class CookieConfig extends AbstractConfig{
	private String domain;
	private String path;
	private int maxAge;
	private boolean cookieTrace;
	private boolean compress;
	private String compressKey;
	private boolean encrypt;
	private String blowfishKey;
	private boolean base64;
	public void fetchConfig(PropertiesConfiguration config){
		if(config != null){
			this.domain = config.getString("cookie.domain","duc.cn");
			this.path = config.getString("cookie.path","/");
			this.maxAge = config.getInt("cookie.maxAge",3600);
			this.cookieTrace = config.getBoolean("cookie.trace",false);
			this.compress = config.getBoolean("cookie.compress",false);
			this.encrypt = config.getBoolean("cookie.encrypt",false);
			this.blowfishKey = config.getString("blowfish.cipherKey");
			this.base64 = config.getBoolean("cookie.base64",false);
			this.compressKey = config.getString("cook.compressKey","duc_c_key");
		}else{
			//throw config no found or parse exception
			throw new IllegalConfigException("PropertiesConfig get error,<param>sessionConfig</param> must not null");
		}
	}
	public String getCompressKey() {
		return compressKey;
	}
	public boolean isBase64() {
		return base64;
	}
	public String getBlowfishKey() {
		return blowfishKey;
	}
	public boolean isEncrypt() {
		return encrypt;
	}
	public boolean isCompress() {
		return compress;
	}
	public boolean isCookieTrace() {
		return cookieTrace;
	}
	public String getDomain() {
		return domain;
	}
	public void setDomain(String domain) {
		this.domain = domain;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public int getMaxAge() {
		return maxAge;
	}
	public void setMaxAge(int maxAge) {
		this.maxAge = maxAge;
	}
}
