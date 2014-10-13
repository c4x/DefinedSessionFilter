package com.session.log;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.DailyRollingFileAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

/**
 * xiaoxie 2011-3-29
 */
public class SessionLogger {
    private static final String GBK        		= "GBK";
    private static final String YYYY_MM_DD 		= "'.'yyyy-MM-dd";
    private static final String D_M_N      		= "%d{dd HH:m:ss} %m%n";
    private static final String DIR_NAME   		= "logs";
    private static final String USER_HOME  		= "user.dir";
    private static Logger sessionLogger = Logger.getLogger("SessionLogger");
    private static Logger sessionIdLogger = Logger.getLogger("SessionIdLogger");
    private static Logger redisLogger = Logger.getLogger("RedisLogger");
    static {
        PatternLayout layout = new PatternLayout(D_M_N);
        String userHome = System.getProperty(USER_HOME);
        if (!userHome.endsWith(File.separator)) {
            userHome += File.separator;
        }
        String path = userHome + DIR_NAME + File.separator;
        File dir = new File(path);
        if (!dir.exists()) {
        	try{
        		dir.mkdirs();
        	}catch(Exception e){
        		e.printStackTrace();
        	}
        }
        
        /**
         * sessionLogger
         */
        FileAppender appender = null;
        try {
            appender = new DailyRollingFileAppender(layout, path + "sessionInf.log", YYYY_MM_DD);
            appender.setAppend(true);
            appender.setEncoding(GBK);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (appender != null) {
            sessionLogger.removeAllAppenders();
            sessionLogger.addAppender(appender);
        }
//        sessionLog.setLevel(Level.INFO);
        sessionLogger.setAdditivity(false);

        /**
         * redisLogger
         */
        FileAppender redisAppender = null;
        try {
        	redisAppender = new DailyRollingFileAppender(layout, path + "redis.log", YYYY_MM_DD);
        	redisAppender.setAppend(true);
        	redisAppender.setEncoding(GBK);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (redisAppender != null) {
        	redisLogger.removeAllAppenders();
        	redisLogger.addAppender(redisAppender);
        }
        redisLogger.setLevel(Level.INFO);
        redisLogger.setAdditivity(false);
         
        /**
         * sessionIdLogger
         */
        FileAppender sessionIdAppender = null;
        try {
        	sessionIdAppender = new DailyRollingFileAppender(layout, path + "sessionId.log", YYYY_MM_DD);
        	sessionIdAppender.setAppend(true);
        	sessionIdAppender.setEncoding(GBK);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (sessionIdAppender != null) {
            sessionIdLogger.removeAllAppenders();
            sessionIdLogger.addAppender(sessionIdAppender);
        }
        sessionIdLogger.setLevel(Level.INFO);
        sessionIdLogger.setAdditivity(false);

    }
    
    public static Logger getSessionIdLogger(){
    	return sessionIdLogger;
    }
    
    public static Logger getSessionLogger() {
        return sessionLogger;
    }

    public static Logger getRedisLogger() {
        return redisLogger;
    }
}
