package com.sj.sound_diary.util;

import javax.servlet.http.HttpSession;

public class SessionUtils {

    private SessionUtils() {
    }

    @SuppressWarnings("unchecked")
    public static <T> T attr(HttpSession session, String key) {
        return (T) session.getAttribute(key);
    }
}
