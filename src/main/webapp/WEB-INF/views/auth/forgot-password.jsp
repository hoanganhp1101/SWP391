<%-- 
    forgot-password.jsp — Form quên mật khẩu 
--%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Quên Mật Khẩu — Fashion Warehouse</title>
          <style>
            * { box-sizing: border-box; margin: 0; padding: 0; }

            body {
                font-family: 'Arial', sans-serif;
                background: #f0f4f8; /* Nền xám xanh nhạt đồng bộ */
                display: flex;
                justify-content: center;
                align-items: center;
                height: 100vh;
                overflow: hidden;
            }

            .container {
                background: #ffffff;
                padding: 2.5rem 2rem;
                border-radius: 12px;
                box-shadow: 0 4px 20px rgba(21, 87, 213, 0.08); /* Đổ bóng tone xanh êm dịu */
                max-width: 420px;
                width: 100%;
                text-align: center;
                animation: fadeIn 0.4s ease;
            }

            @keyframes fadeIn {
                from { opacity: 0; transform: translateY(-20px); }
                to   { opacity: 1; transform: translateY(0); }
            }

            .icon {
                font-size: 48px;
                margin-bottom: 10px;
            }

            h1 {
                color: #1a1a1a;
                margin-bottom: 8px;
                font-size: 1.6rem;
            }

            .subtitle {
                color: #666;
                font-size: 0.95rem;
                margin-bottom: 1.8rem;
                line-height: 1.5;
            }

            input[type="email"] {
                width: 100%;
                padding: 12px 14px;
                margin-bottom: 1rem;
                border: 1.5px solid #e0e4e8;
                border-radius: 8px;
                font-size: 1rem;
                transition: all 0.3s ease;
                outline: none;
                background-color: #fafbfc;
            }

            input[type="email"]:focus {
                border-color: #1557d5; /* Đổi màu viền khi focus */
                background-color: #ffffff;
                box-shadow: 0 0 0 3px rgba(21, 87, 213, 0.1);
            }

            .btn-submit {
                width: 100%;
                padding: 12px;
                background: #1557d5; /* Nút bấm màu xanh chủ đạo */
                color: #ffffff;
                border: none;
                border-radius: 8px;
                font-size: 1rem;
                cursor: pointer;
                font-weight: bold;
                transition: background 0.2s, transform 0.1s;
                margin-bottom: 1rem;
            }

            .btn-submit:hover {
                background: #1046ab;
            }

            .btn-submit:active {
                transform: scale(0.98);
            }

            .btn-back {
                display: inline-block;
                color: #1557d5; /* Đồng bộ màu chữ link */
                text-decoration: none;
                font-size: 0.9rem;
                font-weight: 500;
            }

            .btn-back:hover {
                text-decoration: underline;
            }

            .alert {
                padding: 12px 14px;
                border-radius: 8px;
                margin-bottom: 1rem;
                font-size: 0.9rem;
                text-align: left;
                line-height: 1.5;
            }

            .alert-error {
                background: #fff2f2;
                border: 1px solid #ffa0a0;
                color: #c0392b;
            }

            .alert-success {
                background: #f0fff4;
                border: 1px solid #6fcf97;
                color: #1e7e34;
            }
        </style>
    </head>
    <body>
        <div class="container">
            <div class="icon">🔐</div>
            <h1>Quên mật khẩu?</h1>
            <p class="subtitle">
                Nhập email đăng ký của bạn. Chúng tôi sẽ gửi mật khẩu mới về hộp thư.
            </p>

            <c:if test="${not empty error}">
                <div class="alert alert-error">${error}</div>
            </c:if>
            <c:if test="${not empty success}">
                <div class="alert alert-success">${success}</div>
            </c:if>

            <c:if test="${empty success}">
                <form action="ForgotPassword" method="post">
                    <input type="email" name="email" placeholder="Nhập địa chỉ email của bạn"
                           value="${not empty email ? email : ''}" required autofocus>
                    <button type="submit" class="btn-submit">Gửi mật khẩu mới</button>
                </form>
            </c:if>

            <a href="Logincontroller" class="btn-back">← Quay lại Đăng nhập</a>
        </div>
    </body>
</html>
