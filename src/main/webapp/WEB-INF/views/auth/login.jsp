<%-- 
    Document   : login.jsp
    Created on : May 22, 2026, 10:16:17 PM
    Author     : iac26
--%>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@page contentType="text/html" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="vi">

    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Login — Diabetes Support System</title>
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
                height: 100vh;
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
                from {
                    opacity: 0;
                    transform: translateY(-20px);
                }

                to {
                    opacity: 1;
                    transform: translateY(0);
                }
            }

            .logo {
                font-size: 2rem;
                margin-bottom: 6px;
            }

            h1 {
                color: #333;
                margin-bottom: 4px;
                font-size: 1.6rem;
            }

            .subtitle {
                color: #777;
                margin-bottom: 1.8rem;
                font-size: 0.9rem;
            }

            input[type="text"],
            input[type="password"] {
                width: 100%;
                padding: 12px 14px;
                margin-bottom: 6px;
                border: 1.5px solid #ddd;
                border-radius: 8px;
                font-size: 1rem;
                transition: border-color 0.2s;
                outline: none;
            }

            input[type="text"]:focus,
            input[type="password"]:focus {
                border-color: #4169e1;
            }

            .login-btn {
                width: 100%;
                padding: 12px;
                background: #4169e1;
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

            .login-btn:hover {
                background: #374bb5;
            }

            .error-text {
                color: #e74c3c;
                font-size: 0.82rem;
                text-align: left;
                margin-bottom: 8px;
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

            .divider {
                display: flex;
                align-items: center;
                gap: 10px;
                color: #bbb;
                font-size: 0.85rem;
                margin: 10px 0;
            }

            .divider::before,
            .divider::after {
                content: '';
                flex: 1;
                height: 1px;
                background: #ddd;
            }

            .forgot-link {
                display: block;
                color: #4169e1;
                text-decoration: none;
                font-size: 0.88rem;
                text-align: right;
                margin-bottom: 4px;
            }

            .forgot-link:hover {
                text-decoration: underline;
            }
        </style>
    </head>

    <body>
        <div class="container">
            <div class="logo">👔</div>
            <h1>Diabetes Support System</h1>
            <p class="subtitle">Login into the system</p>

            <%-- Form đăng nhập --%>
            <form action="Logincontroller" method="post">
                <input type="hidden" name="service" value="checkaccount">

                <input type="text" name="UserName" placeholder="Email hoặc tên đăng nhập"
                       value="${not empty UserName ? UserName : ''}" autocomplete="username">
                <c:if test="${not empty emailError}">
                    <p class="error-text">${emailError}</p>
                </c:if>

                <input type="password" name="password" placeholder="Mật khẩu"
                       autocomplete="current-password">
                <c:if test="${not empty passError}">
                    <p class="error-text">${passError}</p>
                </c:if>

                <a href="ForgotPassword" class="forgot-link">Change Password?</a>

                <c:if test="${not empty AccountError}">
                    <div class="alert-error">${AccountError}</div>
                </c:if>

                <button type="submit" class="login-btn">Login</button>
            </form>

            <div class="divider">Or</div>

            <%-- Đã sửa link từ file JSP sang Controller điều hướng --%>
            <div class="register-link-container" style="margin-top: 15px; font-size: 0.9rem; color: #555;">
                Don't have an account? <a href="RegisterController" style="color: #4169e1; text-decoration: none; font-weight: bold;">Register here</a>
            </div>
        </div>
    </body>
</html>