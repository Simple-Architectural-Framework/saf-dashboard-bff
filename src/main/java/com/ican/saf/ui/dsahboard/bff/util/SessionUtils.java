package com.ican.saf.ui.dsahboard.bff.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.server.WebSession;

public class SessionUtils {

    /**
     * Retrieves an attribute from the HTTP Session.
     * In MVC, this is synchronous and blocking, so no Map/Subscription hacks are needed.
     * @param request is an HttpServletRequest
     * @param sessionKey is a key to retrieve the attribute from the session
     */
    public static String getWebSessionAttribute(HttpServletRequest request, String sessionKey){
        var session = request.getSession(false);
        if( null == session ) return null;

        var attribute = session.getAttribute(sessionKey);
        return (attribute instanceof String) ? attribute.toString() : null;
    }

    /**
     * Clears attributes and rotates the session ID.
     * In MVC, this is a simple void or return-type operation without Mono.
     */
    public static WebSession clearSessionAndChangeSessionId(WebSession webSession){
        webSession.getAttributes().clear();
        webSession.changeSessionId();
        webSession.save();
        return webSession;
    }
}
