<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Quên mật khẩu — HealthAlert</title>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/auth.css">
</head>
<body class="auth-page">
    <div class="auth-container">
        <div class="auth-logo">🔐</div>
        <h1 class="auth-title">Quên mật khẩu?</h1>
        <p class="auth-subtitle">
            Nhập email đăng ký của bạn. Chúng tôi sẽ gửi mật khẩu mới về hộp thư.
        </p>

        <c:if test="${not empty error}">
            <div class="auth-alert auth-alert-error">${error}</div>
        </c:if>
        <c:if test="${not empty success}">
            <div class="auth-alert auth-alert-success">${success}</div>
        </c:if>

        <c:if test="${empty success}">
            <form action="ForgotPassword" method="post">
                <div class="auth-field">
                    <input type="email" name="email" placeholder="Nhập địa chỉ email của bạn"
                           value="${not empty email ? email : ''}" required autofocus>
                </div>
                <button type="submit" class="auth-btn">Gửi mật khẩu mới</button>
            </form>
        </c:if>

        <p class="auth-footer" style="margin-top: 1rem;">
            <a href="Logincontroller" class="auth-back-link">← Quay lại đăng nhập</a>
        </p>
    </div>
</body>
</html>
