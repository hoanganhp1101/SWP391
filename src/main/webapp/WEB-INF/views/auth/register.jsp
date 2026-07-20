<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@page contentType="text/html" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Đăng ký — HealthAlert</title>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/auth.css">
</head>
<body class="auth-page">
    <div class="auth-container">
        <div class="auth-logo">📝</div>
        <h1 class="auth-title">Tạo tài khoản</h1>
        <p class="auth-subtitle">Tham gia hệ thống HealthAlert</p>

        <form action="RegisterController" method="post">
            <input type="hidden" name="service" value="createaccount">

            <div class="auth-field">
                <input type="text" name="hoTen" placeholder="Họ và tên"
                       value="${not empty hoTen ? hoTen : ''}" required>
                <c:if test="${not empty hoTenError}">
                    <p class="auth-error-text">${hoTenError}</p>
                </c:if>
            </div>

            <div class="auth-field">
                <input type="text" name="soDienThoai" placeholder="Số điện thoại"
                       value="${not empty soDienThoai ? soDienThoai : ''}" required>
                <c:if test="${not empty phoneError}">
                    <p class="auth-error-text">${phoneError}</p>
                </c:if>
            </div>

            <div class="auth-field">
                <input type="email" name="Email" placeholder="Địa chỉ Email"
                       value="${not empty Email ? Email : ''}" required>
                <c:if test="${not empty emailError}">
                    <p class="auth-error-text">${emailError}</p>
                </c:if>
            </div>

            <div class="auth-field">
                <input type="password" name="password" placeholder="Mật khẩu" required>
            </div>

            <div class="auth-field">
                <input type="password" name="confirmPassword" placeholder="Xác nhận mật khẩu" required>
                <c:if test="${not empty passError}">
                    <p class="auth-error-text">${passError}</p>
                </c:if>
            </div>

            <c:if test="${not empty RegisterError}">
                <div class="auth-alert auth-alert-error">${RegisterError}</div>
            </c:if>

            <button type="submit" class="auth-btn">Đăng ký</button>
        </form>

        <p class="auth-footer">
            Đã có tài khoản? <a href="Logincontroller">Đăng nhập</a>
        </p>
    </div>
</body>
</html>
