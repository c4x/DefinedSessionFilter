package com.session;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

public class DefinedSessionServletResponse extends HttpServletResponseWrapper {
	private DefinedSession session;

	public DefinedSessionServletResponse(HttpServletResponse response) {
		super(response);
	}

	public DefinedSession getSession() {
		return session;
	}

	public void setSession(DefinedSession session) {
		this.session = session;
	}
}
