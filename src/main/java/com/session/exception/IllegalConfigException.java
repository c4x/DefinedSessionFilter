package com.session.exception;

public class IllegalConfigException extends RuntimeException {
	 	private static final long serialVersionUID = 1L;

	    public IllegalConfigException() {
	        super();
	    }

	    public IllegalConfigException(String message, Throwable cause) {
	        super(message, cause);
	    }

	    public IllegalConfigException(String message) {
	        super(message);
	    }

	    public IllegalConfigException(Throwable cause) {
	        super(cause);
	    }
}
