<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Sign in · CampusFlow</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/app.css">
</head>
<body class="login-body">
<div class="login-layout">
    <section class="login-brand-panel">
        <div class="brand-lockup"><span class="brand-mark">C</span><span>CampusFlow</span></div>
        <div class="brand-copy">
            <span class="eyebrow light">CAMPUS OPERATIONS, SIMPLIFIED</span>
            <h1>Every class.<br><em>Every student.</em><br>Accounted for.</h1>
            <p>A clearer way to record attendance, understand engagement and keep your academic community connected.</p>
        </div>
        <div class="brand-bottom"><div class="mini-avatars"><span>AS</span><span>PM</span><span>RK</span><span>+5</span></div><span>One connected campus workspace</span></div>
        <div class="decor-orbit orbit-one"></div><div class="decor-orbit orbit-two"></div>
    </section>
    <section class="login-form-panel">
        <div class="login-form-wrap">
            <span class="eyebrow">WELCOME BACK</span>
            <h2>Sign in to your workspace</h2>
            <p class="muted">Use your college account to continue.</p>
            <c:if test="${not empty error}"><div class="alert alert-error"><span class="alert-dot"></span><c:out value="${error}"/></div></c:if>
            <form method="post" action="${pageContext.request.contextPath}/login" class="login-form">
                <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}">
                <label for="email">College email</label>
                <div class="input-wrap"><span class="input-icon">✉</span><input id="email" name="email" type="email" placeholder="name@college.edu" autocomplete="username" required></div>
                <div class="label-row"><label for="password">Password</label><span class="tiny-note">Secure campus access</span></div>
                <div class="input-wrap"><span class="input-icon">⌑</span><input id="password" name="password" type="password" placeholder="Enter your password" autocomplete="current-password" required><button class="password-toggle" type="button" aria-label="Show password" data-toggle-password>Show</button></div>
                <button class="btn btn-primary btn-full" type="submit">Sign in <span>→</span></button>
            </form>
            <div class="demo-box">
                <div class="demo-box-head"><span class="demo-spark">✦</span><div><strong>Explore the demo</strong><small>Test each role with these sample accounts.</small></div></div>
                <div class="demo-grid">
                    <button type="button" class="demo-account" data-fill-email="admin@college.edu" data-fill-password="Admin@123"><span class="demo-role admin-dot">A</span><span><b>Admin</b><small>Admin@123</small></span><span class="demo-arrow">↗</span></button>
                    <button type="button" class="demo-account" data-fill-email="faculty@college.edu" data-fill-password="Faculty@123"><span class="demo-role faculty-dot">F</span><span><b>Faculty</b><small>Faculty@123</small></span><span class="demo-arrow">↗</span></button>
                    <button type="button" class="demo-account" data-fill-email="student@college.edu" data-fill-password="Student@123"><span class="demo-role student-dot">S</span><span><b>Student</b><small>Student@123</small></span><span class="demo-arrow">↗</span></button>
                </div>
            </div>
            <p class="login-footnote">CampusFlow · College Attendance Management System</p>
        </div>
    </section>
</div>
<script src="${pageContext.request.contextPath}/assets/app.js"></script>
</body>
</html>
