package com.college.attendance.servlet;

import com.college.attendance.util.Jdbc;
import com.college.attendance.util.PasswordUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Map;
import java.util.UUID;

@WebServlet(urlPatterns = {"/", "/login"})
public class LoginServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session != null && session.getAttribute("userId") != null) {
            res.sendRedirect(req.getContextPath() + "/app/dashboard");
            return;
        }
        HttpSession loginSession = req.getSession(true);
        if (loginSession.getAttribute("csrfToken") == null) {
            loginSession.setAttribute("csrfToken", UUID.randomUUID().toString());
        }
        req.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(req, res);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        String email = req.getParameter("email") == null ? "" : req.getParameter("email").trim().toLowerCase();
        String password = req.getParameter("password") == null ? "" : req.getParameter("password");
        HttpSession loginSession = req.getSession(false);
        String expectedToken = loginSession == null ? null : (String) loginSession.getAttribute("csrfToken");
        String suppliedToken = req.getParameter("_csrf");
        if (expectedToken == null || suppliedToken == null || !MessageDigest.isEqual(
                expectedToken.getBytes(StandardCharsets.UTF_8), suppliedToken.getBytes(StandardCharsets.UTF_8))) {
            res.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid or missing security token. Reload the sign-in page and try again.");
            return;
        }
        try {
            Map<String, Object> user = Jdbc.one("SELECT id, full_name, email, password_hash, role FROM app_users WHERE email=? AND active=TRUE", email);
            if (user != null && PasswordUtil.verify(password, String.valueOf(user.get("password_hash")))) {
                HttpSession old = req.getSession(false);
                if (old != null) old.invalidate();
                HttpSession session = req.getSession(true);
                session.setAttribute("userId", user.get("id"));
                session.setAttribute("userName", user.get("full_name"));
                session.setAttribute("userEmail", user.get("email"));
                session.setAttribute("role", user.get("role"));
                session.setAttribute("csrfToken", UUID.randomUUID().toString());
                session.setMaxInactiveInterval(30 * 60);
                res.sendRedirect(req.getContextPath() + "/app/dashboard");
            } else {
                req.setAttribute("error", "The email or password you entered is incorrect.");
                req.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(req, res);
            }
        } catch (Exception ex) {
            getServletContext().log("Login failed", ex);
            req.setAttribute("error", "Unable to connect to the database. Check the setup instructions and try again.");
            req.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(req, res);
        }
    }
}
