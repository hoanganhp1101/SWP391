<%-- 
    Document   : login.jsp
    Created on : May 22, 2026, 10:16:17 PM
    Author     : iac26
--%>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@page contentType="text/html" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="vi">

    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Login — Diabates Support System</title>
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

            /* Google button */
            .btn-google {
                display: flex;
                align-items: center;
                justify-content: center;
                gap: 10px;
                width: 100%;
                padding: 11px;
                background: #fff;
                color: #444;
                border: 1.5px solid #ddd;
                border-radius: 8px;
                font-size: 0.95rem;
                cursor: pointer;
                text-decoration: none;
                font-weight: 500;
                transition: background 0.2s, box-shadow 0.2s;
                margin-bottom: 12px;
            }

            .btn-google:hover {
                background: #f5f5f5;
                box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
            }

            .btn-google svg {
                width: 20px;
                height: 20px;
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
            <h1>Diabates Support System</h1>
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

            <%-- Google OAuth2 Login --%>
            <a href="https://accounts.google.com/o/oauth2/auth?scope=email%20profile%20openid&redirect_uri=http://localhost:8080/FashionWarehouse/GoogleLogin&response_type=code&client_id=377198838448-eo3s2hbdnjnf1dqd8785gd6vm3dg5cjo.apps.googleusercontent.com&access_type=online&prompt=select_account"
               class="btn-google">
                <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 48 48">
                <path fill="#EA4335"
                      d="M24 9.5c3.54 0 6.71 1.22 9.21 3.6l6.85-6.85C35.9 2.38 30.47 0 24 0 14.62 0 6.51 5.38 2.56 13.22l7.98 6.19C12.43 13.72 17.74 9.5 24 9.5z" />
                <path fill="#4285F4"
                      d="M46.98 24.55c0-1.57-.15-3.09-.38-4.55H24v9.02h12.94c-.58 2.96-2.26 5.48-4.78 7.18l7.73 6c4.51-4.18 7.09-10.36 7.09-17.65z" />
                <path fill="#FBBC05"
                      d="M10.53 28.59c-.48-1.45-.76-2.99-.76-4.59s.27-3.14.76-4.59l-7.98-6.19C.92 16.46 0 20.12 0 24c0 3.88.92 7.54 2.56 10.78l7.97-6.19z" />
                <path fill="#34A853"
                      d="M24 48c6.48 0 11.93-2.13 15.89-5.81l-7.73-6c-2.15 1.45-4.92 2.3-8.16 2.3-6.26 0-11.57-4.22-13.47-9.91l-7.98 6.19C6.51 42.62 14.62 48 24 48z" />
                <path fill="none" d="M0 0h48v48H0z" />
                </svg>
                Login with Google
            </a>
            <div class="register-link-container" style="margin-top: 15px; font-size: 0.9rem; color: #555;">
                Don't have an account? <a href="views/auth/register.jsp" style="color: #4169e1; text-decoration: none; font-weight: bold;">Register here</a>
            </div>
        </div>
    </body>
</html>