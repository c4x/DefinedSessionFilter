package com.session;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpSession;

public class DefinedSessionServletRequest extends HttpServletRequestWrapper {
	private DefinedSession session;
	public DefinedSessionServletRequest(HttpServletRequest request) {
		super(request);
	}
	
	public void setSession(DefinedSession session){
		this.session = session;
	}
	
	@Override
	public HttpSession getSession(){
		return this.session;
	}

}
