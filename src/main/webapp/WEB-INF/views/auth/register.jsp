<%-- 
    Document   : register.jsp
    Created on : May 24, 2026
    Author     : iac26
--%>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@page contentType="text/html" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="vi">

    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Register — Diabetes Support System</title>
        <style>
            * {
                box-sizing: border-box;
                margin: 0;
                padding: 0;
            }

            body {
                font-family: 'Arial', sans-serif;
                background: linear-gradient(135deg, #4169e1 0%, #ff7f50 100%);
                display: flex;
                justify-content: center;
                align-items: center;
                min-height: 100vh;
                overflow: hidden;
            }

            .container {
                background: #fff;
                padding: 2.5rem 2rem;
                border-radius: 12px;
                box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
                max-width: 420px;
                width: 100%;
                text-align: center;
                animation: fadeIn 0.4s ease;
            }

            @keyframes fadeIn {
                from { opacity: 0; transform: translateY(-20px); }
                to { opacity: 1; transform: translateY(0); }
            }

            .logo { font-size: 2rem; margin-bottom: 6px; }
            h1 { color: #333; margin-bottom: 4px; font-size: 1.6rem; }
            .subtitle { color: #777; margin-bottom: 1.8rem; font-size: 0.9rem; }

            input[type="text"],
            input[type="email"],
            input[type="password"] {
                width: 100%;
                padding: 12px 14px;
                margin-bottom: 10px;
                border: 1.5px solid #ddd;
                border-radius: 8px;
                font-size: 1rem;
                transition: border-color 0.2s;
                outline: none;
            }

            input[type="text"]:focus,
            input[type="email"]:focus,
            input[type="password"]:focus {
                border-color: #4169e1;
            }

            .register-btn {
                width: 100%;
                padding: 12px;
                background: #ff7f50; 
                color: #fff;
                border: none;
                border-radius: 8px;
                font-size: 1rem;
                cursor: pointer;
                font-weight: bold;
                transition: background 0.2s;
                margin-top: 8px;
                margin-bottom: 12px;
            }

            .register-btn:hover { background: #e06c40; }

            .error-text {
                color: #e74c3c;
                font-size: 0.82rem;
                text-align: left;
                margin-top: -6px;
                margin-bottom: 8px;
                padding-left: 4px;
            }

            .alert-error {
                background: #fff2f2;
                border: 1px solid #ffa0a0;
                color: #c0392b;
                padding: 10px 12px;
                border-radius: 8px;
                font-size: 0.88rem;
                margin-bottom: 10px;
                text-align: left;
            }
        </style>
    </head>

    <body>
        <div class="container">
            <div class="logo">📝</div>
            <h1>Create Account</h1>
            <p class="subtitle">Join Diabetes Support System</p>

            <%-- Form đăng ký hành động gửi dữ liệu lên RegisterController --%>
            <form action="RegisterController" method="post">
                <input type="hidden" name="service" value="createaccount">

                <input type="text" name="hoTen" placeholder="Họ và tên"
                       value="${not empty hoTen ? hoTen : ''}" required>
                <c:if test="${not empty hoTenError}">
                    <p class="error-text">${hoTenError}</p>
                </c:if>

                <input type="text" name="soDienThoai" placeholder="Số điện thoại"
                       value="${not empty soDienThoai ? soDienThoai : ''}" required>
                <c:if test="${not empty phoneError}">
                    <p class="error-text">${phoneError}</p>
                </c:if>

                <input type="email" name="Email" placeholder="Địa chỉ Email"
                       value="${not empty Email ? Email : ''}" required>
                <c:if test="${not empty emailError}">
                    <p class="error-text">${emailError}</p>
                </c:if>

                <input type="password" name="password" placeholder="Mật khẩu" required>
                
                <input type="password" name="confirmPassword" placeholder="Xác nhận mật khẩu" required>
                <c:if test="${not empty passError}">
                    <p class="error-text">${passError}</p>
                </c:if>

                <c:if test="${not empty RegisterError}">
                    <div class="alert-error">${RegisterError}</div>
                </c:if>

                <button type="submit" class="register-btn">Register</button>
            </form>

            <div style="margin-top: 10px; font-size: 0.9rem; color: #555;">
                Already have an account? <a href="login.jsp" style="color: #4169e1; text-decoration: none; font-weight: bold;">Login here</a>
            </div>
        </div>
    </body>
</html>