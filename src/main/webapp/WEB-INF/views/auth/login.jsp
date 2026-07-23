<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@page contentType="text/html" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Đăng nhập — HealthAlert</title>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/auth.css">
</head>
<body class="auth-page">
    <div class="auth-container">
        <div class="auth-logo">💙</div>
        <h1 class="auth-title">HealthAlert</h1>
        <p class="auth-subtitle">Đăng nhập để tiếp tục</p>

        <c:if test="${not empty AccountError}">
            <div class="auth-alert auth-alert-error">${AccountError}</div>
        </c:if>

        <form action="${pageContext.request.contextPath}/Logincontroller" method="post" autocomplete="on">
            <input type="hidden" name="service" value="checkaccount">

            <div class="auth-field">
                <label for="UserName">Email</label>
                <input id="UserName" type="text" name="UserName"
                       placeholder="vd: name@email.com"
                       value="${not empty UserName ? UserName : ''}"
                       autocomplete="username" required>
                <c:if test="${not empty emailError}">
                    <p class="auth-error-text">${emailError}</p>
                </c:if>
            </div>

            <div class="auth-field">
                <label for="password">Mật khẩu</label>
                <input id="password" type="password" name="password"
                       placeholder="Nhập mật khẩu"
                       autocomplete="current-password" required>
                <c:if test="${not empty passError}">
                    <p class="auth-error-text">${passError}</p>
                </c:if>
            </div>

            <div class="auth-row-actions">
                <a href="${pageContext.request.contextPath}/ForgotPassword">Quên mật khẩu?</a>
            </div>

            <button type="submit" class="auth-btn">Đăng nhập</button>
        </form>

        <p class="auth-footer">
            Chưa có tài khoản? <a href="${pageContext.request.contextPath}/RegisterController">Đăng ký</a>
        </p>
    </div>
</body>
</html>
