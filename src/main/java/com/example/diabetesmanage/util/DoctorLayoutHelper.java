package com.example.diabetesmanage.util;

import com.example.diabetesmanage.model.User;
import jakarta.servlet.http.HttpServletRequest;

public final class DoctorLayoutHelper {

    private DoctorLayoutHelper() {
    }

    public static void prepare(HttpServletRequest request, User user, String activeMenu) {
        prepare(request, user, activeMenu, activeMenu);
    }

    public static void prepare(
            HttpServletRequest request,
            User user,
            String activeMenu,
            String activeTopNav
    ) {
        if (user != null) {
            request.setAttribute("doctor", user);
        }
        if (activeMenu != null && !activeMenu.isBlank()) {
            request.setAttribute("activeMenu", activeMenu);
        }
        if (activeTopNav != null && !activeTopNav.isBlank()) {
            request.setAttribute("activeTopNav", activeTopNav);
        }
    }
}
