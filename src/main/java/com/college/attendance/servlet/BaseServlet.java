package com.college.attendance.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import java.io.IOException;

abstract class BaseServlet extends HttpServlet {
    protected long userId(HttpServletRequest req) { return ((Number) req.getSession().getAttribute("userId")).longValue(); }
    protected String role(HttpServletRequest req) { return String.valueOf(req.getSession().getAttribute("role")); }
    protected String userName(HttpServletRequest req) { return String.valueOf(req.getSession().getAttribute("userName")); }
    protected void page(HttpServletRequest req, HttpServletResponse res, String view, String title, String active)
            throws ServletException, IOException {
        req.setAttribute("pageTitle", title);
        req.setAttribute("activePage", active);
        req.getRequestDispatcher("/WEB-INF/views/" + view + ".jsp").forward(req, res);
    }
    protected void flash(HttpServletRequest req, String type, String message) {
        req.getSession().setAttribute("flashType", type);
        req.getSession().setAttribute("flashMessage", message);
    }
    protected void requireRole(HttpServletRequest req, HttpServletResponse res, String... allowed) throws IOException {
        String actual = role(req);
        for (String a : allowed) if (a.equals(actual)) return;
        res.sendError(HttpServletResponse.SC_FORBIDDEN, "You do not have permission to access this page.");
    }
    protected String value(HttpServletRequest req, String key) {
        String s = req.getParameter(key);
        return s == null ? "" : s.trim();
    }
    protected int intValue(HttpServletRequest req, String key, int fallback) {
        try { return Integer.parseInt(value(req, key)); } catch (NumberFormatException e) { return fallback; }
    }
    protected long longValue(HttpServletRequest req, String key, long fallback) {
        try { return Long.parseLong(value(req, key)); } catch (NumberFormatException e) { return fallback; }
    }
}
