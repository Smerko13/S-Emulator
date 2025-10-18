package utils;

import constants.Constants;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

public class SessionUtils {

    public static String getUsername (HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        Object sessionAttribute = session != null ? session.getAttribute(Constants.USERNAME) : null;
        return sessionAttribute != null ? sessionAttribute.toString() : null;
    }
    
    public static String getUserId (HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        Object sessionAttribute = session != null ? session.getAttribute(Constants.USER_ID) : null;
        // If USER_ID is not set, fall back to USERNAME
        if (sessionAttribute == null) {
            sessionAttribute = session != null ? session.getAttribute(Constants.USERNAME) : null;
        }
        return sessionAttribute != null ? sessionAttribute.toString() : null;
    }

    public static void clearSession (HttpServletRequest request) {
        request.getSession().invalidate();
    }
}