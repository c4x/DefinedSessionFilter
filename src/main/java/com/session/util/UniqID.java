package com.session.util;

import java.io.IOException;
import java.net.InetAddress;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Random;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.session.log.SessionLogger;



/**
 * @author wuyuan.lfk
 * @date  2012-11-13
 */
public class UniqID {
	private static final Logger logger =SessionLogger.getSessionLogger();
    private static char[]       digits = {
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'
    };
    private static UniqID me       = new UniqID();
    private String        hostAddr;
    private Random        random  = new SecureRandom();
    private MessageDigest mHasher;
    private UniqTimer     timer   = new UniqTimer();

    private UniqID() {
        try {
            InetAddress addr = InetAddress.getLocalHost();

            hostAddr = addr.getHostAddress();
        } catch (IOException e) {
            logger.error("[UniqID] Get HostAddr Error", e);
            hostAddr = String.valueOf(System.currentTimeMillis());
        }

        if (StringUtils.isBlank(hostAddr) || "127.0.0.1".equals(hostAddr)) {
        	hostAddr = String.valueOf(System.currentTimeMillis());
        }

        if (logger.isDebugEnabled()) {
			logger.debug("[UniqID]hostAddr is:" + hostAddr);
        }

        try {
            mHasher = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException nex) {
            mHasher = null;
            logger.error("[UniqID]new MD5 Hasher error", nex);
        }
    }

    public static UniqID getInstance() {
        return me;
    }

    /**
     * 可能是ip，也可能是系统时间毫秒
     * @return
     */
    public String getHostAddr(){
    	return hostAddr;
    }
    
    public String getUniqID2() {
        StringBuffer sb = new StringBuffer();
        long         t = timer.getCurrentTime2();

        sb.append(t);

        sb.append("-");

        sb.append(random.nextInt(8999) + 1000);

        sb.append("-");
        sb.append(hostAddr);

        sb.append("-");
        sb.append(Thread.currentThread().hashCode());

        if (logger.isDebugEnabled()) {
            logger.debug("[UniqID.getUniqID]" + sb.toString());
        }

        return sb.toString();
    }
    
    public String getUniqID(){
    	StringBuffer sb = new StringBuffer();
        long         t = timer.getCurrentTime();

        sb.append(t);

        sb.append("-");

        sb.append(random.nextInt(89999) + 10000);

        sb.append("-");
        sb.append(hostAddr);

        sb.append("-");
        sb.append(Thread.currentThread().hashCode());

        if (logger.isDebugEnabled()) {
            logger.debug("[UniqID.getUniqID]" + sb.toString());
        }

        return sb.toString();
    }

    public String getUniqIDHash() {
        return hash(getUniqID());
    }
    
    public synchronized String hash(String str) {
        byte[] bt = mHasher.digest(str.getBytes());
        int    l = bt.length;

        char[] out = new char[l << 1];

        for (int i = 0, j = 0; i < l; i++) {
            out[j++]     = digits[(0xF0 & bt[i]) >>> 4];
            out[j++]     = digits[0x0F & bt[i]];
        }

        if (logger.isDebugEnabled()) {
            logger.debug("[UniqID.hash]" + (new String(out)));
        }
        
        return new String(out);
    }

    private class UniqTimer {
        private long lastTime2 = System.currentTimeMillis();

        public synchronized long getCurrentTime2() {
            long currTime2 = System.currentTimeMillis();
            lastTime2 = Math.max(lastTime2 + 1, currTime2);
            return lastTime2;
        }
        
        
        private long lastTime = System.nanoTime();
        public synchronized long getCurrentTime(){
        	long currTime = System.nanoTime();
        	lastTime = Math.max(lastTime, currTime);
        	return lastTime;
        }
    }
}

