package com.college.attendance.servlet;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@WebServlet("/logout")
public class LogoutServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        res.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "Use the sign-out button to end your session.");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        HttpSession session = req.getSession(false);
        String expected = session == null ? null : (String) session.getAttribute("csrfToken");
        String supplied = req.getParameter("_csrf");
        if (expected == null || supplied == null || !MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.UTF_8), supplied.getBytes(StandardCharsets.UTF_8))) {
            res.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid or missing security token.");
            return;
        }
        session.invalidate();
        res.sendRedirect(req.getContextPath() + "/login");
    }
}
