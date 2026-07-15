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
                background: #f0f4f8; /* Nền xám xanh nhạt đồng bộ */
                display: flex;
                justify-content: center;
                align-items: center;
                min-height: 100vh;
                overflow: hidden;
            }

            .container {
                background: #ffffff;
                padding: 2.5rem 2rem;
                border-radius: 12px;
                box-shadow: 0 4px 20px rgba(21, 87, 213, 0.08); /* Đổ bóng tone xanh */
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
            
            h1 { 
                color: #1a1a1a; 
                margin-bottom: 4px; 
                font-size: 1.6rem; 
            }
            
            .subtitle { 
                color: #666; 
                margin-bottom: 1.8rem; 
                font-size: 0.9rem; 
            }

            input[type="text"],
            input[type="email"],
            input[type="password"] {
                width: 100%;
                padding: 12px 14px;
                margin-bottom: 10px;
                border: 1.5px solid #e0e4e8;
                border-radius: 8px;
                font-size: 1rem;
                transition: all 0.3s ease;
                outline: none;
                background-color: #fafbfc;
            }

            input[type="text"]:focus,
            input[type="email"]:focus,
            input[type="password"]:focus {
                border-color: #1557d5; /* Viền xanh chủ đạo khi focus */
                background-color: #ffffff;
                box-shadow: 0 0 0 3px rgba(21, 87, 213, 0.1);
            }

            .register-btn {
                width: 100%;
                padding: 12px;
                background: #1557d5; /* Nút bấm xanh chủ đạo */
                color: #ffffff;
                border: none;
                border-radius: 8px;
                font-size: 1rem;
                cursor: pointer;
                font-weight: bold;
                transition: background 0.2s, transform 0.1s;
                margin-top: 8px;
                margin-bottom: 12px;
            }

            .register-btn:hover { 
                background: #1046ab; 
            }
            
            .register-btn:active {
                transform: scale(0.98);
            }

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
            
            .login-link-container {
                margin-top: 10px; 
                font-size: 0.9rem; 
                color: #555;
            }
            
            .login-link-container a {
                color: #1557d5; 
                text-decoration: none; 
                font-weight: bold;
            }
            
            .login-link-container a:hover {
                text-decoration: underline;
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
                Already have an account? <a href="Logincontroller" style="color: #4169e1; text-decoration: none; font-weight: bold;">Login here</a>
            </div>
        </div>
    </body>
</html>