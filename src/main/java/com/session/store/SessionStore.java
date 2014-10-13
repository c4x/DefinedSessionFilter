package com.session.store;

import org.apache.commons.configuration.PropertiesConfiguration;
import com.session.DefinedSessionServletRequest;
import com.session.DefinedSessionServletResponse;
import com.session.config.AbstractConfig;

public interface SessionStore {
	 /**
     * 获取属性值
     *
     * @param key
     * @return
     */
    public Object getAttribute(String key);

    /**
     * 设置属性值
     *
     * @param key
     * @param value 为null时表示删除
     */
    public void setAttribute(String key, Object value);

    /**
     * 初始化
     *
     * @param session
     */
    public void init(PropertiesConfiguration config,DefinedSessionServletRequest request,
			DefinedSessionServletResponse response,AbstractConfig abstractConfig);

	public void init(PropertiesConfiguration config,
			AbstractConfig abstractConfig);
	public void commit(String token);
}
