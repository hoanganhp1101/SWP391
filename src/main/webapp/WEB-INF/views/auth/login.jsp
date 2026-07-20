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
        <title>Đăng nhập — HealthAlert</title>
         <style>
            * {
                box-sizing: border-box;
                margin: 0;
                padding: 0;
            }

            body {
                font-family: 'Arial', sans-serif;
                /* Nền xám xanh nhạt rất nhẹ, giúp khối form màu trắng nổi bật hẳn lên */
                background: #f0f4f8; 
                display: flex;
                justify-content: center;
                align-items: center;
                height: 100vh;
                overflow: hidden;
            }

            .container {
                background: #ffffff; /* Khối trắng chủ đạo */
                padding: 2.5rem 2rem;
                border-radius: 12px;
                /* Đổ bóng nhẹ nhàng hơn để phù hợp với nền sáng */
                box-shadow: 0 4px 20px rgba(21, 87, 213, 0.08); 
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
                /* Màu chữ tiêu đề đậm và sắc nét hơn */
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
            input[type="password"] {
                width: 100%;
                padding: 12px 14px;
                margin-bottom: 6px;
                border: 1.5px solid #e0e4e8;
                border-radius: 8px;
                font-size: 1rem;
                transition: all 0.3s ease;
                outline: none;
                background-color: #fafbfc;
            }

            /* Đổi màu viền khi click vào ô nhập liệu thành màu chủ đạo */
            input[type="text"]:focus,
            input[type="password"]:focus {
                border-color: #1557d5; 
                background-color: #ffffff;
                box-shadow: 0 0 0 3px rgba(21, 87, 213, 0.1);
            }

            .login-btn {
                width: 100%;
                padding: 12px;
                /* Sử dụng màu chủ đạo cho nút bấm */
                background: #1557d5; 
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

            /* Màu khi di chuột qua nút (đậm hơn #1557d5 một chút) */
            .login-btn:hover {
                background: #1046ab; 
            }
            
            .login-btn:active {
                transform: scale(0.98);
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
                color: #999;
                font-size: 0.85rem;
                margin: 15px 0;
            }

            .divider::before,
            .divider::after {
                content: '';
                flex: 1;
                height: 1px;
                background: #eaeaea;
            }

            .forgot-link {
                display: block;
                /* Link quên mật khẩu dùng màu chủ đạo */
                color: #1557d5; 
                text-decoration: none;
                font-size: 0.88rem;
                text-align: right;
                margin-bottom: 4px;
                font-weight: 500;
            }

            .forgot-link:hover {
                text-decoration: underline;
            }
            
            .register-link-container {
                margin-top: 15px; 
                font-size: 0.9rem; 
                color: #555;
            }
            
            .register-link-container a {
                /* Link đăng ký dùng màu chủ đạo */
                color: #1557d5; 
                text-decoration: none; 
                font-weight: bold;
            }
            
            .register-link-container a:hover {
                text-decoration: underline;
            }
        </style>
    </head>

    <body>
        <div class="container">
            <div class="logo">🩺</div>
            <h1>HealthAlert</h1>
            <p class="subtitle">Đăng nhập hệ thống — tự chuyển theo vai trò</p>

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

                <a href="ForgotPassword" class="forgot-link">Quên mật khẩu?</a>

                <c:if test="${not empty AccountError}">
                    <div class="alert-error">${AccountError}</div>
                </c:if>

                <button type="submit" class="login-btn">Đăng nhập</button>
            </form>

            <div class="divider">Hoặc</div>

            <div class="register-link-container" style="margin-top: 15px; font-size: 0.9rem; color: #555;">
                Chưa có tài khoản? <a href="RegisterController" style="color: #4169e1; text-decoration: none; font-weight: bold;">Đăng ký</a>
            </div>
        </div>
    </body>
</html>