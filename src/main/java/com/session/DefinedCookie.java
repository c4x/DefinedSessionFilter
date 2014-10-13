package com.session;

import javax.servlet.http.Cookie;
//纯粹为了以后方便扩展
public class DefinedCookie extends Cookie {

	public DefinedCookie(String name, String value) {
		super(name, value);
	}

}
