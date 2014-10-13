package com.session.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;



import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;

import com.session.DefinedSessionFilter;
import com.session.DefinedSessionServletRequest;
import com.session.log.SessionLogger;


public class UserCheckUtil {

	private static final Logger logger = Logger.getLogger(DefinedSessionFilter.class);
    private static Pattern p = Pattern.compile("(?<=http://|\\.)[^.]*?\\.(com|cn|net|org|biz|info|cc|tv|hk)",
        Pattern.CASE_INSENSITIVE);

    public static boolean domainCheck(DefinedSessionServletRequest request,PropertiesConfiguration config) {
        String url = request.getRequestURL().toString();

        try {
//            if (null != url && (url.contains("localhost") || url.contains("127.0.0.1"))) {
//                return false;
//            }
        	//for test
            if (null != url && (url.contains("localhost") || url.contains("127.0.0.1"))) {
                return true;
            }else {
                Matcher matcher = p.matcher(url);
                if (!matcher.find()) {
                    return false;
                }
                String requestDomain = matcher.group();
                String defaultDomain = config.getString("domain");
                if (defaultDomain.indexOf(requestDomain) != -1) {
                    return true;
                }
            }
        }
        catch (Exception e) {
            logger.error(e + " url=" + url, e);
            return false;
        }
        return false;
    }

}
